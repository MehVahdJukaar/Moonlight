package net.mehvahdjukaar.selene.resourcepack.asset_generators.textures;

import com.google.gson.JsonObject;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.datafixers.util.Pair;
import net.mehvahdjukaar.selene.math.colors.HSVColor;
import net.mehvahdjukaar.selene.resourcepack.RPUtils;
import net.mehvahdjukaar.selene.resourcepack.ResType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.GsonHelper;
import org.apache.commons.compress.utils.IOUtils;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.BiConsumer;

public final class SpriteUtils {

    /**
     * Shorthand method to read a NativeImage
     */
    public static NativeImage readImage(ResourceManager manager, ResourceLocation resourceLocation) throws IOException {
        return NativeImage.read(manager.getResource(resourceLocation).getInputStream());
    }

    public static void forEachPixel(NativeImage image, BiConsumer<Integer, Integer> function){
        for (int x = 0; x < image.getWidth(); x++) {
            for (int y = 0; y < image.getHeight(); y++) {
                function.accept(x,y);
            }
        }
    }


    //TODO: maybe use HCL here

    /**
     * Algorithm that approximates and generates a texture to be used on signs based off its corresponding planks texture.
     * It basically removes last 2 colors and adds another highlight
     * Returns a list of Palettes to work with possible animated (plank) textures
     * @param planksTexture plank texture of the desired wood type
     */
    public static List<Palette> extrapolateSignBlockPalette(TextureImage planksTexture) {
        List<Palette> newPalettes = new ArrayList<>();
        List<Palette> oakPalettes = Palette.fromAnimatedImage(planksTexture, null, 1/300);
        for(Palette palette : oakPalettes) {
            int size = palette.size();
            if (size == 7) {
                PaletteColor color = palette.get(size - 3);
                HSVColor hsv = color.rgb().asHSV();
                //just saturates last color
                float satIncrease = 1 / 0.94f;
                float brightnessIncrease = 1 / 0.94f;
                HSVColor newCol = new HSVColor(hsv.hue(), hsv.saturation() * satIncrease, hsv.value() * brightnessIncrease, hsv.alpha());
                PaletteColor newP = new PaletteColor(newCol);
                newP.occurrence = color.occurrence;
                palette.set(size - 1, newP);
                palette.remove(size - 2);
            }
            newPalettes.add(palette);
        }
        return newPalettes;
    }

    //
    /**
     * Algorithm that approximates and generates a texture to be used on wooden item.
     * It basically just darkens the first color
     * Returns just one Palette since items should not have animated textures
     * @param planksTexture plank texture of the desired wood type
     */
    public static Palette extrapolateWoodItemPalette(TextureImage planksTexture) {
        Palette palette = Palette.fromAnimatedImage(planksTexture, null).get(0);
        PaletteColor color = palette.get(0);
        HSVColor hsv = color.rgb().asHSV();
        //just saturates last color
        float satIncrease = 1.11f;
        float brightnessIncrease = 0.945f;
        HSVColor newCol = new HSVColor(hsv.hue(), hsv.saturation() * satIncrease, hsv.value() * brightnessIncrease,hsv.alpha());
        PaletteColor newP = new PaletteColor(newCol);
        newP.occurrence = color.occurrence;
        palette.set(0, newP);
        return palette;
    }




    //Better use LAB color
    public static float getLuminance(int r, int g, int b) {
        return (0.299f * r + 0.587f * g + 0.114f * b);
    }


}

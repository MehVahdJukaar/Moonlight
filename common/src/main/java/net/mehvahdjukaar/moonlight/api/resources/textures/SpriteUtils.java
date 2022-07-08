package net.mehvahdjukaar.moonlight.api.resources.textures;

import com.mojang.blaze3d.platform.NativeImage;
import net.mehvahdjukaar.moonlight.api.util.math.colors.HSVColor;
import net.mehvahdjukaar.moonlight.api.util.math.colors.RGBColor;
import net.mehvahdjukaar.moonlight.api.util.math.kmeans.DataSet;
import net.mehvahdjukaar.moonlight.api.util.math.kmeans.KMeans;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Function;

public final class SpriteUtils {

    /**
     * Shorthand method to read a NativeImage
     */
    public static NativeImage readImage(ResourceManager manager, ResourceLocation resourceLocation) throws IOException, NoSuchElementException {
        try (var res = manager.getResource(resourceLocation).get().open()) {
            return NativeImage.read(res);
        } catch (Exception e) {
            throw new IOException(e);
        }
    }

    public static void forEachPixel(NativeImage image, BiConsumer<Integer, Integer> function) {
        for (int x = 0; x < image.getWidth(); x++) {
            for (int y = 0; y < image.getHeight(); y++) {
                function.accept(x, y);
            }
        }
    }

    public static void grayscaleImage(NativeImage image) {
        forEachPixel(image, (x, y) -> image.setPixelRGBA(x, y,
                new RGBColor(image.getPixelRGBA(x, y)).asHCL().withChroma(0).asRGB().toInt()));
    }


    //TODO: maybe use HCL here

    /**
     * Algorithm that approximates and generates a texture to be used on signs based off its corresponding planks texture.
     * It basically removes last 2 colors and adds another highlight
     * Returns a list of Palettes to work with possible animated (plank) textures
     *
     * @param planksTexture plank texture of the desired wood type
     */
    public static List<Palette> extrapolateSignBlockPalette(TextureImage planksTexture) {
        List<Palette> newPalettes = new ArrayList<>();
        List<Palette> oakPalettes = Palette.fromAnimatedImage(planksTexture, null, 1 / 300f);
        for (Palette palette : oakPalettes) {
            int size = palette.size();
            if (size == 7) {
                PaletteColor color = palette.get(size - 3);
                HSVColor hsv = color.rgb().asHSV();
                //just saturates last color
                float satIncrease = 1 / 0.94f;
                float brightnessIncrease = 1 / 0.94f;
                HSVColor newCol = new HSVColor(hsv.hue(),
                        Mth.clamp(hsv.saturation() * satIncrease, 0, 1),
                        Mth.clamp(hsv.value() * brightnessIncrease, 0, 1),
                        hsv.alpha());
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
     *
     * @param planksTexture plank texture of the desired wood type
     */
    public static Palette extrapolateWoodItemPalette(TextureImage planksTexture) {
        Palette palette = Palette.fromAnimatedImage(planksTexture, null).get(0);
        PaletteColor color = palette.get(0);
        HSVColor hsv = color.rgb().asHSV();
        //just saturates last color
        float satMult = 1.11f;
        float brightnessMult = 0.94f;
        HSVColor newCol = new HSVColor(hsv.hue(),
                Mth.clamp(hsv.saturation() * satMult, 0, 1),
                Mth.clamp(hsv.value() * brightnessMult, 0, 1),
                hsv.alpha());
        PaletteColor newP = new PaletteColor(newCol);
        newP.occurrence = color.occurrence;
        palette.set(0, newP);
        return palette;
    }


    //Better use LAB color
    @Deprecated
    public static float getLuminance(int r, int g, int b) {
        return (0.299f * r + 0.587f * g + 0.114f * b);
    }


    public static void reduceColors(NativeImage image, Function<Integer, Integer> sizeFn) {

        // read data
        Palette p = Palette.fromImage(TextureImage.of(image, null), null, 0);

        if (p.size() == 0) return;
        DataSet<DataSet.ColorPoint> data = DataSet.fromPalette(p);

        int size = sizeFn.apply(p.size());

        if (size >= p.size()) return;

        // cluster
        KMeans.kMeans(data, size);

        Map<Integer, Integer> colorToColorMap = new HashMap<>();

        for (var c : data.getColorPoints()) {
            var centroid = data.getLastCentroids().get(c.getClusterNo());
            colorToColorMap.put(c.cast().getColor().value(), centroid.cast().getColor().value());
        }

        SpriteUtils.forEachPixel(image, (x, y) -> {
            int i = image.getPixelRGBA(x, y);
            if (colorToColorMap.containsKey(i)) {
                image.setPixelRGBA(x, y, colorToColorMap.get(i));
            }
        });

    }


    @NotNull
    public static boolean looksLikeTopLogTexture(String s) {
        s = new ResourceLocation(s).getPath();
        return s.contains("_top") || s.contains("_end") || s.contains("_up");
    }

    @NotNull
    public static boolean looksLikeSideLogTexture(String s) {
        return !looksLikeTopLogTexture(s);
    }
}

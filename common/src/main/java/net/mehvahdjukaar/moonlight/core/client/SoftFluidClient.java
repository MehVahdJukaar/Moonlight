package net.mehvahdjukaar.moonlight.core.client;
/*
import net.mehvahdjukaar.moonlight.core.Moonlight;
import net.mehvahdjukaar.moonlight.fluids.SoftFluid;
import net.mehvahdjukaar.moonlight.fluids.SoftFluidRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.FastColor;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraftforge.client.event.TextureStitchEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class SoftFluidClient extends GenericSimpleResourceReloadListener {

    public SoftFluidClient() {
        super("textures/soft_fluids", ".png");
    }

    @Override
    public void apply(List<ResourceLocation> locations, ResourceManager manager, ProfilerFiller filler) {
        TEXTURES_TO_STITCH.clear();
        TEXTURES_TO_STITCH.addAll(locations);
    }

    private static final List<ResourceLocation> TEXTURES_TO_STITCH = new ArrayList<>();

    private static final HashMap<ResourceLocation, Integer> PARTICLE_COLORS = new HashMap<>();

    public static List<ResourceLocation> getTexturesToStitch() {
        //fluids aren't registered here, so we can't just iterate over them
        var list = new ArrayList<ResourceLocation>();
        TEXTURES_TO_STITCH.forEach(e -> list.add(new ResourceLocation(e.getNamespace(), "soft_fluids/" + e.getPath())));
        return list;
    }

    //TODO: possibly do it for ALL fluids, not only non grayscale ones
    public static void refresh() {
        if (Minecraft.getInstance().level == null) return;
        var v = SoftFluidRegistry.getEntries();
        PARTICLE_COLORS.clear();
        for (var entry : v) {
            SoftFluid s = entry.getValue();
            ResourceLocation key = entry.getKey().location();
            if (!PARTICLE_COLORS.containsKey(key) && !s.isColored()) {
                ResourceLocation location = s.getStillTexture();
                if (location == null) continue;
                TextureAtlas textureMap = Minecraft.getInstance().getModelManager().getAtlas(TextureAtlas.LOCATION_BLOCKS);
                TextureAtlasSprite sprite = textureMap.getSprite(location);
                int averageColor = -1;
                try {
                    averageColor = getColorFrom(sprite, s.getTintColor());
                } catch (Exception e) {
                    Moonlight.LOGGER.warn("Failed to load particle color for " + sprite + " using current resource pack. might be a broken png.mcmeta");
                }
                PARTICLE_COLORS.put(key, averageColor);
            }
        }
    }

    public static int get(SoftFluid s) {
        return PARTICLE_COLORS.getOrDefault(SoftFluidRegistry.getID(s), -1);
    }


    //credits to Random832
    private static int getColorFrom(TextureAtlasSprite sprite, int tint) {
        if (sprite == null || sprite.getFrameCount() == 0) return -1;

        int tintR = tint >> 16 & 255;
        int tintG = tint >> 8 & 255;
        int tintB = tint & 255;
        int total = 0, totalR = 0, totalB = 0, totalG = 0;

        for (int tryFrame = 0; tryFrame < sprite.getFrameCount(); tryFrame++) {
            try {
                for (int x = 0; x < sprite.getWidth(); x++) {
                    for (int y = 0; y < sprite.getHeight(); y++) {

                        int pixel = sprite.getPixelRGBA(tryFrame, x, y);

                        // this is in 0xAABBGGRR format, not the usual 0xAARRGGBB.
                        int pixelB = pixel >> 16 & 255;
                        int pixelG = pixel >> 8 & 255;
                        int pixelR = pixel & 255;
                        ++total;
                        totalR += pixelR;
                        totalG += pixelG;
                        totalB += pixelB;
                    }
                }
                break;
            } catch (Exception e) {
                total = 0;
                totalR = 0;
                totalB = 0;
                totalG = 0;
            }
        }
        if (total <= 0) return -1;
        return FastColor.ARGB32.color(255,
                totalR / total * tintR / 255,
                totalG / total * tintG / 255,
                totalB / total * tintB / 255);
    }


}
*/
//TODO: disabled, re add

import net.mehvahdjukaar.moonlight.api.fluids.SoftFluid;

public  class SoftFluidClient{
    public static int get(SoftFluid fluid) {
        return 0;
    }

    public static void refresh() {
    }
}
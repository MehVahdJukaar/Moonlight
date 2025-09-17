package net.mehvahdjukaar.moonlight.api.fluids;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import dev.architectury.injectables.annotations.ExpectPlatform;
import net.mehvahdjukaar.moonlight.api.client.TextureCache;
import net.mehvahdjukaar.moonlight.api.client.texture_renderer.RenderedTexturesManager;
import net.mehvahdjukaar.moonlight.api.misc.RegistryAccessJsonReloadListener;
import net.mehvahdjukaar.moonlight.api.platform.ClientHelper;
import net.mehvahdjukaar.moonlight.core.Moonlight;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.SpriteContents;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;
import net.minecraft.world.level.BlockAndTintGetter;

import java.util.Map;

// client class
public class SoftFluidColors extends RegistryAccessJsonReloadListener {

    public SoftFluidColors() {
        super(new Gson(), "moonlight/dummy");
    }

    @Override
    public void parse(Map<ResourceLocation, JsonElement> jsonMap, RegistryAccess access) {
        RenderedTexturesManager.clearCache();
        TextureCache.clear();
        refreshParticleColors(access);
    }

    //why is it called twice?
    //once on fluid map reload (server join on client) since there we have all the fluids
    //then once when resources are reloaded (resource pack change) since these depend on textures
    public static void refreshParticleColors(RegistryAccess access) {
        Minecraft mc = Minecraft.getInstance();

        Registry<SoftFluid> reg = access.registry(SoftFluidRegistry.KEY).orElse(null);
        //dumb velocity
        if (reg == null) {
            for (SoftFluid f : SoftFluidRegistry.getValues()) {
                //purple, clearly something is wrong
                f.averageTextureTint = 0xff_aa00ff;
            }
            Moonlight.warnInvalidServer();
            return;
        }

        for (var entry : reg.entrySet()) {
            SoftFluid fluid = entry.getValue();
            ResourceLocation location = fluid.getStillTexture();
            int averageColor = -1;
            int tint = fluid.getTintMethod().appliesToStill() ? fluid.getTintColor() : -1;

            TextureAtlas textureMap = mc.getModelManager().getAtlas(TextureAtlas.LOCATION_BLOCKS);
            TextureAtlasSprite sprite = textureMap.getSprite(location);
            try {
                averageColor = getAverageColor(sprite, tint);
            } catch (Exception e) {
                Moonlight.LOGGER.warn("Failed to load particle color for {} using current resource pack. might be a broken png.mcmeta", sprite);
            }
            fluid.averageTextureTint = averageColor;
        }
    }

    //credits to Random832
    @SuppressWarnings("ConstantConditions")
    private static int getAverageColor(TextureAtlasSprite sprite, int tint) {
        SpriteContents c = sprite.contents();
        if (sprite == null || c.getFrameCount() == 0) return -1;

        int tintR = tint >> 16 & 255;
        int tintG = tint >> 8 & 255;
        int tintB = tint & 255;
        int total = 0, totalR = 0, totalB = 0, totalG = 0;

        for (int tryFrame = 0; tryFrame < c.getFrameCount(); tryFrame++) {
            try {
                for (int x = 0; x < c.width(); x++) {
                    for (int y = 0; y < c.height(); y++) {

                        int pixel = ClientHelper.getPixelRGBA(sprite, tryFrame, x, y);

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

    @ExpectPlatform
    public static int getSpecialColor(SoftFluidStack softFluidStack, BlockAndTintGetter world, BlockPos pos) {
        throw new AssertionError();
    }
}
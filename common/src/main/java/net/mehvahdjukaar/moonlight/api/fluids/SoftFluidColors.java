package net.mehvahdjukaar.moonlight.api.fluids;

import dev.architectury.injectables.annotations.ExpectPlatform;
import net.mehvahdjukaar.moonlight.api.client.TextureCache;
import net.mehvahdjukaar.moonlight.api.client.texture_renderer.RenderedTexturesManager;
import net.mehvahdjukaar.moonlight.api.platform.ClientHelper;
import net.mehvahdjukaar.moonlight.core.Moonlight;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.util.FastColor;
import net.minecraft.world.level.BlockAndTintGetter;

// client class
public class SoftFluidColors implements ResourceManagerReloadListener {

    @Override
    public void onResourceManagerReload(ResourceManager resourceManager) {
        RenderedTexturesManager.clearCache();
        TextureCache.clear();
        refreshParticleColors();
    }

    public static void refreshParticleColors() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;

        Registry<SoftFluid> reg = SoftFluidRegistry.getRegistry(mc.level.registryAccess());
        if (reg == null) {                          // ← 防御：注册表不存在
            // 清空旧缓存，避免脏数据
            for (SoftFluid f : SoftFluidRegistry.getValues())
                f.averageTextureTint = -1;
            return;                                 // 安静返回，不继续跑
        }

        var fluids = reg.entrySet();
        for (var entry : fluids) {
            SoftFluid fluid = entry.getValue();
            ResourceLocation location = fluid.getStillTexture();
            int averageColor = -1;
            int tint = fluid.getTintMethod().appliesToStill() ? fluid.getTintColor() : -1;

            TextureAtlas textureMap = mc.getModelManager().getAtlas(TextureAtlas.LOCATION_BLOCKS);
            TextureAtlasSprite sprite = textureMap.getSprite(location);
            try {
                averageColor = getAverageColor(sprite, tint);
            } catch (Exception e) {
                Moonlight.LOGGER.warn("Failed to load particle color for " + sprite +
                        " using current resource pack. might be a broken png.mcmeta");
            }
            fluid.averageTextureTint = averageColor;
        }
    }

    //credits to Random832
    @SuppressWarnings("ConstantConditions")
    private static int getAverageColor(TextureAtlasSprite sprite, int tint) {
        var c = sprite.contents();
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
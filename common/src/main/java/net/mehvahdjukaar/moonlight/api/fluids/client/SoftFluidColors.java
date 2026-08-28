package net.mehvahdjukaar.moonlight.api.fluids.client;

import net.mehvahdjukaar.moonlight.api.client.TextureCache;
import net.mehvahdjukaar.moonlight.api.client.texture_renderer.DynamicTextureRenderer;
import net.mehvahdjukaar.moonlight.api.fluids.SoftFluid;
import net.mehvahdjukaar.moonlight.api.fluids.SoftFluidRegistry;
import net.mehvahdjukaar.moonlight.api.fluids.SoftFluidStack;
import net.mehvahdjukaar.moonlight.api.fluids.SoftFluidTank;
import net.mehvahdjukaar.moonlight.api.platform.ClientHelper;
import net.mehvahdjukaar.moonlight.api.resources.textures.PalettedPermutationsHelper;
import net.mehvahdjukaar.moonlight.core.Moonlight;
import net.minecraft.client.Minecraft;
import net.minecraft.client.color.block.BlockTintSource;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.FluidModel;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.AtlasIds;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.util.ARGB;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.component.DyedItemColor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import org.jetbrains.annotations.Nullable;

/**
 * Client side tint helpers for soft fluids, kept out of SoftFluidStack and SoftFluidTank so a dedicated
 * server never loads client types.
 */
public class SoftFluidColors implements ResourceManagerReloadListener {

    @Override
    public void onResourceManagerReload(ResourceManager resourceManager) {
        //also using this to reset texture cache
        DynamicTextureRenderer.clearCache();

        //also using for this
        TextureCache.clear();
        PalettedPermutationsHelper.invalidate();

        ClientLevel level = Minecraft.getInstance().level;
        if (level != null) {
            refreshParticleColors(SoftFluidRegistry.get(level.registryAccess()));
        }
    }

    public static void onFluidsSynced(RegistryAccess registryAccess) {
        refreshParticleColors(SoftFluidRegistry.get(registryAccess));
    }

    // fluid models bake on a resource reload while soft fluids are built on a datapack one, so the
    // use_texture_from appearance can't be resolved in the SoftFluid constructor
    public static void refreshParticleColors(Registry<SoftFluid> reg) {
        for (var fluid : reg) {
            fluid.setResolvedAppearance(resolveAppearance(fluid.getTextureOverride()));

            Identifier location = fluid.getStillTexture();
            int averageColor = -1;

            int tint = fluid.getTintMethod().appliesToStill() ? fluid.getTintColor() : -1;

            TextureAtlas textureMap = Minecraft.getInstance().getAtlasManager().getAtlasOrThrow(AtlasIds.BLOCKS);
            TextureAtlasSprite sprite = textureMap.getSprite(location);
            try {
                averageColor = getAverageColor(sprite, tint);
            } catch (Exception e) {
                Moonlight.LOGGER.warn("Failed to load particle color for {} using current resource pack. might be a broken png.mcmeta", sprite);
            }
            fluid.setAverageTextureTint(averageColor);
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

                        int pixel = ClientHelper.getPixelABGR(sprite, tryFrame, x, y);

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
        return ARGB.color(255,
                totalR / total * tintR / 255,
                totalG / total * tintG / 255,
                totalB / total * tintB / 255);
    }

    @Nullable
    private static SoftFluid.Appearance resolveAppearance(@Nullable Identifier useTexturesFrom) {
        if (useTexturesFrom == null) return null;
        Fluid fluid = BuiltInRegistries.FLUID.getValue(useTexturesFrom);
        if (fluid == null || fluid == Fluids.EMPTY) return null;
        FluidState state = fluid.defaultFluidState();
        FluidModel model = Minecraft.getInstance().getModelManager().getFluidStateModelSet().get(state);
        BlockTintSource tint = model.tintSource();
        return new SoftFluid.Appearance(
                model.stillMaterial().sprite().contents().name(),
                model.flowingMaterial().sprite().contents().name(),
                tint == null ? -1 : tint.color(state.createLegacyBlock()));
    }

    /**
     * Tint of the vanilla fluid or dye the stack maps onto, 0 when there is none.
     */
    public static int getSpecialColor(SoftFluidStack stack, @Nullable BlockAndTintGetter world, @Nullable BlockPos pos) {
        //yay hardcoding
        DyedItemColor dyeColor = stack.get(DataComponents.DYED_COLOR);
        if (dyeColor != null) return dyeColor.rgb();

        PotionContents potionContents = stack.get(DataComponents.POTION_CONTENTS);
        if (potionContents != null) return potionContents.getColor();

        DyeColor discreteDyeColor = stack.get(DataComponents.BASE_COLOR);
        if (discreteDyeColor != null) return discreteDyeColor.getTextureDiffuseColor();

        //at least this works for any fluid
        Fluid fluid = stack.getVanillaFluid().value();
        if (fluid == Fluids.EMPTY) return 0;
        FluidState fluidState = fluid.defaultFluidState();
        BlockTintSource tint = Minecraft.getInstance().getModelManager()
                .getFluidStateModelSet().get(fluidState).tintSource();
        if (tint == null) return 0;
        BlockState blockState = fluidState.createLegacyBlock();
        int color = world != null && pos != null
                ? tint.colorInWorld(blockState, world, pos)
                : tint.color(blockState);
        return color == -1 ? 0 : color;
    }

    /**
     * @return tint color to be applied on the fluid texture
     */
    public static int getStillColor(SoftFluidStack fluidStack, @Nullable BlockAndTintGetter world, @Nullable BlockPos pos) {
        SoftFluid fluid = fluidStack.fluid();
        SoftFluid.TintMethod method = fluid.getTintMethod();
        if (method == SoftFluid.TintMethod.NO_TINT) return -1;
        int specialColor = getSpecialColor(fluidStack, world, pos);

        if (specialColor != 0) return specialColor;
        return fluid.getTintColor();
    }

    /**
     * @return tint color to be applied on the flowing fluid texture
     */
    public static int getFlowingColor(SoftFluidStack fluidStack, @Nullable BlockAndTintGetter world, @Nullable BlockPos pos) {
        SoftFluid.TintMethod method = fluidStack.fluid().getTintMethod();
        if (method == SoftFluid.TintMethod.FLOWING) return getParticleColor(fluidStack, world, pos);
        else return getStillColor(fluidStack, world, pos);
    }

    /**
     * @return tint color to be used on particles. Differs from the still color since it falls back to a color
     * extrapolated from the fluid texture itself
     */
    public static int getParticleColor(SoftFluidStack fluidStack, @Nullable BlockAndTintGetter world, @Nullable BlockPos pos) {
        int tintColor = getStillColor(fluidStack, world, pos);
        //if tint color is white gets averaged color
        if (tintColor == -1) return fluidStack.fluid().getAverageTextureTintColor();
        return tintColor;
    }

    public static int getCachedStillColor(SoftFluidTank tank, @Nullable BlockAndTintGetter world, @Nullable BlockPos pos) {
        return refreshIfNeeded(tank, world, pos).still;
    }

    public static int getCachedFlowingColor(SoftFluidTank tank, @Nullable BlockAndTintGetter world, @Nullable BlockPos pos) {
        return refreshIfNeeded(tank, world, pos).flowing;
    }

    public static int getCachedParticleColor(SoftFluidTank tank, @Nullable BlockAndTintGetter world, @Nullable BlockPos pos) {
        return refreshIfNeeded(tank, world, pos).particle;
    }

    private static SoftFluidTank.TintCache refreshIfNeeded(SoftFluidTank tank, @Nullable BlockAndTintGetter world,
                                                           @Nullable BlockPos pos) {
        SoftFluidTank.TintCache cache = tank.getTintCache();
        if (cache.needsRefresh) {
            SoftFluidStack fluidStack = tank.getFluid();
            cache.still = getStillColor(fluidStack, world, pos);
            cache.flowing = getFlowingColor(fluidStack, world, pos);
            cache.particle = getParticleColor(fluidStack, world, pos);
            cache.needsRefresh = false;
        }
        return cache;
    }

}

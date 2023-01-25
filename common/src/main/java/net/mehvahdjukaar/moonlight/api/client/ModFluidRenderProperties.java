package net.mehvahdjukaar.moonlight.api.client;

import com.mojang.blaze3d.shaders.FogShape;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.FogRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.material.FluidState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Register with ClientPlatformHelper.registerFluidRenderProperties
 * Extend to implement more properties
 * <p>
 * Again this just wraps forge stuff. Some of its basic properties are picked up and registered with fabric api
 */
public class ModFluidRenderProperties {

    private final ResourceLocation flowing;
    private final ResourceLocation still;
    private final int tint;

    public ModFluidRenderProperties(ResourceLocation still, ResourceLocation flowing, int tint) {
        this.still = still;
        this.flowing = flowing;
        this.tint = tint;
    }

    public ModFluidRenderProperties(ResourceLocation still, ResourceLocation flowing) {
        this(still, flowing, 0xFFFFFFFF);
    }


    public int getTintColor() {
        return tint;
    }

    @NotNull
    public ResourceLocation getStillTexture() {
        return still;
    }

    @NotNull
    public ResourceLocation getFlowingTexture() {
        return flowing;
    }

    /**
     * the reference of the texture to apply to a fluid directly touching
     * a non-opaque block. Null will call flowing or still textures
     */
    @Nullable
    public ResourceLocation getOverlayTexture() {
        return null;
    }

    /**
     * Modifies how the fog is currently being rendered when the camera is
     * within a fluid.
     */
    public void modifyFogRender(Camera camera, FogRenderer.FogMode mode, float renderDistance, float partialTick, float nearDistance, float farDistance, FogShape shape) {
    }

    // Level accessors

    public ResourceLocation getStillTexture(FluidState state, BlockAndTintGetter getter, BlockPos pos) {
        return this.getStillTexture();
    }

    public ResourceLocation getFlowingTexture(FluidState state, BlockAndTintGetter getter, BlockPos pos) {
        return this.getFlowingTexture();
    }

    public ResourceLocation getOverlayTexture(FluidState state, BlockAndTintGetter getter, BlockPos pos) {
        return this.getOverlayTexture();
    }

    public int getTintColor(FluidState state, BlockAndTintGetter getter, BlockPos pos) {
        return this.getTintColor();
    }
}

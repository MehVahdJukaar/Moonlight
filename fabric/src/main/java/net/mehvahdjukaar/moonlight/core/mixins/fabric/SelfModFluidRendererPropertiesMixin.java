package net.mehvahdjukaar.moonlight.core.mixins.fabric;

import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandler;
import net.mehvahdjukaar.moonlight.api.client.ModFluidRenderProperties;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.material.FluidState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

//implementing is enough. they have same signature
@Mixin(ModFluidRenderProperties.class)
public abstract class SelfModFluidRendererPropertiesMixin implements FluidRenderHandler {

    @Unique
    protected final TextureAtlasSprite[] sprites = new TextureAtlasSprite[3];

    @Shadow
    public abstract int getTintColor(FluidState state, BlockAndTintGetter getter, BlockPos pos);

    @Shadow
    @NotNull
    public abstract ResourceLocation getStillTexture();

    @Shadow
    @NotNull
    public abstract ResourceLocation getFlowingTexture();

    @Shadow
    @Nullable
    public abstract ResourceLocation getOverlayTexture();

    @Override
    public int getFluidColor(@Nullable BlockAndTintGetter view, @Nullable BlockPos pos, FluidState state) {
        return getTintColor(state, view, pos);
    }


    @Override
    public void reloadTextures(TextureAtlas textureAtlas) {
        sprites[0] = textureAtlas.getSprite(getStillTexture());
        sprites[1] = textureAtlas.getSprite(getFlowingTexture());

        var overlayTexture = this.getOverlayTexture();
        if (overlayTexture != null) {
            sprites[2] = textureAtlas.getSprite(overlayTexture);
        }
    }

    @Override
    public TextureAtlasSprite[] getFluidSprites(@Nullable BlockAndTintGetter view, @Nullable BlockPos pos, FluidState state) {
        return sprites;
    }
}

package net.mehvahdjukaar.moonlight.core.mixins.fabric;

import com.mojang.blaze3d.platform.NativeImage;
import net.mehvahdjukaar.moonlight.core.misc.fabric.ITextureAtlasSpriteExtension;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(TextureAtlasSprite.class)
public abstract class TextureAtlasSpriteMixin implements ITextureAtlasSpriteExtension {


    @Shadow @Final @Nullable private TextureAtlasSprite.AnimatedTexture animatedTexture;

    @Shadow @Final
    int width;

    @Shadow @Final protected NativeImage[] mainImage;

    @Shadow @Final
    int height;


    public int getPixelRGBA(int frameIndex, int x, int y) {
        if (this.animatedTexture != null) {
            x += this.animatedTexture.getFrameX(frameIndex) * this.width;
            y += this.animatedTexture.getFrameY(frameIndex) * this.height;
        }
        return this.mainImage[0].getPixelRGBA(x, y);
    }


}

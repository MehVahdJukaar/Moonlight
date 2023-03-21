package net.mehvahdjukaar.moonlight.core.mixins.fabric;

import com.mojang.blaze3d.platform.NativeImage;
import net.mehvahdjukaar.moonlight.core.misc.fabric.ITextureAtlasSpriteExtension;
import net.minecraft.client.renderer.texture.SpriteContents;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(TextureAtlasSprite.class)
public abstract class TextureAtlasSpriteMixin implements ITextureAtlasSpriteExtension {


    @Shadow @Final private SpriteContents contents;

    public int getPixelRGBA(int frameIndex, int x, int y) {
        if (this.contents.animatedTexture != null) {
            x += this.contents.animatedTexture.getFrameX(frameIndex) * this.contents.width();
            y += this.contents.animatedTexture.getFrameY(frameIndex) * this.contents.height();
        }

        return this.contents.originalImage.getPixelRGBA(x, y);
    }


}

package net.mehvahdjukaar.moonlight.core.mixins;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.platform.TextureUtil;
import net.mehvahdjukaar.moonlight.core.ClientConfigs;
import net.mehvahdjukaar.moonlight.core.MoonlightClient;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.Dumpable;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import java.io.IOException;
import java.nio.file.Path;

@Mixin(DynamicTexture.class)
public abstract class DynamicTextureMixin extends AbstractTexture implements Dumpable {

    @Shadow
    private NativeImage pixels;

    @WrapOperation(method = "upload", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/platform/NativeImage;upload(IIIZ)V"))
    public void forceMipMap(NativeImage instance, int a, int b, int c, boolean autoClose, Operation<Void> op) {
        if (MoonlightClient.isMapMipMap()) {
            this.mipmap = true;
            instance.upload(a, b, c, 0, 0, instance.getWidth(), instance.getHeight(),
                    false, true, true, autoClose);
        } else op.call(instance, a, b, c, autoClose);
    }

    @WrapOperation(method = "<init>(IIZ)V", at = @At(value = "INVOKE",
            target = "Lcom/mojang/blaze3d/platform/TextureUtil;prepareImage(III)V"))
    public void forceMipMap(int a, int b, int c, Operation<Void> op) {
        if (MoonlightClient.isMapMipMap()) {
            this.mipmap = true;
            TextureUtil.prepareImage(a, ClientConfigs.MAPS_MIPMAP.get(), b, c);
        } else op.call(a, b, c);

    }

    /**
     * @author MehVahdJukaar
     * @reason proper mipmap
     */
    @Overwrite
    public void dumpContents(ResourceLocation pResourceLocation, Path pPath) {
        if (this.pixels != null) {
            String s = pResourceLocation.toDebugFileName();
            TextureUtil.writeAsPNG(pPath, s, this.getId(), this.mipmap ? ClientConfigs.MAPS_MIPMAP.get() : 0, this.pixels.getWidth(), this.pixels.getHeight());
        }
    }
}

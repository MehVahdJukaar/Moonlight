package net.mehvahdjukaar.moonlight.core.mixins;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.mehvahdjukaar.moonlight.api.client.util.RenderUtil;
import net.mehvahdjukaar.moonlight.core.ClientConfigs;
import net.mehvahdjukaar.moonlight.core.MoonlightClient;
import net.minecraft.client.gui.MapRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// All require 0 because of Optishit. I should have not given in...
@Mixin(targets = "net/minecraft/client/gui/MapRenderer$MapInstance", priority = 900)
public abstract class MapInstanceMixin {


    @WrapOperation(method = "updateTexture",
            require = 0,
            at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/renderer/texture/DynamicTexture;upload()V"))
    public void forceMipMap(DynamicTexture instance, Operation<Void> op) {
        MoonlightClient.setMipMap(true);
        op.call(instance);
        MoonlightClient.setMipMap(false);
    }

    @WrapOperation(
            method = {"<init>"},
            require = 0,
            at = {@At(
                    value = "NEW",
                    target ="(IIZ)Lnet/minecraft/client/renderer/texture/DynamicTexture;",
                    remap = false
            )}
    )
    private DynamicTexture forceMipMapOn(int width, int height, boolean useCalloc, Operation<DynamicTexture> original) {
        MoonlightClient.setMipMap(true);
        var t = original.call(width, height, useCalloc);
        MoonlightClient.setMipMap(false);
        return t;
    }

    @WrapOperation(method = "<init>",
            require = 0,
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/RenderType;text(Lnet/minecraft/resources/ResourceLocation;)Lnet/minecraft/client/renderer/RenderType;"))
    private RenderType getTextMipMap(ResourceLocation pLocation, Operation<RenderType> op) {
        if (ClientConfigs.MAPS_MIPMAP.get() != 0) {
            return RenderUtil.getTextMipmapRenderType(pLocation);
        } else return op.call(pLocation);
    }
}

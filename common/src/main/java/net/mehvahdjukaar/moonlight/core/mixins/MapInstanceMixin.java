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

@Mixin(targets = "net/minecraft/client/gui/MapRenderer$MapInstance", priority = 900)
public abstract class MapInstanceMixin {


    @WrapOperation(method = "updateTexture", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/renderer/texture/DynamicTexture;upload()V"))
    public void forceMipMap(DynamicTexture instance, Operation<Void> op) {
        MoonlightClient.setMipMap(true);
        op.call(instance);
        MoonlightClient.setMipMap(false);
    }

    @Inject(
            method = {"<init>"},
            at = {@At(
                    value = "moonlight:INVOKE_UNRESTRICTED",
                    target = "Ljava/lang/Object;<init>()V",
                    remap = false
            )}
    )
    private void forceMipMapOn(MapRenderer arg, int i, MapItemSavedData arg2, CallbackInfo ci) {
        MoonlightClient.setMipMap(true);
    }

    @Inject(method = "<init>", at = @At("RETURN"))
    public void forceMipMapOff(MapRenderer r, int pId, MapItemSavedData pData, CallbackInfo ci) {
        MoonlightClient.setMipMap(false);
    }

    @WrapOperation(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/RenderType;text(Lnet/minecraft/resources/ResourceLocation;)Lnet/minecraft/client/renderer/RenderType;"))
    private RenderType getTextMipMap(ResourceLocation pLocation, Operation<RenderType> op) {
        if (ClientConfigs.MAPS_MIPMAP.get() != 0) {
            return RenderUtil.getTextMipmapRenderType(pLocation);
        } else return op.call(pLocation);
    }
}

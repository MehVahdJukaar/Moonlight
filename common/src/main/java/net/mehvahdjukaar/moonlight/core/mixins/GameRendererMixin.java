package net.mehvahdjukaar.moonlight.core.mixins;

import net.mehvahdjukaar.moonlight.core.MoonlightClient;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = GameRenderer.class, priority = 501)
public abstract class GameRendererMixin {

    @Inject(method = "render", at = @At(value = "INVOKE",
            target = "Lcom/mojang/blaze3d/platform/Lighting;setupFor3DItems()V"))
    private void moonlight$messWithEntityRendererLighting(float partialTicks, long nanoTime, boolean renderLevel, CallbackInfo ci) {
        MoonlightClient.beforeRenderGui();
    }

    @Inject(method = "render", at = @At(value = "TAIL"))
    private void moonlight$resetEntityRendererLighting(float partialTicks, long nanoTime, boolean renderLevel, CallbackInfo ci) {
        MoonlightClient.afterRenderGui();
    }

}

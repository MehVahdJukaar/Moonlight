package net.mehvahdjukaar.moonlight.core.mixins;

import net.mehvahdjukaar.moonlight.core.MoonlightClient;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = GameRenderer.class, priority = 501)
public abstract class GameRendererMixin {

    @Inject(method = "render", at = @At(value = "NEW",
            target = "(Lnet/minecraft/client/Minecraft;Lnet/minecraft/client/renderer/MultiBufferSource$BufferSource;)Lnet/minecraft/client/gui/GuiGraphics;"))
    private void moonlight$messWithEntityRendererLighting(DeltaTracker deltaTracker, boolean bl, CallbackInfo ci) {
        MoonlightClient.beforeRenderGui();
    }

    @Inject(method = "render", at = @At(value = "TAIL"))
    private void moonlight$resetEntityRendererLighting(DeltaTracker deltaTracker, boolean bl, CallbackInfo ci) {
        MoonlightClient.afterRenderGui();
    }

}

package net.mehvahdjukaar.moonlight.core.mixins;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import net.mehvahdjukaar.moonlight.core.ClientConfigs;
import net.mehvahdjukaar.moonlight.core.MoonlightClient;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = Lighting.class, priority = 501)
public abstract class LightingMixin {

    @Inject(method = "setupLevel", at = @At(value = "HEAD"), cancellable = true)
    private static void moonlight$messWithEntityRendererLighting(CallbackInfo ci) {
        if(MoonlightClient.fixShade != ClientConfigs.ShadeFix.FALSE){
            RenderSystem.setupLevelDiffuseLighting(MoonlightClient.NEW_L_0, MoonlightClient.NEW_L_1);
            ci.cancel();
        }
    }

    @Inject(method = "setupNetherLevel", at = @At(value = "HEAD"), cancellable = true)
    private static void moonlight$messEntityRendererLightingN(CallbackInfo ci) {
        if(MoonlightClient.fixShade != ClientConfigs.ShadeFix.FALSE){
            RenderSystem.setupLevelDiffuseLighting(MoonlightClient.NEW_L_0, MoonlightClient.NEW_L_1);
            ci.cancel();
        }
    }


    @Inject(method = "setupFor3DItems", at = @At(value = "HEAD"), cancellable = true)
    private static void moonlight$resetEntityRendererLightingGui(CallbackInfo ci) {
        if(MoonlightClient.fixShade == ClientConfigs.ShadeFix.TRUE){
            RenderSystem.setupGui3DDiffuseLighting(MoonlightClient.NEW_L_0, MoonlightClient.NEW_L_1);
            ci.cancel();
        }
    }

}

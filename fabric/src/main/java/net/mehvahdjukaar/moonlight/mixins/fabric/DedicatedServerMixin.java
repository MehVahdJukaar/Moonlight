package net.mehvahdjukaar.moonlight.mixins.fabric;

import net.mehvahdjukaar.moonlight.fabric.MoonlightFabric;
import net.minecraft.server.dedicated.DedicatedServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(DedicatedServer.class)
public class DedicatedServerMixin {

    @Inject(method = "initServer", at = @At("RETURN"))
    private void onInitServer(CallbackInfoReturnable<Boolean> cir) {
        if (cir.getReturnValue()) {
            MoonlightFabric.onCommonSetup();
        }
    }
}
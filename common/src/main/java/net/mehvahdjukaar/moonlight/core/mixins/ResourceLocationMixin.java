package net.mehvahdjukaar.moonlight.core.mixins;

import net.mehvahdjukaar.moonlight.core.misc.ResourceLocationSanityCheck;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ResourceLocation.class)
public class ResourceLocationMixin {

    @Inject(method = "<init>(Ljava/lang/String;Ljava/lang/String;)V", at = @At("RETURN"))
    private void moonlight$validateValidNamespace(String namespace, String path, CallbackInfo ci) {
        ResourceLocationSanityCheck.validateNamespace(namespace, path);
    }
}

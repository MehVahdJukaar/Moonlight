package net.mehvahdjukaar.moonlight.core.mixins.fabric;

import net.mehvahdjukaar.moonlight.api.misc.RegistryAccessJsonReloadListener;
import net.mehvahdjukaar.moonlight.core.Moonlight;
import net.minecraft.core.RegistryAccess;
import net.minecraft.server.ReloadableServerRegistries;
import net.minecraft.server.ReloadableServerResources;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ReloadableServerResources.class)
public abstract class DataPackContentMixin {

    @Shadow public abstract ReloadableServerRegistries.Holder fullRegistries();

    @Inject(method = "updateRegistryTags()V", at = @At("TAIL"))
    private void onTagReload(CallbackInfo ci){
        Moonlight.afterDataReload(fullRegistries().get());
    }
}

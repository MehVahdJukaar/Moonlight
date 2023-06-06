package net.mehvahdjukaar.moonlight.core.mixins.fabric;

import net.mehvahdjukaar.moonlight.fabric.MoonlightFabricClient;
import net.minecraft.client.renderer.texture.TextureAtlas;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TextureAtlas.class)
public class TextureAtlasMixin {

    @Inject(method = "reload", at = @At("TAIL"))
    public void afterReload(TextureAtlas.Preparations preparations, CallbackInfo ci){
        MoonlightFabricClient.onAtlasReload();
    }
}

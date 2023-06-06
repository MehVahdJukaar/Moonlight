package net.mehvahdjukaar.moonlight.core.mixins.fabric;

import net.mehvahdjukaar.moonlight.core.MoonlightClient;
import net.mehvahdjukaar.moonlight.fabric.MoonlightFabricClient;
import net.minecraft.client.renderer.texture.SpriteLoader;
import net.minecraft.client.renderer.texture.TextureAtlas;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TextureAtlas.class)
public class TextureAtlasMixin {

    @Inject(method = "upload", at = @At("TAIL"))
    public void afterReload(SpriteLoader.Preparations preparations, CallbackInfo ci){
        MoonlightClient.afterTextureReload();
    }
}

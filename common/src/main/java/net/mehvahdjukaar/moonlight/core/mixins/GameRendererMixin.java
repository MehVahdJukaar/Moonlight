package net.mehvahdjukaar.moonlight.core.mixins;

import com.mojang.blaze3d.resource.CrossFrameResourcePool;
import net.mehvahdjukaar.moonlight.api.client.PostShadersHelper;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public class GameRendererMixin {

    @Shadow
    @Final
    private CrossFrameResourcePool resourcePool;

    // vanilla only runs one post effect, the extra grouped ones run right after it, still inside the
    // shouldRenderLevel branch
    @Inject(method = "render", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/util/profiling/ProfilerFiller;pop()V", ordinal = 0))
    private void moonlight$processExtraPostEffects(CallbackInfo ci) {
        PostShadersHelper.processScreenEffects(this.resourcePool);
    }

    // vanilla drops its effect here when the camera entity has none, drop the group too or it stays on forever
    @Inject(method = "checkEntityPostEffect", at = @At("HEAD"))
    private void moonlight$clearSpectatorGroup(@Nullable Entity cameraEntity, CallbackInfo ci) {
        PostShadersHelper.toggleEffect(null, PostShadersHelper.Group.SPECTATOR_SHADERS);
    }
}

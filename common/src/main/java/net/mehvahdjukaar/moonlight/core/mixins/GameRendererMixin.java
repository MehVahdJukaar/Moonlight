package net.mehvahdjukaar.moonlight.core.mixins;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.mehvahdjukaar.moonlight.api.client.PostShadersHelper;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.PostChain;
import net.minecraft.resources.ResourceLocation;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(GameRenderer.class)
public class GameRendererMixin {

    @WrapOperation(method = "checkEntityPostEffect", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/GameRenderer;loadEffect(Lnet/minecraft/resources/ResourceLocation;)V"))
    public void ml$setCorrectGroup(GameRenderer instance, ResourceLocation resourceLocation, Operation<Void> original) {
        PostShadersHelper.toggleEffect(resourceLocation, PostShadersHelper.Group.SPECTATOR_SHADERS);
    }

    @WrapOperation(
            method = "checkEntityPostEffect",
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/client/renderer/GameRenderer;postEffect:Lnet/minecraft/client/renderer/PostChain;",
                    opcode = Opcodes.PUTFIELD
            )
    )
    private void ml$preventClearingPost(GameRenderer instance, PostChain value, Operation<Void> original) {
        PostShadersHelper.toggleEffect(null, PostShadersHelper.Group.SPECTATOR_SHADERS);
    }
}

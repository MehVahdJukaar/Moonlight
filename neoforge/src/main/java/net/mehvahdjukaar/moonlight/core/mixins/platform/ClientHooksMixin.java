package net.mehvahdjukaar.moonlight.core.mixins.platform;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.mehvahdjukaar.moonlight.api.client.PostShadersHelper;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.client.ClientHooks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ClientHooks.class)
public class ClientHooksMixin {

    //route the spectator shader through the group system so it stacks with other effects
    @WrapOperation(method = "loadEntityShader", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/GameRenderer;setPostEffect(Lnet/minecraft/resources/Identifier;)V"))
    private static void ml$setCorrectGroup(GameRenderer instance, Identifier resourceLocation, Operation<Void> original) {
        instance.clearPostEffect();
        PostShadersHelper.toggleEffect(resourceLocation, PostShadersHelper.Group.SPECTATOR_SHADERS);
    }
}


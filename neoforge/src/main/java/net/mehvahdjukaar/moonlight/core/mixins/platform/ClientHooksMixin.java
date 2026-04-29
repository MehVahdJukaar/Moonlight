package net.mehvahdjukaar.moonlight.core.mixins.platform;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.mehvahdjukaar.moonlight.api.client.PostShadersHelper;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.ClientHooks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ClientHooks.class)
public class ClientHooksMixin {

    @WrapOperation(method = "loadEntityShader", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/GameRenderer;loadEffect(Lnet/minecraft/resources/ResourceLocation;)V"))
    private static void ml$setCorrectGroup(GameRenderer instance, ResourceLocation resourceLocation, Operation<Void> original) {
        PostShadersHelper.toggleEffect(resourceLocation, PostShadersHelper.Group.SPECTATOR_SHADERS);
    }
}


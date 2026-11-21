package net.mehvahdjukaar.moonlight.core.mixins.platform;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.mehvahdjukaar.moonlight.api.platform.platform.ClientHelperImpl;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.client.particle.ParticleGroup;
import net.minecraft.client.particle.ParticleRenderType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

@Mixin(ParticleEngine.class)
public class ParticleEngineMixin {

    @Inject(method = "createParticleGroup", at = @At("HEAD"), cancellable = true)
    private void moonlight$createModdedGroup(ParticleRenderType type, CallbackInfoReturnable<ParticleGroup<?>> cir) {
        Function<ParticleEngine, ParticleGroup<?>> factory = ClientHelperImpl.PARTICLE_GROUPS.get(type);
        if (factory != null) {
            cir.setReturnValue(factory.apply((ParticleEngine) (Object) this));
        }
    }


    //vanilla only extracts the groups in this list
    @ModifyExpressionValue(method = "extract", at = @At(value = "FIELD",
            target = "Lnet/minecraft/client/particle/ParticleEngine;RENDER_ORDER:Ljava/util/List;"))
    private List<ParticleRenderType> moonlight$addModdedGroups(List<ParticleRenderType> original) {
        if (ClientHelperImpl.PARTICLE_GROUPS_ORDER.isEmpty()) return original;

        List<ParticleRenderType> list = new ArrayList<>(original);
        list.addAll(ClientHelperImpl.PARTICLE_GROUPS_ORDER);
        return list;
    }
}
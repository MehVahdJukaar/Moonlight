package net.mehvahdjukaar.moonlight.core.mixins;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.mehvahdjukaar.moonlight.core.commands.BackCommand;
import net.minecraft.core.BlockPos;
import net.minecraft.server.commands.TeleportCommand;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Relative;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Set;

@Mixin(TeleportCommand.class)
public abstract class TeleportCommandMixin {

    @WrapOperation(method = "performTeleport", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;teleportTo(Lnet/minecraft/server/level/ServerLevel;DDDLjava/util/Set;FFZ)Z"))
    private static boolean ml$captureOldTpPos(Entity instance, ServerLevel level, double x, double y, double z, Set<Relative> relatives, float yRot, float xRot, boolean resetCamera, Operation<Boolean> original) {
        BlockPos oldPos = instance.blockPosition();
        var oldDim = instance.level().dimension();
        boolean result = original.call(instance, level, x, y, z, relatives, yRot, xRot, resetCamera);
        if (result) BackCommand.onTeleported(instance, oldPos, oldDim);
        return result;
    }
}

package net.mehvahdjukaar.moonlight.core.mixins;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.mehvahdjukaar.moonlight.api.platform.PlatHelper;
import net.mehvahdjukaar.moonlight.core.commands.BackCommand;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.portal.DimensionTransition;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;


@Mixin(ServerPlayer.class)
public class ServerPlayerMixin {

    //for some reason this takes ahuge amount of time when
    @WrapOperation(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerPlayer;adjustSpawnLocation(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/core/BlockPos;)Lnet/minecraft/core/BlockPos;"))
    private BlockPos ml$preventUselessCalculations(ServerPlayer instance, ServerLevel serverLevel, BlockPos blockPos, Operation<BlockPos> original) {
        if (PlatHelper.isFakePlayer(instance)) {
            return BlockPos.ZERO;
        }
        return original.call(instance, serverLevel, blockPos);
    }

    @Inject(method = "changeDimension", at = @At("HEAD"))
    private void ml$captureOldDimensionPos(DimensionTransition transition, CallbackInfoReturnable<Entity> cir) {
        ServerPlayer self = (ServerPlayer) (Object) this;
        if (self.isRemoved()) return;
        BackCommand.onTeleported(self, self.blockPosition(), self.level().dimension());
    }
}

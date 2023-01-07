package net.mehvahdjukaar.moonlight.core.mixins;

import net.mehvahdjukaar.moonlight.api.block.IPistonMotionReact;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.piston.PistonMovingBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PistonMovingBlockEntity.class)
public abstract class PistonBaseMixin extends BlockEntity {

    @Shadow
    private BlockState movedState;

    @Shadow
    private Direction direction;

    @Shadow
    private boolean extending;

    protected PistonBaseMixin(BlockEntityType<?> blockEntityType, BlockPos blockPos, BlockState blockState) {
        super(blockEntityType, blockPos, blockState);
    }

    @Inject(method = "finalTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;neighborChanged(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/Block;Lnet/minecraft/core/BlockPos;)V",
            shift = At.Shift.AFTER))
    public void onFinishedShortPulse(CallbackInfo ci) {
        if (this.movedState.getBlock() instanceof IPistonMotionReact pr) {
            pr.onMoved(this.movedState, this.level, this.worldPosition, this.direction, this.extending);
        }
    }

    @Inject(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;setBlock(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;I)Z",
            shift = At.Shift.AFTER), require = 2)
    private static void onFinishedMoving(Level level, BlockPos pos, BlockState state, PistonMovingBlockEntity blockEntity, CallbackInfo ci) {
        if (blockEntity.getMovedState().getBlock() instanceof IPistonMotionReact pr) {
            pr.onMoved(blockEntity.getMovedState(), level, blockEntity.getBlockPos(), blockEntity.getDirection(), blockEntity.isExtending());
        }
    }
}

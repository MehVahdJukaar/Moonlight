package net.mehvahdjukaar.moonlight.core.mixins.platform;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.mehvahdjukaar.moonlight.core.misc.StickyBlockHelper;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.piston.PistonStructureResolver;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(PistonStructureResolver.class)
public abstract class PistonStructureResolverMixin {

    private static final String CAN_STICK_TO = "Lnet/minecraft/world/level/block/state/BlockState;canStickTo(Lnet/minecraft/world/level/block/state/BlockState;)Z";

    @Shadow
    @Final
    private Direction pushDirection;

    @WrapOperation(method = {"resolve", "addBlockLine"},
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/level/block/state/BlockState;isStickyBlock()Z"))
    private boolean moonlight$stickyOnAnyFace(BlockState state, Operation<Boolean> original) {
        return StickyBlockHelper.isSticky(state, original.call(state));
    }

    @WrapOperation(method = "addBlockLine", at = @At(value = "INVOKE", target = CAN_STICK_TO, ordinal = 0))
    private boolean moonlight$stickAlongLine(BlockState state, BlockState behind, Operation<Boolean> original) {
        return StickyBlockHelper.canStickToEachOther(state, behind,
                this.pushDirection.getOpposite(), original.call(state, behind));
    }

    @WrapOperation(method = "addBlockLine", at = @At(value = "INVOKE", target = CAN_STICK_TO, ordinal = 1))
    private boolean moonlight$stickAlongLineBack(BlockState behind, BlockState state, Operation<Boolean> original) {
        return StickyBlockHelper.canStickToEachOther(behind, state,
                this.pushDirection, original.call(behind, state));
    }

    @WrapOperation(method = "addBranchingBlocks", at = @At(value = "INVOKE", target = CAN_STICK_TO, ordinal = 0))
    private boolean moonlight$stickSideways(BlockState neighbor, BlockState origin, Operation<Boolean> original,
                                            @Local Direction direction) {
        return StickyBlockHelper.canStickToEachOther(neighbor, origin,
                direction.getOpposite(), original.call(neighbor, origin));
    }

    @WrapOperation(method = "addBranchingBlocks", at = @At(value = "INVOKE", target = CAN_STICK_TO, ordinal = 1))
    private boolean moonlight$stickSidewaysBack(BlockState origin, BlockState neighbor, Operation<Boolean> original,
                                                @Local Direction direction) {
        return StickyBlockHelper.canStickToEachOther(origin, neighbor,
                direction, original.call(origin, neighbor));
    }
}

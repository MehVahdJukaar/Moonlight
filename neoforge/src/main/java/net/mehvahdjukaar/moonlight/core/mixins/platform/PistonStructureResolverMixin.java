package net.mehvahdjukaar.moonlight.core.mixins.platform;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.mehvahdjukaar.moonlight.core.misc.DirectionalStickyBlockHelper;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.piston.PistonStructureResolver;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(PistonStructureResolver.class)
public abstract class PistonStructureResolverMixin {

    @Shadow
    @Final
    private Direction pushDirection;

    @WrapOperation(method = {"resolve", "addBlockLine"},
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/level/block/state/BlockState;isStickyBlock()Z"))
    private boolean moonlight$stickyOnAnyFace(BlockState state, Operation<Boolean> original) {
        return DirectionalStickyBlockHelper.isSticky(state, original.call(state));
    }

    @WrapOperation(method = "addBlockLine", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;canStickTo(Lnet/minecraft/world/level/block/state/BlockState;)Z", ordinal = 0))
    private boolean moonlight$stickAlongLine(BlockState state, BlockState behind, Operation<Boolean> original) {
        return DirectionalStickyBlockHelper.canStickToEachOther(state, behind,
                this.pushDirection.getOpposite(), original.call(state, behind));
    }

    @WrapOperation(method = "addBlockLine", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;canStickTo(Lnet/minecraft/world/level/block/state/BlockState;)Z", ordinal = 1))
    private boolean moonlight$stickAlongLineBack(BlockState behind, BlockState state, Operation<Boolean> original) {
        return DirectionalStickyBlockHelper.canStickToEachOther(behind, state,
                this.pushDirection, original.call(behind, state));
    }

    @WrapOperation(method = "addBranchingBlocks", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;canStickTo(Lnet/minecraft/world/level/block/state/BlockState;)Z", ordinal = 0))
    private boolean moonlight$stickSideways(BlockState neighbor, BlockState origin, Operation<Boolean> original,
                                            @Local Direction direction) {
        return DirectionalStickyBlockHelper.canStickToEachOther(neighbor, origin,
                direction.getOpposite(), original.call(neighbor, origin));
    }

    @WrapOperation(method = "addBranchingBlocks", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;canStickTo(Lnet/minecraft/world/level/block/state/BlockState;)Z", ordinal = 1))
    private boolean moonlight$stickSidewaysBack(BlockState origin, BlockState neighbor, Operation<Boolean> original,
                                                @Local Direction direction) {
        return DirectionalStickyBlockHelper.canStickToEachOther(origin, neighbor,
                direction, original.call(origin, neighbor));
    }
}

package net.mehvahdjukaar.moonlight.core.mixins.platform;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.mehvahdjukaar.moonlight.api.events.IFireConsumeBlockEvent;
import net.mehvahdjukaar.moonlight.api.events.MoonlightEventsHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.FireBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;


@Mixin(FireBlock.class)
public abstract class FireBlockMixin extends BaseFireBlock {

    protected FireBlockMixin(Properties settings, float damage) {
        super(settings, damage);
    }


    @WrapOperation(method = "checkBurnOut",
            at = @At(value = "INVOKE",
                    target = "net/minecraft/world/level/Level.removeBlock (Lnet/minecraft/core/BlockPos;Z)Z"))
    private boolean ml$afterRemoveBlock(Level level, BlockPos pos, boolean isMoving, Operation<Boolean> original,
                                     @Local BlockState oldState, @Local(ordinal = 0, argsOnly = true) int chance,
                                     @Local(ordinal = 1, argsOnly = true) int age) {
        var event = IFireConsumeBlockEvent.create(pos, level, oldState, chance, age,
                Direction.DOWN, false);
        MoonlightEventsHelper.postEvent(event, IFireConsumeBlockEvent.class);
        BlockState newState = event.getFinalState();
        if (newState != null) return level.setBlockAndUpdate(pos, newState);
        else return original.call(level, pos, isMoving);
    }

    @WrapOperation(method = "checkBurnOut",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/level/Level;setBlock(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;I)Z"))
    private boolean ml$afterFireReplace(Level level, BlockPos p, BlockState newFireState, int flags,
                                        Operation<Boolean> operation, @Local BlockState oldState,
                                        @Local(ordinal = 0, argsOnly = true) int chance,
                                        @Local(ordinal = 1, argsOnly = true) int age) {
        var event = IFireConsumeBlockEvent.create(p, level, oldState, chance, age,
                Direction.DOWN, true);
        MoonlightEventsHelper.postEvent(event, IFireConsumeBlockEvent.class);
        BlockState newState = event.getFinalState();
        if (newState != null) return level.setBlockAndUpdate(p, newState);
        else return  operation.call(level, p, newFireState, flags);
    }

}
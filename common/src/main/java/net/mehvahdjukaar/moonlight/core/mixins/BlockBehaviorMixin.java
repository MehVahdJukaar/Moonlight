package net.mehvahdjukaar.moonlight.core.mixins;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BlockBehaviour.class)
public class BlockBehaviorMixin {


    @Inject(method = "onRemove", at = @At("TAIL"))
    private void moonlight$cleanupOptional(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston, CallbackInfo ci) {
        //handles same block case
        //needed because vanila while allowing it nicely, doesnt totally work well with states that have optional BE
        if (state.is(newState.getBlock()) && !newState.hasBlockEntity() && state.hasBlockEntity()) {
            level.removeBlockEntity(pos); //also needed here because previous check doesn't clear if its the same block
        }
    }
}

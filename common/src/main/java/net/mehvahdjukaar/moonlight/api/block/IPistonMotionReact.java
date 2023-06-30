package net.mehvahdjukaar.moonlight.api.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.piston.PistonMovingBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

/**
 * Callback for piston movement reaction on blocks
 */
public interface IPistonMotionReact {

    /**
     * Called on movement finished
     */
    default void onMoved(BlockState movedState, Level level, BlockPos pos, Direction direction, boolean extending,  PistonMovingBlockEntity tile) {
    }

    default boolean ticksWhileMoved() {
        return false;
    }

    /**
     * Called while moving
     */
    default void moveTick(BlockState movedState, Level level, BlockPos pos, AABB aabb, PistonMovingBlockEntity tile) {

    }

    /**
     * Quark method for magnet callback
     */
    default void onMagnetMoved(Level level, BlockPos blockPos, Direction direction, BlockState blockState, BlockEntity blockEntity) {
    }
}

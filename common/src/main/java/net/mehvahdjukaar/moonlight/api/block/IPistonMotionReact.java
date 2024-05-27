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
    @Deprecated(forRemoval = true)
    default void onMoved(BlockState movedState, Level level, BlockPos pos, Direction direction, boolean extending,  PistonMovingBlockEntity tile) {
    }

    default void onMoved(Level level, BlockPos pos, BlockState movedState,  Direction direction, boolean extending) {
        this.onMoved(movedState, level, pos, direction, extending, null);
    }

    default boolean ticksWhileMoved() {
        return false;
    }

    /**
     * Called while moving
     */
    @Deprecated
    default void moveTick(BlockState movedState, Level level, BlockPos pos, AABB aabb, PistonMovingBlockEntity tile) {

    }

    default void moveTick(Level level, BlockPos pos, BlockState movedState, AABB aabb, PistonMovingBlockEntity tile) {
        this.moveTick(movedState, level, pos, aabb, tile);
    }

    /**
     * Quark method for magnet callback
     */
    default void onMagnetMoved(Level level, BlockPos blockPos, Direction direction, BlockState blockState, BlockEntity blockEntity) {
    }
}

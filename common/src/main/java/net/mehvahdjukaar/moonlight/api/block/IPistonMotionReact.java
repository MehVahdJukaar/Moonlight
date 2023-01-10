package net.mehvahdjukaar.moonlight.api.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
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
        onMoved(movedState, level, pos, direction,extending);
    }

    @Deprecated(forRemoval = true)
    default void onMoved(BlockState movedState, Level level, BlockPos pos, Direction direction, boolean extending) {

    }

    default boolean ticksWhileMoved() {
        return false;
    }

    /**
     * Called while moving
     */
    @Deprecated(forRemoval = true)
    default void moveTick(BlockState movedState, Level level, BlockPos pos, Direction dir, AABB aabb, PistonMovingBlockEntity tile) {
    }

    default void moveTick(BlockState movedState, Level level, BlockPos pos, AABB aabb, PistonMovingBlockEntity tile) {
        moveTick(movedState, level, pos, tile.getMovementDirection(), aabb, tile);
    }
}

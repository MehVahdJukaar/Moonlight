package net.mehvahdjukaar.moonlight.api.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Callback for piston movement reaction on blocks
 */
public interface IPistonMotionReact {

    void onMoved(BlockState movedState, Level level, BlockPos pos, Direction direction, boolean extending);
}

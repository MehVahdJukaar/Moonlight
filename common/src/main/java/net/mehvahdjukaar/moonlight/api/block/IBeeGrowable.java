package net.mehvahdjukaar.moonlight.api.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

/**
 * For blocks that have a special behavior when grown by bees
 */
public interface IBeeGrowable {

    boolean getPollinated(Level level, BlockPos pos, BlockState state);
}

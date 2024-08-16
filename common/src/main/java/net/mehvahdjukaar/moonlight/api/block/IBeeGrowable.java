package net.mehvahdjukaar.moonlight.api.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockState;

/**
 * For blocks that have a special behavior when grown by bees.
 * Mostly used for double crops
 */
public interface IBeeGrowable {

    boolean getPollinated(Level level, BlockPos pos, BlockState state);

    /**
     * Use for double crops that need their upper block grown to be considered fully grown
     */
    default boolean isPlantFullyGrown(BlockState state, BlockPos pos, Level level) {
        if (state.getBlock() instanceof CropBlock cb) {
            return cb.isMaxAge(state);
        }
        return false;
    }

}

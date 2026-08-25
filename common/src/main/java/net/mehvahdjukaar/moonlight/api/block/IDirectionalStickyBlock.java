package net.mehvahdjukaar.moonlight.api.block;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;

/**
 * A block that drags neighbors along with pistons like a slime block, but only on the faces it chooses.
 * Both blocks of a pair must agree before a piston keeps them together.
 */
public interface IDirectionalStickyBlock {

    /**
     * @param face     side of this block that touches the neighbor
     * @param neighbor state sitting on that side
     */
    boolean canStickTo(BlockState state, Direction face, BlockState neighbor);

    /**
     * Piston code asks this before looking at the six sides. Must not return false
     * for a state where canStickTo can still return true.
     */
    default boolean isEverSticky(BlockState state) {
        return true;
    }
}

package net.mehvahdjukaar.moonlight.core.misc;

import net.mehvahdjukaar.moonlight.api.block.IDirectionalStickyBlock;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;

public class DirectionalStickyBlockHelper {

    public static boolean isSticky(BlockState state, boolean vanilla) {
        if (state.getBlock() instanceof IDirectionalStickyBlock sticky) {
            return sticky.isEverSticky(state);
        }
        return vanilla;
    }

    public static boolean canStickToEachOther(BlockState first, BlockState second, Direction firstToSecond, boolean vanilla) {
        boolean anyDirectional = false;
        if (first.getBlock() instanceof IDirectionalStickyBlock sticky) {
            if (!sticky.canStickTo(first, firstToSecond, second)) return false;
            anyDirectional = true;
        }
        if (second.getBlock() instanceof IDirectionalStickyBlock sticky) {
            if (!sticky.canStickTo(second, firstToSecond.getOpposite(), first)) return false;
            anyDirectional = true;
        }
        return anyDirectional || vanilla;
    }
}

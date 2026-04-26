package net.mehvahdjukaar.moonlight.api.fluids;

import net.mehvahdjukaar.candlelight.api.PlatformImpl;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

public class FluidsHelper {

    @Contract
    @PlatformImpl
    public static boolean extractFluidFromTank(BlockEntity tileBack, Direction dir, int amount) {
        throw new AssertionError();
    }

    @PlatformImpl
    public static Integer fillFluidTank(BlockEntity tileBelow, FluidOffer offer, Direction dir) {
        throw new AssertionError();
    }

    @Contract
    @PlatformImpl
    public static boolean hasFluidHandler(Level level, BlockPos pos, Direction dir) {
        throw new AssertionError();
    }

    @Nullable
    @Contract
    @PlatformImpl
    public static FluidOffer getFluidInTank(Level level, BlockPos pos, Direction dir, BlockEntity source) {
        throw new AssertionError();
    }
}

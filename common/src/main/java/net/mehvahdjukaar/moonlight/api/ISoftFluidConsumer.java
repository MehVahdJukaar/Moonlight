package net.mehvahdjukaar.moonlight.api;

import net.mehvahdjukaar.moonlight.fluids.SoftFluid;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;

/**
 * implement in a block that can consume a soft fluid (not a tank)
 * prevents any other further interaction it this block has a fluid tank
 */
public interface ISoftFluidConsumer {

    boolean tryAcceptingFluid(Level world, BlockState state, BlockPos pos, SoftFluid f, @Nullable CompoundTag nbt, int amount);

}

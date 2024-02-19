package net.mehvahdjukaar.moonlight.api.block;

import net.mehvahdjukaar.moonlight.api.fluids.SoftFluid;
import net.mehvahdjukaar.moonlight.api.fluids.SoftFluidStack;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import org.jetbrains.annotations.Nullable;

/**
 * implement in a block that can consume a soft fluid (not a tank)
 * prevents any other further interaction it this block has a fluid tank
 */
public interface ISoftFluidConsumer {

    boolean tryAcceptingFluid(Level world, BlockState state, BlockPos pos, SoftFluidStack stack);

}

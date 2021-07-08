package net.mehvahdjukaar.selene.fluids;

import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nullable;

/**
 * implement in a block that can consume a soft fluid
 * prevents any other further interaction it this block has a fluid tank
 */
public interface ISoftFluidConsumer {

    boolean tryAcceptingFluid(World world, BlockState state, BlockPos pos, SoftFluid f, @Nullable CompoundNBT nbt, int amount);

}

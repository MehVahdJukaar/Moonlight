package net.mehvahdjukaar.selene.fluids;

import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.apache.commons.lang3.tuple.Pair;

/**
 * implement in a simple block that can provide a soft fluid
 * prevents any other further interaction it this block has a fluid tank
 */
public interface ISoftFluidProvider {

    Pair<SoftFluid, CompoundNBT> getProvidedFluid(World world, BlockState state, BlockPos pos);

    void consumeProvidedFluid(World world, BlockState state, BlockPos pos, SoftFluid f, CompoundNBT nbt, int amount);

}

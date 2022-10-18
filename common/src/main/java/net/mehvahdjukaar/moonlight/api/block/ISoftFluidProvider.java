package net.mehvahdjukaar.moonlight.api.block;

import com.mojang.datafixers.util.Pair;
import net.mehvahdjukaar.moonlight.api.fluids.SoftFluid;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

/**
 * implement in a simple block that can provide a soft fluid (not a tank)
 * prevents any other further interaction it this block has a fluid tank
 */
public interface ISoftFluidProvider {

    Pair<SoftFluid, CompoundTag> getProvidedFluid(Level world, BlockState state, BlockPos pos);

    void consumeProvidedFluid(Level world, BlockState state, BlockPos pos, SoftFluid f, CompoundTag nbt, int amount);

}

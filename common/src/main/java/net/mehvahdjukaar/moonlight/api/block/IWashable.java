package net.mehvahdjukaar.moonlight.api.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public interface IWashable {

    boolean tryWash(Level level, BlockPos pos, BlockState state, Vec3 hitPos);
}

package net.mehvahdjukaar.moonlight.core.misc;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

public interface IExtendedPistonTile {

    void tickMovedBlock(Level level, BlockPos pos);
}

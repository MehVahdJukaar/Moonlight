package net.mehvahdjukaar.moonlight.api.entity;

import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.Nullable;

public interface ITileEntityCarry {

    @Nullable
    BlockEntity getCarriedTileEntity();
}

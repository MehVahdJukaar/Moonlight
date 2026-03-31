package net.mehvahdjukaar.moonlight.api.block;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.Nullable;

public interface IEntityCarry {

    @Nullable
    Entity getCarriedEntity();
}

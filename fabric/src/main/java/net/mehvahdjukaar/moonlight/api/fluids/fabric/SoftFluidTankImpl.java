package net.mehvahdjukaar.moonlight.api.fluids.fabric;

import net.mehvahdjukaar.moonlight.api.fluids.SoftFluid;
import net.mehvahdjukaar.moonlight.api.fluids.SoftFluidTank;
import net.mehvahdjukaar.moonlight.api.fluids.BuiltInSoftFluids;
import net.mehvahdjukaar.moonlight.api.util.PotionNBTHelper;
import net.mehvahdjukaar.moonlight.api.fluids.SoftFluidColors;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;

import org.jetbrains.annotations.Nullable;

/**
 * instance this fluid tank in your tile entity
 */
@SuppressWarnings("unused")
@Deprecated(forRemoval = true)
public class SoftFluidTankImpl extends SoftFluidTank {

    public static SoftFluidTank create(int capacity) {
        return new SoftFluidTankImpl(capacity);
    }

    protected SoftFluidTankImpl(int capacity) {
        super(capacity);
    }

}

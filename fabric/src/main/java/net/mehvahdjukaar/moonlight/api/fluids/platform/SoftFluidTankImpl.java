package net.mehvahdjukaar.moonlight.api.fluids.platform;

import net.mehvahdjukaar.moonlight.api.fluids.SoftFluid;
import net.mehvahdjukaar.moonlight.api.fluids.SoftFluidTank;
import net.minecraft.core.HolderGetter;

/**
 * instance this fluid tank in your tile entity
 */
public class SoftFluidTankImpl extends SoftFluidTank {

    protected SoftFluidTankImpl(int capacity, HolderGetter<SoftFluid> registries) {
        super(capacity, registries);
    }

    public static SoftFluidTank create(int capacity, HolderGetter<SoftFluid> registries) {
        return new SoftFluidTankImpl(capacity, registries);
    }

}

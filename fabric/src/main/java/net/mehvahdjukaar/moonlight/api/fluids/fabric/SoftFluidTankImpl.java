package net.mehvahdjukaar.moonlight.api.fluids.fabric;

import net.mehvahdjukaar.moonlight.api.fluids.SoftFluidTank;

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

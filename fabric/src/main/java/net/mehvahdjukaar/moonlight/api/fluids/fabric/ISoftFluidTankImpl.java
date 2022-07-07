package net.mehvahdjukaar.moonlight.api.fluids.fabric;

import net.mehvahdjukaar.moonlight.api.fluids.ISoftFluidTank;

public class ISoftFluidTankImpl {
    public static ISoftFluidTank create(int capacity) {
        return new SoftFluidTank(capacity);
    }
}

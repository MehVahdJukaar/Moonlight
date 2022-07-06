package net.mehvahdjukaar.moonlight.fluids.fabric;

import net.mehvahdjukaar.moonlight.fluids.ISoftFluidTank;

public class ISoftFluidTankImpl {
    public static ISoftFluidTank create(int capacity) {
        return new SoftFluidTank(capacity);
    }
}

package net.mehvahdjukaar.moonlight.api.fluids.forge;

import net.mehvahdjukaar.moonlight.api.fluids.ISoftFluidTank;

public class ISoftFluidTankImpl {
    public static ISoftFluidTank create(int capacity) {
        return new SoftFluidTank(capacity);
    }
}

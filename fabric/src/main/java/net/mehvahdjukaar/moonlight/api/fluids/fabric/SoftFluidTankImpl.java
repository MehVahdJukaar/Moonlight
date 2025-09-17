package net.mehvahdjukaar.moonlight.api.fluids.fabric;

import net.mehvahdjukaar.moonlight.api.fluids.SoftFluidTank;
import net.mehvahdjukaar.moonlight.api.util.Utils;
import net.minecraft.core.HolderLookup;

/**
 * instance this fluid tank in your tile entity
 */
@SuppressWarnings("unused")
@Deprecated(forRemoval = true)
public class SoftFluidTankImpl extends SoftFluidTank {


    @Deprecated(forRemoval = true)
    protected SoftFluidTankImpl(int capacity) {
        super(capacity, Utils.hackyGetRegistryAccess());
    }

    protected SoftFluidTankImpl(int capacity, HolderLookup.Provider registries) {
        super(capacity, registries);
    }

    public static SoftFluidTank create(int capacity, HolderLookup.Provider registries) {
        return new SoftFluidTankImpl(capacity, registries);
    }

}

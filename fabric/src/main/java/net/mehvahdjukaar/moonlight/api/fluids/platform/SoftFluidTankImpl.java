package net.mehvahdjukaar.moonlight.api.fluids.platform;

import net.mehvahdjukaar.moonlight.api.fluids.SoftFluid;
import net.mehvahdjukaar.moonlight.api.fluids.SoftFluidRegistry;
import net.mehvahdjukaar.moonlight.api.fluids.SoftFluidTank;
import net.mehvahdjukaar.moonlight.api.util.Utils;
import net.minecraft.core.HolderGetter;

/**
 * instance this fluid tank in your tile entity
 */
@SuppressWarnings("unused")
@Deprecated(forRemoval = true)
public class SoftFluidTankImpl extends SoftFluidTank {


    @Deprecated(forRemoval = true)
    protected SoftFluidTankImpl(int capacity) {
        this(capacity, SoftFluidRegistry.get(Utils.hackyGetRegistryAccess()).asLookup());
    }

    protected SoftFluidTankImpl(int capacity, HolderGetter<SoftFluid> registries) {
        super(capacity, registries);
    }

    public static SoftFluidTank create(int capacity, HolderGetter<SoftFluid> registries) {
        return new SoftFluidTankImpl(capacity, registries);
    }

}

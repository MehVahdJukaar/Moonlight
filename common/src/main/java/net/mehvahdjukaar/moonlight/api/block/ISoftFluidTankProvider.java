package net.mehvahdjukaar.moonlight.api.block;

import net.mehvahdjukaar.moonlight.api.fluids.SoftFluidTank;

/**
 * implement this in your tile entities.
 */
public interface ISoftFluidTankProvider {

    SoftFluidTank getSoftFluidTank();

    default boolean canInteractWithSoftFluidTank(){
        return true;
    }
}

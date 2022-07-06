package net.mehvahdjukaar.moonlight.fluids;

import net.mehvahdjukaar.moonlight.fluids.ISoftFluidTank;

/**
 * implement this in your tile entities.
 */
public interface ISoftFluidTankProvider {

    ISoftFluidTank getSoftFluidTank();

    default boolean canInteractWithSoftFluidTank(){
        return true;
    }
}

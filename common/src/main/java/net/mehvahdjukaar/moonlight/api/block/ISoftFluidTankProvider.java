package net.mehvahdjukaar.moonlight.api.block;

import net.mehvahdjukaar.moonlight.api.fluids.ISoftFluidTank;

/**
 * implement this in your tile entities.
 */
public interface ISoftFluidTankProvider {

    ISoftFluidTank getSoftFluidTank();

    default boolean canInteractWithSoftFluidTank(){
        return true;
    }
}

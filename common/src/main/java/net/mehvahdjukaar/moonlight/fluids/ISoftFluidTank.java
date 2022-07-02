package net.mehvahdjukaar.moonlight.fluids;

/**
 * implement this in your tile entities.
 */
public interface ISoftFluidTank {

    SoftFluidTank getSoftFluidTank();

    default boolean canInteractWithSoftFluidTank(){
        return true;
    }
}

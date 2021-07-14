package net.mehvahdjukaar.selene.fluids;

/**
 * implement this in your tile entities.
 */
//idk how to forge cap xD
public interface ISoftFluidHolder {
    SoftFluidHolder getSoftFluidHolder();

    default boolean canInteractWithFluidHolder(){
        return true;
    }
}

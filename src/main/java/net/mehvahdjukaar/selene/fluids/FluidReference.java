package net.mehvahdjukaar.selene.fluids;

import net.minecraft.resources.ResourceLocation;

import java.util.function.Supplier;

/**
 * Use these to store fluids instances. They are similar to vanilla holders since they need to refresh after data is reloaded
 */
//I wish I could use vanilla holders here
//TODO: 1.19: use vanilla registry and holders
    @Deprecated
public class FluidReference implements Supplier<SoftFluid> {

    public static FluidReference of(SoftFluid instance) {
        return new FluidReference(instance.getRegistryName());
    }

    public static FluidReference of(String name) {
        return new FluidReference(new ResourceLocation(name));
    }

    public static FluidReference of(ResourceLocation name) {
        return new FluidReference(name);
    }

    private final Object lock = new Object();
    private SoftFluid value;
    private final ResourceLocation id;
    private int reloadNumber = -1;

    private FluidReference(ResourceLocation id) {
        this.value = null;
        this.id = id;
    }

    @Override
    public SoftFluid get() {
        int n = SoftFluidRegistryOld.INSTANCE.currentReload;
        if (n == 0) return SoftFluidRegistry.EMPTY; //if registry hasn't been initialized yet
        synchronized (lock) {
            if (n != this.reloadNumber) {
                this.reloadNumber = SoftFluidRegistryOld.INSTANCE.currentReload;
                this.value = SoftFluidRegistryOld.get(id);
                if (this.value.isEmpty()) {
                    //client might call this during server reload. remeber on single player this is the only object for both "sides"
                    //we return empty so even client during reload can be fine
                    //throw new UnsupportedOperationException("Soft Fluid not present: " + this.id);
                }
            }
            return value;
        }
    }

    public ResourceLocation getId() {
        return id;
    }

    public boolean is(SoftFluid softFluid) {
        return this.get() == softFluid;
    }

    public boolean is(FluidReference softFluid) {
        return this.get() == softFluid.get();
        //this.id == softFluid.id;
    }
}

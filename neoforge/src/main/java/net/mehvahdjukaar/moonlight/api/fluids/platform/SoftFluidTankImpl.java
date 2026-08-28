package net.mehvahdjukaar.moonlight.api.fluids.platform;

import net.mehvahdjukaar.moonlight.api.fluids.SoftFluid;
import net.mehvahdjukaar.moonlight.api.fluids.SoftFluidTank;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderLookup;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;

import static net.mehvahdjukaar.moonlight.api.fluids.platform.SoftFluidStackImpl.bottlesToMB;

/**
 * instance this fluid tank in your tile entity
 */
@Deprecated() //move forge specific to a helper class //forgot why
public class SoftFluidTankImpl extends SoftFluidTank {

    public static SoftFluidTank create(int capacity, HolderGetter<SoftFluid> registries) {
        return new SoftFluidTankImpl(capacity, registries);
    }

    protected SoftFluidTankImpl(int capacity, HolderGetter<SoftFluid> registries) {
        super(capacity, registries);
    }

    /**
     * pours n bottle of my content into said fluid handler
     *
     * @param fluidDestination fluid handler to fill
     * @param bottles          number of bottles to empty (1blt = 250mb)
     * @return success
     */
    public boolean transferToFluidTank(ResourceHandler<FluidResource> fluidDestination, int bottles) {
        if (this.isEmpty() || this.getFluidCount() < bottles) return false;
        FluidStack stack = ((SoftFluidStackImpl) this.fluidStack).toForgeFluid();
        if (stack.isEmpty()) return false;
        int milliBuckets = stack.getAmount();
        try (Transaction t = Transaction.openRoot()) {
            if (fluidDestination.insert(FluidResource.of(stack), milliBuckets, t) != milliBuckets) return false;
            t.commit();
        }
        this.fluidStack.shrink(bottles);
        return true;
    }

    public boolean transferToFluidTank(ResourceHandler<FluidResource> fluidDestination) {
        return this.transferToFluidTank(fluidDestination, BOTTLE_COUNT);
    }

    //drains said fluid handler of 250mb (1 bottle) of fluid
    public boolean drainFluidTank(ResourceHandler<FluidResource> fluidSource, int bottles, HolderLookup.Provider ra) {
        if (this.getSpace() < bottles) return false;
        int milliBuckets = bottlesToMB(bottles);
        for (int i = 0; i < fluidSource.size(); i++) {
            FluidResource resource = fluidSource.getResource(i);
            if (resource.isEmpty()) continue;
            FluidStack drainable = resource.toStack(milliBuckets);
            boolean transfer;
            if (this.fluidStack.isEmpty()) {
                transfer = true;
            } else {
                transfer = ((SoftFluidStackImpl) fluidStack).isFluidEqual(drainable, ra);
            }
            if (!transfer) continue;
            try (Transaction t = Transaction.openRoot()) {
                if (fluidSource.extract(i, resource, milliBuckets, t) != milliBuckets) continue;
                t.commit();
            }
            if (this.fluidStack.isEmpty()) this.setFluid(drainable, ra);
            else this.fluidStack.grow(bottles);
            return true;
        }
        return false;
    }

    public boolean drainFluidTank(ResourceHandler<FluidResource> fluidSource, HolderLookup.Provider ra) {
        return this.drainFluidTank(fluidSource, BOTTLE_COUNT, ra);
    }

    /**
     * copies the content of a fluid handler's first tank into this
     */
    public void copy(ResourceHandler<FluidResource> other, HolderLookup.Provider ra) {
        FluidStack fluid = other.getResource(0).toStack(other.getAmountAsInt(0));
        this.setFluid(fluid, ra);
        this.capCapacity();
    }

    /**
     * sets current fluid to provided vanilla fluid equivalent
     *
     * @param fluidStack fluid stack
     */
    public void setFluid(FluidStack fluidStack, HolderLookup.Provider ra) {
        this.setFluid(SoftFluidStackImpl.fromForgeFluid(fluidStack, ra));
    }


}

package net.mehvahdjukaar.moonlight.api.fluids.neoforge;

import net.mehvahdjukaar.moonlight.api.fluids.SoftFluid;
import net.mehvahdjukaar.moonlight.api.fluids.SoftFluidRegistry;
import net.mehvahdjukaar.moonlight.api.fluids.SoftFluidTank;
import net.mehvahdjukaar.moonlight.api.util.Utils;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderLookup;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;

import static net.mehvahdjukaar.moonlight.api.fluids.neoforge.SoftFluidStackImpl.bottlesToMB;

/**
 * instance this fluid tank in your tile entity
 */
@Deprecated() //move forge specific to a helper class //forgot why
public class SoftFluidTankImpl extends SoftFluidTank {

    public static SoftFluidTank create(int capacity, HolderGetter<SoftFluid> registries) {
        return new SoftFluidTankImpl(capacity, registries);
    }

    @Deprecated(forRemoval = true)
    protected SoftFluidTankImpl(int capacity) {
        super(capacity, SoftFluidRegistry.get(Utils.hackyGetRegistryAccess()).asLookup());
    }

    protected SoftFluidTankImpl(int capacity, HolderGetter<SoftFluid> registries) {
        super(capacity, registries);
    }

    /**
     * try adding provided forge fluid to the tank
     *
     * @param fluidStack forge fluid stack
     * @return success
     */
    @Deprecated(forRemoval = true)
    public boolean addVanillaFluid(FluidStack fluidStack) {
        var s = SoftFluidStackImpl.fromForgeFluid(fluidStack, Utils.hackyGetRegistryAccess());
        if (s.isEmpty()) return false;
        return addFluid(s, false) == s.getCount();
    }

    /**
     * pours n bottle of my content into said forge fluid tank
     *
     * @param fluidDestination forge fluid tank handler
     * @param bottles          number of bottles to empty (1blt = 250mb)
     * @return success
     */
    public boolean transferToFluidTank(IFluidHandler fluidDestination, int bottles) {
        if (this.isEmpty() || this.getFluidCount() < bottles) return false;
        FluidStack stack = ((SoftFluidStackImpl) this.fluidStack).toForgeFluid();
        int milliBuckets = stack.getAmount();
        if (!stack.isEmpty()) {
            int fillableAmount = fluidDestination.fill(stack, IFluidHandler.FluidAction.SIMULATE);
            if (fillableAmount == milliBuckets) {
                fluidDestination.fill(stack, IFluidHandler.FluidAction.EXECUTE);
                this.fluidStack.shrink(bottles);
                return true;
            }
        }
        return false;
    }

    public boolean transferToFluidTank(IFluidHandler fluidDestination) {
        return this.transferToFluidTank(fluidDestination, BOTTLE_COUNT);
    }

    @Deprecated(forRemoval = true)
    public boolean drainFluidTank(IFluidHandler fluidSource, int bottles) {
        return drainFluidTank(fluidSource,bottles, Utils.hackyGetRegistryAccess());
    }

    //drains said fluid tank of 250mb (1 bottle) of fluid
    public boolean drainFluidTank(IFluidHandler fluidSource, int bottles, HolderLookup.Provider ra  ) {
        if (this.getSpace() < bottles) return false;
        int milliBuckets = bottlesToMB(bottles);
        FluidStack drainable = fluidSource.drain(milliBuckets, IFluidHandler.FluidAction.SIMULATE);
        if (!drainable.isEmpty() && drainable.getAmount() == milliBuckets) {
            boolean transfer = false;
            if (this.fluidStack.isEmpty()) {
                this.setFluid(drainable, ra);
                transfer = true;
            } else if (((SoftFluidStackImpl) fluidStack).isFluidEqual(drainable, ra)) {
                transfer = true;
            }
            if (transfer) {
                fluidSource.drain(milliBuckets, IFluidHandler.FluidAction.EXECUTE);
                return true;
            }
        }
        return false;
    }

    public boolean drainFluidTank(IFluidHandler fluidSource, HolderLookup.Provider ra) {
        return this.drainFluidTank(fluidSource, BOTTLE_COUNT, ra);
    }

    @Deprecated(forRemoval = true)
    public boolean drainFluidTank(IFluidHandler fluidSource) {
        return this.drainFluidTank(fluidSource, BOTTLE_COUNT, Utils.hackyGetRegistryAccess());
    }

    /**
     * copies the content of a fluid tank into this
     *
     * @param other forge fluid tank
     */
    public void copy(IFluidHandler other, HolderLookup.Provider ra) {
        FluidStack forgeFluid = other.getFluidInTank(0).copy();// 250, IFluidHandler.FluidAction.SIMULATE);
        this.setFluid(forgeFluid, ra);
        this.capCapacity();
    }

    @Deprecated(forRemoval = true)
    public void copy(IFluidHandler other) {
        this.copy(other, Utils.hackyGetRegistryAccess());
    }


    @Deprecated(forRemoval = true)
    public void setFluid(FluidStack fluidStack) {
        this.setFluid(SoftFluidStackImpl.fromForgeFluid(fluidStack, Utils.hackyGetRegistryAccess()));
    }

    /**
     * sets current fluid to provided forge fluid equivalent
     *
     * @param fluidStack forge fluid
     */
    public void setFluid(FluidStack fluidStack, HolderLookup.Provider ra) {
        this.setFluid(SoftFluidStackImpl.fromForgeFluid(fluidStack, ra));
    }



}

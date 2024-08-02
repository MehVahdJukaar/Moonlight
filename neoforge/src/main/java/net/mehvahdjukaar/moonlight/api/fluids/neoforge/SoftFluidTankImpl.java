package net.mehvahdjukaar.moonlight.api.fluids.neoforge;

import net.mehvahdjukaar.moonlight.api.fluids.SoftFluidTank;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;

import static net.mehvahdjukaar.moonlight.api.fluids.neoforge.SoftFluidStackImpl.bottlesToMB;

/**
 * instance this fluid tank in your tile entity
 */
@Deprecated() //forgot why
public class SoftFluidTankImpl extends SoftFluidTank {

    public static SoftFluidTank create(int capacity) {
        return new SoftFluidTankImpl(capacity);
    }

    protected SoftFluidTankImpl(int capacity) {
        super(capacity);
    }

    /**
     * try adding provided forge fluid to the tank
     *
     * @param fluidStack forge fluid stack
     * @return success
     */
    @Deprecated(forRemoval = true)
    public boolean addVanillaFluid(FluidStack fluidStack) {
        var s = SoftFluidStackImpl.fromForgeFluid(fluidStack);
        if (s.isEmpty()) return false;
        return addFluid(s, false) == s.getCount();
    }

    //TODO: re check all the ones below here. I blindly ported

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

    //drains said fluid tank of 250mb (1 bottle) of fluid
    public boolean drainFluidTank(IFluidHandler fluidSource, int bottles) {
        if (this.getSpace() < bottles) return false;
        int milliBuckets = bottlesToMB(bottles);
        FluidStack drainable = fluidSource.drain(milliBuckets, IFluidHandler.FluidAction.SIMULATE);
        if (!drainable.isEmpty() && drainable.getAmount() == milliBuckets) {
            boolean transfer = false;
            if (this.fluidStack.isEmpty()) {
                this.setFluid(drainable);
                transfer = true;
            } else if (((SoftFluidStackImpl)fluidStack).isFluidEqual(drainable)) {
                transfer = true;
            }
            if (transfer) {
                fluidSource.drain(milliBuckets, IFluidHandler.FluidAction.EXECUTE);
                return true;
            }
        }
        return false;
    }

    public boolean drainFluidTank(IFluidHandler fluidSource) {
        return this.drainFluidTank(fluidSource, BOTTLE_COUNT);
    }

    /**
     * copies the content of a fluid tank into this
     *
     * @param other forge fluid tank
     */
    public void copy(IFluidHandler other) {
        FluidStack forgeFluid = other.getFluidInTank(0).copy();// 250, IFluidHandler.FluidAction.SIMULATE);
        this.setFluid(forgeFluid);
        this.capCapacity();
    }

    /**
     * sets current fluid to provided forge fluid equivalent
     *
     * @param fluidStack forge fluid
     */
    public void setFluid(FluidStack fluidStack) {
        this.setFluid(SoftFluidStackImpl.fromForgeFluid(fluidStack));
    }


}

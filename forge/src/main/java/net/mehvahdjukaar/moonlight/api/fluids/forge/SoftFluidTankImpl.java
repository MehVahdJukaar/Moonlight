package net.mehvahdjukaar.moonlight.api.fluids.forge;

import com.google.common.base.Objects;
import net.mehvahdjukaar.moonlight.api.fluids.SoftFluidStack;
import net.mehvahdjukaar.moonlight.api.fluids.SoftFluidTank;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import org.jetbrains.annotations.NotNull;

import static net.mehvahdjukaar.moonlight.api.fluids.forge.SoftFluidStackImpl.MBtoBottles;
import static net.mehvahdjukaar.moonlight.api.fluids.forge.SoftFluidStackImpl.bottlesToMB;

/**
 * instance this fluid tank in your tile entity
 */
@Deprecated(forRemoval = true)
public class SoftFluidTankImpl extends SoftFluidTank {

    public static SoftFluidTank create(int capacity) {
        return new SoftFluidTankImpl(capacity);
    }

    protected SoftFluidTankImpl(int capacity) {
        super(capacity);
    }

    /**
     * checks if current tank holds equivalent fluid as provided forge fluids stack & nbt
     *
     * @param fluidStack forge fluid stack
     * @param com        fluid nbt
     * @return is same
     */
    @Deprecated(forRemoval = true)
    public boolean isSameFluidAs(FluidStack fluidStack, CompoundTag com) {
        return this.fluidStack.isEquivalent(fluidStack.getFluid()) && Objects.equal(com, this.fluidStack.getTag());
    }

    /**
     * try adding provided forge fluid to the tank
     *
     * @param fluidStack forge fluid stack
     * @return success
     */
    public boolean addVanillaFluid(FluidStack fluidStack) {
        var s = SoftFluidStackImpl.fromForgeFluid(fluidStack);
        if (s.isEmpty()) return false;
        return addFluid(s);
    }

    @NotNull
    @Deprecated(forRemoval = true)
    public static SoftFluidStack convertForgeFluid(FluidStack fluidStack) {
        int amount = MBtoBottles(fluidStack.getAmount());
        return SoftFluidStack.fromFluid(fluidStack.getFluid(), amount,
                fluidStack.hasTag() ? fluidStack.getTag().copy() : null);
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
        int milliBuckets = bottlesToMB(bottles);
        FluidStack stack = this.toEquivalentVanillaFluid(milliBuckets);
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
            CompoundTag fsTag = drainable.getTag();
            if (this.fluidStack.isEmpty()) {
                this.setFluid(drainable);
                transfer = true;
            } else if (this.isSameFluidAs(drainable, fsTag)) {
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
     * gets the equivalent forge fluid without draining the tank. returned stack might be empty
     *
     * @param mb forge minecraft buckets
     * @return forge fluid stacks
     */
    @Deprecated(forRemoval = true)
    public FluidStack toEquivalentVanillaFluid(int mb) {
        var s = ((SoftFluidStackImpl) this.fluidStack).toForgeFluid();
        s.setAmount(mb);
        return s;
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

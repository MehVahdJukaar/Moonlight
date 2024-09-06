package net.mehvahdjukaar.moonlight.api.fluids.neoforge;

import net.mehvahdjukaar.moonlight.api.fluids.SoftFluid;
import net.mehvahdjukaar.moonlight.api.fluids.SoftFluidStack;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentPatch;
import net.neoforged.neoforge.fluids.FluidStack;

public class SoftFluidStackImpl extends SoftFluidStack {

    public SoftFluidStackImpl(Holder<SoftFluid> fluid, int count, DataComponentPatch comp) {
        super(fluid, count, comp);
    }

    public static SoftFluidStack of(Holder<SoftFluid> fluid, int count,  DataComponentPatch components) {
        return new SoftFluidStackImpl(fluid, count, components);
    }

    public boolean isFluidEqual(FluidStack fluidStack) {
        return this.isSameFluidSameComponents(SoftFluidStackImpl.fromForgeFluid(fluidStack));
    }

    public static FluidStack toForgeFluid(SoftFluidStack softFluid) {
        FluidStack stack = new FluidStack(softFluid.fluid().getVanillaFluid(), bottlesToMB(softFluid.getCount()));
        if (!stack.isEmpty()) {
            softFluid.copyComponentsTo(stack);
        }
        return stack;
    }

    /**
     * gets the equivalent forge fluid without draining the tank. returned stack might be empty
     *
     * @return forge fluid stacks
     */
    public FluidStack toForgeFluid() {
        return toForgeFluid(this);
    }

    public static SoftFluidStack fromForgeFluid(FluidStack fluidStack) {
        int amount = MBtoBottles(fluidStack.getAmount());
        SoftFluidStack sf = SoftFluidStack.fromFluid(fluidStack.getFluid(), amount);
        SoftFluidStack.copyComponentsTo(fluidStack, sf, sf.fluid().getPreservedComponents());
        return sf;
    }

    public static int bottlesToMB(int bottles) {
        return bottles * 250;
    }

    public static int MBtoBottles(int milliBuckets) {
        return (int) (milliBuckets / 250f);
    }


}

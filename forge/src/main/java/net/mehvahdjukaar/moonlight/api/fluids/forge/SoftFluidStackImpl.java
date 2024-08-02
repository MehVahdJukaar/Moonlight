package net.mehvahdjukaar.moonlight.api.fluids.forge;

import net.mehvahdjukaar.moonlight.api.fluids.SoftFluid;
import net.mehvahdjukaar.moonlight.api.fluids.SoftFluidStack;
import net.mehvahdjukaar.moonlight.api.util.Utils;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.neoforged.neoforge.fluids.FluidStack;

import java.util.List;

public class SoftFluidStackImpl extends SoftFluidStack {

    public SoftFluidStackImpl(Holder<SoftFluid> fluid, int count, DataComponentPatch comp) {
        super(fluid, count, comp);
    }

    public static SoftFluidStack of(Holder<SoftFluid> fluid, int count,  DataComponentPatch components) {
        var f = new SoftFluidStackImpl(fluid, count, components);
        if (f.isEmpty()) {
            return SoftFluidStack.empty();
        }
        return f;
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

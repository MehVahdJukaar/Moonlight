package net.mehvahdjukaar.moonlight.api.fluids.platform;

import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.mehvahdjukaar.moonlight.api.fluids.SoftFluid;
import net.mehvahdjukaar.moonlight.api.fluids.SoftFluidStack;
import net.mehvahdjukaar.moonlight.api.util.Utils;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponentType;

public class SoftFluidStackImpl extends SoftFluidStack {

    public SoftFluidStackImpl(Holder<SoftFluid> fluid, int count, DataComponentPatch tag) {
        super(fluid, count, tag);
    }

    public static SoftFluidStack of(Holder<SoftFluid> fluid, int count, DataComponentPatch components) {
        return new SoftFluidStackImpl(fluid, count, components);
    }

    public static FluidVariant toFabricFluid(SoftFluidStack softFluid) {
        var comps = softFluid.getComponents().asPatch();
        var patch = DataComponentPatch.builder();
        for (var t : softFluid.fluid().getPreservedComponents()) {
            setComp(t.value(), comps, patch);
        }
        return FluidVariant.of(softFluid.fluid().getVanillaFluid().value(), patch.build());
    }

    private static <A> void setComp(DataComponentType<A> t, DataComponentPatch comps, DataComponentPatch.Builder patch) {
        var val = comps.get(t);
        if (val != null && val.isPresent()) {
            patch.set(t, val.get());
        }
    }

    @Deprecated(forRemoval = true)
    public static SoftFluidStack fromFabricFluid(FluidVariant variant, int bottlesAmount) {
        return fromFabricFluid(variant, bottlesAmount, Utils.hackyGetRegistryAccess());
    }

    public static SoftFluidStack fromFabricFluid(FluidVariant variant, int bottlesAmount, HolderLookup.Provider reg) {
        var comps = variant.getComponents();
        var patch = DataComponentPatch.builder();
        var softFluid = SoftFluidStack.fromFluid(variant.getFluid(), bottlesAmount, reg);
        for (var t : softFluid.fluid().getPreservedComponents()) {
            setComp(t.value(), comps, patch);
        }

        return SoftFluidStackImpl.of(softFluid.getHolder(), softFluid.getCount(), patch.build());
    }

}

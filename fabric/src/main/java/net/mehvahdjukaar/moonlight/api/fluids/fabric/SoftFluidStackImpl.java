package net.mehvahdjukaar.moonlight.api.fluids.fabric;

import net.mehvahdjukaar.moonlight.api.fluids.SoftFluid;
import net.mehvahdjukaar.moonlight.api.fluids.SoftFluidStack;
import net.minecraft.core.Holder;
import net.minecraft.nbt.CompoundTag;
import org.jetbrains.annotations.Nullable;

public class SoftFluidStackImpl extends SoftFluidStack{

    public SoftFluidStackImpl(Holder<SoftFluid> fluid, int count, CompoundTag tag) {
        super(fluid, count, tag);
    }

    public static SoftFluidStack of(Holder<SoftFluid> fluid, int count, @Nullable CompoundTag tag) {
        return new SoftFluidStackImpl(fluid, count, tag);
    }
}

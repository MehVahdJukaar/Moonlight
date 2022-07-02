package net.mehvahdjukaar.moonlight.fluids.fabric;

import net.mehvahdjukaar.moonlight.fluids.SoftFluid;
import net.minecraft.world.level.material.Fluid;

public class SoftFluidImpl extends SoftFluid {

    protected SoftFluidImpl(Builder builder) {
        super(builder);
    }

    public static void addFluidParam(Fluid fluid) {
    }

    public static SoftFluidImpl createInstance(SoftFluid.Builder builder) {
        return new SoftFluidImpl(builder);
    }


    public static void addFluidParam(SoftFluid.Builder builder, Fluid fluid) {
    }
}

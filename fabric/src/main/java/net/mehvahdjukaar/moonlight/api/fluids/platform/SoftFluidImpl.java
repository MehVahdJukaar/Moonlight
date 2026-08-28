package net.mehvahdjukaar.moonlight.api.fluids.platform;

import com.mojang.datafixers.util.Pair;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariantAttributes;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.material.Fluid;

public class SoftFluidImpl {

    public static Pair<Integer, Component> getFluidSpecificAttributes(Fluid fluid) {
        FluidVariant variant = FluidVariant.of(fluid);
        return Pair.of(FluidVariantAttributes.getLuminance(variant), FluidVariantAttributes.getName(variant));
    }
}

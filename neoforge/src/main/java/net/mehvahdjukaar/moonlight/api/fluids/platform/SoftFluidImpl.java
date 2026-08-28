package net.mehvahdjukaar.moonlight.api.fluids.platform;

import com.mojang.datafixers.util.Pair;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidType;

public class SoftFluidImpl {

    public static Pair<Integer, Component> getFluidSpecificAttributes(Fluid fluid) {
        FluidType type = fluid.getFluidType();
        return Pair.of(type.getLightLevel(), type.getDescription());
    }
}

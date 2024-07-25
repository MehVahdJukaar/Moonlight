package net.mehvahdjukaar.moonlight.api.fluids.forge;

import com.mojang.datafixers.util.Pair;
import net.mehvahdjukaar.moonlight.api.misc.Triplet;
import net.mehvahdjukaar.moonlight.core.Moonlight;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidType;

public class SoftFluidImpl {

    public static Pair<Integer, Component> getFluidSpecificAttributes(Fluid fluid) {
        FluidType type = fluid.getFluidType();
        int l = type.getLightLevel();
        Component tr = type.getDescription();
        return Pair.of(l, tr);
    }

    public static Triplet<ResourceLocation, ResourceLocation, Integer> getRenderingData(ResourceLocation useTexturesFrom) {
        Fluid f = BuiltInRegistries.FLUID.getOptional(useTexturesFrom).orElse(null);
        if (f != null && f != Fluids.EMPTY) {
            // do we even have access on this on server side?
            IClientFluidTypeExtensions prop = IClientFluidTypeExtensions.of(f);
            if (prop != IClientFluidTypeExtensions.DEFAULT) {
                var s = new FluidStack(f, 1000);
                var still = prop.getStillTexture(s);
                var flowing = prop.getFlowingTexture(s);
                var tint = prop.getTintColor(s);
                //accounts for some mods that don't respect getStillTexture contract
                if (still == null || flowing == null) {
                    Moonlight.LOGGER.warn("Fluid " + useTexturesFrom + " returned null on its textures. Its soft fluid might not render well");
                    return null;
                }
                return Triplet.of(still, flowing, tint);
            }
        }
        return null;
    }
}

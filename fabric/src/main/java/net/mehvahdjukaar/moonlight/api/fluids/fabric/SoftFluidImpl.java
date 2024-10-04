package net.mehvahdjukaar.moonlight.api.fluids.fabric;

import com.mojang.datafixers.util.Pair;
import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandler;
import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandlerRegistry;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariantAttributes;
import net.mehvahdjukaar.moonlight.api.misc.Triplet;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.material.Fluid;
import org.jetbrains.annotations.Nullable;

public class SoftFluidImpl {

    public static Pair<Integer, Component> getFluidSpecificAttributes(Fluid fluid) {
        FluidVariant variant = FluidVariant.of(fluid);
        int l = FluidVariantAttributes.getLuminance(variant);
        Component tr = FluidVariantAttributes.getName(variant);
        return Pair.of(l, tr);
    }

    @Nullable
    public static Triplet<ResourceLocation, ResourceLocation, Integer> getRenderingData(ResourceLocation useTexturesFrom) {
        var fluid = BuiltInRegistries.FLUID.getOptional(useTexturesFrom);
        if (fluid.isPresent()) {
            var f = fluid.get();
            FluidRenderHandler handler = FluidRenderHandlerRegistry.INSTANCE.get(f);
            if (handler != null) {
                try {
                    var textures = handler.getFluidSprites(null, null, f.defaultFluidState());
                    int tint = handler.getFluidColor(null, null, f.defaultFluidState());
                    return Triplet.of(textures[0].contents().name(), textures[1].contents().name(), tint);
                } catch (Exception e) {
                    throw new IllegalStateException("Fluid " + useTexturesFrom + " had invalid rendering data. How? FluidRenderHandler MUST have at lest 2 textures", e);
                }
            }
        }
        return null;
    }
}

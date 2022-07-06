package net.mehvahdjukaar.moonlight.fluids.forge;

import net.mehvahdjukaar.moonlight.fluids.SoftFluid;
import net.mehvahdjukaar.moonlight.misc.Triplet;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.client.IFluidTypeRenderProperties;
import net.minecraftforge.client.RenderProperties;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidType;
import net.minecraftforge.registries.ForgeRegistries;

public class SoftFluidImpl {

    public static void addFluidSpecificAttributes(SoftFluid.Builder builder, Fluid fluid) {
        FluidType type = fluid.getFluidType();
        builder.luminosity(type.getLightLevel());
        String tr = type.getDescriptionId();
        if (tr != null) builder.translationKey(tr);
    }

    public static Triplet<ResourceLocation, ResourceLocation, Integer> getRenderingData(ResourceLocation useTexturesFrom) {
        Fluid f = ForgeRegistries.FLUIDS.getValue(useTexturesFrom);
        if (f != null && f != Fluids.EMPTY) {
            var prop = RenderProperties.get(f);
            if (prop != IFluidTypeRenderProperties.DUMMY) {
                var s = new FluidStack(f, 1000);
                var still = prop.getStillTexture(s);
                var flowing = prop.getFlowingTexture(s);
                var tint = prop.getColorTint(s);
                return Triplet.of(still,flowing,tint);
            }
        }
        return null;
    }
}

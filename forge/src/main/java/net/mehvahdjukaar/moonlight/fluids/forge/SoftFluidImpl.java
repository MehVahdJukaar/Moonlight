package net.mehvahdjukaar.moonlight.fluids.forge;

import net.mehvahdjukaar.moonlight.fluids.SoftFluid;
import net.mehvahdjukaar.moonlight.misc.Triplet;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.client.RenderProperties;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.IForgeRegistryEntry;

import javax.annotation.Nullable;

public class SoftFluidImpl extends SoftFluid implements IForgeRegistryEntry<SoftFluidImpl> {

    protected SoftFluidImpl(Builder builder) {
        super(builder);
    }

    @Nullable
    public static Triplet<ResourceLocation, ResourceLocation, Integer> getRenderingData(ResourceLocation useTexturesFrom) {
        Fluid f = ForgeRegistries.FLUIDS.getValue(useTexturesFrom);
        if (f != null && f != Fluids.EMPTY) {
            var prop = f.getAttributes();
            return Triplet.of(prop.getStillTexture(), prop.getFlowingTexture(), prop.getColor());
            /*
            var prop = RenderProperties.get(f);
            if (prop != IFluidTypeRenderProperties.DUMMY) {
                var s = new FluidStack(f, 1000);
                var still = prop.getStillTexture(s);
                var flowing = prop.getFlowingTexture(s);
                var tint = prop.getColorTint(s);
                return Triplet.of(still, flowing, tint)
            }*/
        }
        return null;

    }

    public static void addFluidParam(SoftFluid.Builder builder, Fluid fluid) {
        //FluidType type = fluid.getFluidType();
        builder.luminosity(        fluid.getAttributes().getLuminosity());
        String tr = fluid.getAttributes().getTranslationKey();
        if (tr != null) builder.translationKey(tr);
    }

    public static SoftFluidImpl createInstance(Builder builder) {
        return new SoftFluidImpl(builder);
    }

    private ResourceLocation id;

    @Override
    public SoftFluidImpl setRegistryName(ResourceLocation arg) {
        this.id = arg;
        return this;
    }

    @Nullable
    @Override
    public ResourceLocation getRegistryName() {
        return id;
    }

    @Override
    public Class<SoftFluidImpl> getRegistryType() {
        return SoftFluidImpl.class;
    }

}

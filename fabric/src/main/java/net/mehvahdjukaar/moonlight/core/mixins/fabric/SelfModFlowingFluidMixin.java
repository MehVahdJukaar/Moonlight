package net.mehvahdjukaar.moonlight.core.mixins.fabric;

import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandler;
import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandlerRegistry;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariantAttributeHandler;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariantAttributes;
import net.mehvahdjukaar.moonlight.api.fluids.ModFlowingFluid;
import net.mehvahdjukaar.moonlight.api.platform.PlatHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.material.FlowingFluid;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import java.util.function.Supplier;

@Mixin(ModFlowingFluid.class)
public abstract class SelfModFlowingFluidMixin extends FlowingFluid {

    /**
     * @author
     * @reason
     */
    @Overwrite(remap = false)
    private void afterInit(ModFlowingFluid.Properties properties) {
        if (properties.copyFluid != null) {
            var handler = FluidVariantAttributes.getHandler(properties.copyFluid);
            if (handler != null) {
                FluidVariantAttributes.register(this, handler);
            }
        } else {
            FluidVariantAttributes.register(this, new FluidVariantAttributeHandler() {
                @Override
                public int getLuminance(FluidVariant variant) {
                    return properties.lightLevel;
                }

                @Override
                public int getTemperature(FluidVariant variant) {
                    return properties.temperature;
                }

                @Override
                public int getViscosity(FluidVariant variant, @Nullable Level world) {
                    return properties.viscosity;
                }
            });
        }
        if (PlatHelper.getPhysicalSide().isClient()) {
            FluidRenderHandlerRegistry.INSTANCE.register(this, (FluidRenderHandler) ((ModFlowingFluid) (Object) this).createRenderProperties());
        }
    }
}

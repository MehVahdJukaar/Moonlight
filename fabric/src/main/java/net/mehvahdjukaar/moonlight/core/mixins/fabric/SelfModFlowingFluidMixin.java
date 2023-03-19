package net.mehvahdjukaar.moonlight.core.mixins.fabric;

import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandler;
import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandlerRegistry;
import net.mehvahdjukaar.moonlight.api.fluids.ModFlowingFluid;
import net.mehvahdjukaar.moonlight.api.platform.PlatHelper;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.material.FlowingFluid;
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
    private void afterInit(ModFlowingFluid.Properties properties, Supplier<? extends LiquidBlock> block) {
        if (PlatHelper.getPhysicalSide().isClient()) {
            FluidRenderHandlerRegistry.INSTANCE.register(this, (FluidRenderHandler) ((ModFlowingFluid) (Object) this).createRenderProperties());
        }
    }
}

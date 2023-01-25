package net.mehvahdjukaar.moonlight.core.mixins.fabric;

import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandler;
import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandlerRegistry;
import net.mehvahdjukaar.moonlight.api.fluids.ModFlowingFluid;
import net.mehvahdjukaar.moonlight.api.platform.PlatformHelper;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.material.FlowingFluid;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Supplier;

@Mixin(ModFlowingFluid.class)
public abstract class SelfModFlowingFluidMixin extends FlowingFluid {

    @Inject(method = "<init>", at = @At("TAIL"))
    public void afterInit(ModFlowingFluid.Properties properties, Supplier<? extends LiquidBlock> block, CallbackInfo cir) {

        if (PlatformHelper.getEnv().isClient()) {
            FluidRenderHandlerRegistry.INSTANCE.register(this, (FluidRenderHandler) ((ModFlowingFluid) (Object) this).createRenderProperties());
        }
    }
}

package net.mehvahdjukaar.moonlight.core.mixins.forge;

import net.mehvahdjukaar.moonlight.api.client.forge.ModFluidType;
import net.mehvahdjukaar.moonlight.api.fluids.ModFlowingFluid;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraftforge.fluids.FluidType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Supplier;

@Mixin(ModFlowingFluid.class)
public abstract class SelfModFlowingFluidMixin extends FlowingFluid {

    @Unique
    private ModFluidType type;

    @Inject(method = "<init>", at = @At("TAIL"))
    public void afterInit(ModFlowingFluid.Properties properties, Supplier<? extends LiquidBlock> block, CallbackInfo cir) {
        this.type = ModFluidType.create(properties,(ModFlowingFluid)(Object) this);
    }

    @Override
    public FluidType getFluidType() {
        return type;
    }
}

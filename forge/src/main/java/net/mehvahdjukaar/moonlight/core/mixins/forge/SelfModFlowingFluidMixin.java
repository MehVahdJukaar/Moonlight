package net.mehvahdjukaar.moonlight.core.mixins.forge;

import net.mehvahdjukaar.moonlight.api.client.forge.ModFluidType;
import net.mehvahdjukaar.moonlight.api.fluids.ModFlowingFluid;
import net.minecraft.world.level.material.FlowingFluid;
import net.neoforged.neoforge.fluids.FluidType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Unique;

import java.util.function.Supplier;

@SuppressWarnings("all")
@Mixin(ModFlowingFluid.class)
public abstract class SelfModFlowingFluidMixin extends FlowingFluid {

    @Unique
    private Supplier<FluidType> type;

    @Overwrite(remap = false)
    private void afterInit(ModFlowingFluid.Properties properties) {
        var t = ModFluidType.create(properties, (ModFlowingFluid) (Object) this);
        this.type = () -> t; //this also needs to be registered later
    }

    @Override
    public FluidType getFluidType() {
        return type.get();
    }
}

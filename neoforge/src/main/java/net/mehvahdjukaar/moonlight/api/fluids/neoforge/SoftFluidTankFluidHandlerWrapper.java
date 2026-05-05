package net.mehvahdjukaar.moonlight.api.fluids.neoforge;

import net.mehvahdjukaar.moonlight.api.block.ISoftFluidTankProvider;
import net.mehvahdjukaar.moonlight.api.fluids.SoftFluidStack;
import net.mehvahdjukaar.moonlight.api.fluids.SoftFluidTank;
import net.mehvahdjukaar.moonlight.api.fluids.platform.SoftFluidStackImpl;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
@Deprecated(forRemoval = true)
public record SoftFluidTankFluidHandlerWrapper(SoftFluidTank tank, BlockEntity be) implements IFluidHandler {

    public static <T extends BlockEntity & ISoftFluidTankProvider> SoftFluidTankFluidHandlerWrapper wrap(T be) {
        return new SoftFluidTankFluidHandlerWrapper(be.getSoftFluidTank(), be);
    }

    @Override
    public int getTanks() {
        return 1;
    }

    @Override
    public @NotNull FluidStack getFluidInTank(int i) {
        return SoftFluidStackImpl.toForgeFluid(tank.getFluid());
    }

    @Override
    public int getTankCapacity(int i) {
        return SoftFluidStackImpl.bottlesToMB(tank.getCapacity());
    }

    @Override
    public boolean isFluidValid(int i, @NotNull FluidStack fluidStack) {
        return tank.isFluidCompatible(SoftFluidStackImpl.fromForgeFluid(fluidStack, Objects.requireNonNull(be.getLevel()).registryAccess()));
    }

    @Override
    public int fill(FluidStack fluidStack, FluidAction fluidAction) {
        RegistryAccess ra = Objects.requireNonNull(be.getLevel()).registryAccess();
        SoftFluidStack original = SoftFluidStackImpl.fromForgeFluid(fluidStack, ra);
        int filled = tank.addFluid(original, fluidAction.simulate());
        if (!fluidAction.simulate()) {
            int bottlesRemoved = SoftFluidStackImpl.fromForgeFluid(fluidStack, ra).getCount() - original.getCount();
            fluidStack.shrink(SoftFluidStackImpl.bottlesToMB(bottlesRemoved));
            be.setChanged();
        }
        return filled;
    }

    @Override
    public @NotNull FluidStack drain(FluidStack fluidStack, FluidAction fluidAction) {
        return drain(fluidStack.getAmount(), fluidAction);
    }

    @Override
    public @NotNull FluidStack drain(int i, FluidAction fluidAction) {
        var drained = tank.removeFluid(i, fluidAction.simulate());
        if (!fluidAction.simulate()) be.setChanged();
        return SoftFluidStackImpl.toForgeFluid(drained);
    }
}
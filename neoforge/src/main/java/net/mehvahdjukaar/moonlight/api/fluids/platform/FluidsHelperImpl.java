package net.mehvahdjukaar.moonlight.api.fluids.platform;

import net.mehvahdjukaar.moonlight.api.fluids.FluidOffer;
import net.mehvahdjukaar.moonlight.api.fluids.SoftFluidStack;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import org.jetbrains.annotations.Nullable;

import static net.mehvahdjukaar.moonlight.api.fluids.platform.SoftFluidStackImpl.bottlesToMB;

@SuppressWarnings("ConstantConditions")
public class FluidsHelperImpl {

    public static boolean extractFluidFromTank(BlockEntity tileBack, Direction dir, int amount) {
        var handlerBack = tileBack.getLevel().getCapability(Capabilities.Fluid.BLOCK,
                tileBack.getBlockPos(), tileBack.getBlockState(), tileBack, dir);
        if (handlerBack == null) return false;
        FluidResource resource = firstFluid(handlerBack);
        if (resource == null) return false;
        //only works in 250 increment
        int toExtract = bottlesToMB(amount);
        try (Transaction t = Transaction.openRoot()) {
            if (handlerBack.extract(resource, toExtract, t) != toExtract) return false;
            t.commit();
        }
        tileBack.setChanged();
        return true;
    }

    public static Integer fillFluidTank(BlockEntity tileBelow, FluidOffer offer, Direction dir) {
        var handlerDown = tileBelow.getLevel().getCapability(Capabilities.Fluid.BLOCK,
                tileBelow.getBlockPos(), tileBelow.getBlockState(), tileBelow, dir);
        if (handlerDown != null && offer.fluid() instanceof SoftFluidStackImpl impl) {
            FluidStack stack = impl.toForgeFluid();
            int toFill = bottlesToMB(offer.minAmount());
            if (!stack.isEmpty() && toFill > 0) {
                int filled;
                try (Transaction t = Transaction.openRoot()) {
                    filled = handlerDown.insert(FluidResource.of(stack), toFill, t);
                    t.commit();
                }
                tileBelow.setChanged();

                return Mth.ceil(filled / 250f);
            }
        }
        return null;
    }

    public static boolean hasFluidHandler(Level level, BlockPos pos, Direction dir) {
        return level.getCapability(Capabilities.Fluid.BLOCK, pos, dir) != null;
    }

    @Nullable
    public static FluidOffer getFluidInTank(Level level, BlockPos pos, Direction dir, BlockEntity source) {
        var handler = level.getCapability(Capabilities.Fluid.BLOCK, pos, dir);
        if (handler != null) {
            FluidResource resource = firstFluid(handler);
            if (resource == null) return null;
            //a tank may only allow certain increments
            for (int i = 1; i <= 4; i++) {
                int toDrain = bottlesToMB(i);
                int drained;
                try (Transaction t = Transaction.openRoot()) {
                    drained = handler.extract(resource, toDrain, t);
                }
                if (drained >= 250) {
                    SoftFluidStack forgeFluid = SoftFluidStackImpl.fromForgeFluid(
                            resource.toStack(drained), level.registryAccess());
                    if (!forgeFluid.isEmpty()) {
                        //TODO: technically here we could try all lower amounts too to find the min but its probably not worth it
                        return FluidOffer.of(forgeFluid, drained / 250);
                    }
                }
            }
        }
        return null;
    }

    @Nullable
    private static FluidResource firstFluid(ResourceHandler<FluidResource> handler) {
        for (int i = 0; i < handler.size(); i++) {
            FluidResource resource = handler.getResource(i);
            if (!resource.isEmpty() && handler.getAmountAsInt(i) > 0) return resource;
        }
        return null;
    }

}

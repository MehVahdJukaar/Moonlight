package net.mehvahdjukaar.moonlight.api.fluids.platform;

import net.mehvahdjukaar.moonlight.api.block.ISoftFluidTankProvider;
import net.mehvahdjukaar.moonlight.api.fluids.SoftFluidStack;
import net.mehvahdjukaar.moonlight.api.fluids.SoftFluidTank;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.transaction.SnapshotJournal;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

import java.util.Objects;

import static net.mehvahdjukaar.moonlight.api.fluids.platform.SoftFluidStackImpl.MBtoBottles;
import static net.mehvahdjukaar.moonlight.api.fluids.platform.SoftFluidStackImpl.bottlesToMB;

/** SoftFluidTank as a NeoForge fluid handler. Amounts below a bottle (250 mB) are never moved. */
public class SoftFluidTankFluidHandlerWrapper extends SnapshotJournal<SoftFluidStack>
        implements ResourceHandler<FluidResource> {

    private final SoftFluidTank tank;
    private final BlockEntity be;

    public SoftFluidTankFluidHandlerWrapper(SoftFluidTank tank, BlockEntity be) {
        this.tank = tank;
        this.be = be;
    }

    public static <T extends BlockEntity & ISoftFluidTankProvider> SoftFluidTankFluidHandlerWrapper wrap(T be) {
        return new SoftFluidTankFluidHandlerWrapper(be.getSoftFluidTank(), be);
    }

    private HolderLookup.Provider registries() {
        return Objects.requireNonNull(be.getLevel()).registryAccess();
    }

    private SoftFluidStack toSoftFluid(FluidResource resource, int bottles) {
        return SoftFluidStackImpl.fromForgeFluid(resource.toStack(bottlesToMB(bottles)), registries());
    }

    @Override
    public int size() {
        return 1;
    }

    @Override
    public FluidResource getResource(int index) {
        Objects.checkIndex(index, 1);
        return FluidResource.of(SoftFluidStackImpl.toForgeFluid(tank.getFluid()));
    }

    @Override
    public long getAmountAsLong(int index) {
        Objects.checkIndex(index, 1);
        return bottlesToMB(tank.getFluidCount());
    }

    @Override
    public long getCapacityAsLong(int index, FluidResource resource) {
        Objects.checkIndex(index, 1);
        return bottlesToMB(tank.getCapacity());
    }

    @Override
    public boolean isValid(int index, FluidResource resource) {
        Objects.checkIndex(index, 1);
        return tank.isFluidCompatible(toSoftFluid(resource, SoftFluidTank.BOTTLE_COUNT));
    }

    @Override
    public int insert(int index, FluidResource resource, int amount, TransactionContext transaction) {
        Objects.checkIndex(index, 1);
        int bottles = MBtoBottles(amount);
        if (bottles <= 0) return 0;
        SoftFluidStack toAdd = toSoftFluid(resource, bottles);
        if (toAdd.isEmpty() || tank.addFluid(toAdd, true) <= 0) return 0;
        updateSnapshots(transaction);
        return bottlesToMB(tank.addFluid(toAdd, false));
    }

    @Override
    public int extract(int index, FluidResource resource, int amount, TransactionContext transaction) {
        Objects.checkIndex(index, 1);
        int bottles = MBtoBottles(amount);
        if (bottles <= 0 || !resource.matches(SoftFluidStackImpl.toForgeFluid(tank.getFluid()))) return 0;
        if (tank.removeFluid(bottles, true).isEmpty()) return 0;
        updateSnapshots(transaction);
        return bottlesToMB(tank.removeFluid(bottles, false).getCount());
    }

    @Override
    protected SoftFluidStack createSnapshot() {
        return tank.getFluid().copy();
    }

    @Override
    protected void revertToSnapshot(SoftFluidStack snapshot) {
        tank.setFluid(snapshot);
    }

    @Override
    protected void onRootCommit(SoftFluidStack originalState) {
        be.setChanged();
    }
}

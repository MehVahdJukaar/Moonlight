package net.mehvahdjukaar.moonlight.api.fluids.forge;

import net.mehvahdjukaar.moonlight.api.fluids.SoftFluid;
import net.mehvahdjukaar.moonlight.api.fluids.SoftFluidRegistry;
import net.mehvahdjukaar.moonlight.api.fluids.SoftFluidTank;
import net.mehvahdjukaar.moonlight.api.fluids.BuiltInSoftFluids;
import net.mehvahdjukaar.moonlight.api.util.PotionNBTHelper;
import net.mehvahdjukaar.moonlight.api.util.Utils;
import net.mehvahdjukaar.moonlight.core.client.SoftFluidParticleColors;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;

import org.jetbrains.annotations.Nullable;
import java.util.List;

/**
 * instance this fluid tank in your tile entity
 */
public class SoftFluidTankImpl extends SoftFluidTank {


    public static SoftFluidTank create(int capacity) {
        return new SoftFluidTankImpl(capacity);
    }

    protected SoftFluidTankImpl(int capacity) {
        super(capacity);
    }

    /**
     * checks if current tank holds equivalent fluid as provided forge fluids stack
     *
     * @param fluidStack forge fluid stack
     * @return is same
     */
    //might be wrong
    public boolean isSameFluidAs(FluidStack fluidStack) {
        return isSameFluidAs(fluidStack, fluidStack.getTag());
    }

    /**
     * checks if current tank holds equivalent fluid as provided forge fluids stack & nbt
     *
     * @param fluidStack forge fluid stack
     * @param com        fluid nbt
     * @return is same
     */
    public boolean isSameFluidAs(FluidStack fluidStack, CompoundTag com) {
        return this.fluid.isEquivalent(fluidStack.getFluid()) && areNbtEquals(com, this.nbt);
    }

    /**
     * try adding provided forge fluid to the tank
     *
     * @param fluidStack forge fluid stack
     * @return success
     */
    public boolean tryAddingFluid(FluidStack fluidStack) {
        int count = fluidStack.getAmount();
        SoftFluid s = SoftFluidRegistry.fromVanillaFluid(fluid.getVanillaFluid());
        return tryAddingFluid(s, count, fluidStack.getTag());
    }

    /**
     * empties n bottle of content into said forge fluid tank
     *
     * @param fluidDestination forge fluid tank handler
     * @param bottles          number of bottles to empty (1blt = 250mb)
     * @return success
     */
    public boolean tryTransferToFluidTank(IFluidHandler fluidDestination, int bottles) {
        if (!this.canRemove(bottles)) return false;
        int milliBuckets = bottles * 250;
        FluidStack stack = this.toEquivalentVanillaFluid(milliBuckets);
        if (!stack.isEmpty()) {
            int fillableAmount = fluidDestination.fill(stack, IFluidHandler.FluidAction.SIMULATE);
            if (fillableAmount == milliBuckets) {
                fluidDestination.fill(stack, IFluidHandler.FluidAction.EXECUTE);
                this.shrink(bottles);
                return true;
            }
        }
        return false;
    }

    public boolean tryTransferToFluidTank(IFluidHandler fluidDestination) {
        return this.tryTransferToFluidTank(fluidDestination, BOTTLE_COUNT);
    }

    //drains said fluid tank of 250mb (1 bottle) of fluid
    public boolean drainFluidTank(IFluidHandler fluidSource, int bottles) {
        if (!this.canAdd(bottles)) return false;
        int milliBuckets = bottles * 250;
        FluidStack drainable = fluidSource.drain(milliBuckets, IFluidHandler.FluidAction.SIMULATE);
        if (!drainable.isEmpty() && drainable.getAmount() == milliBuckets) {
            boolean transfer = false;
            CompoundTag fsTag = drainable.getTag();
            if (this.fluid.isEmpty()) {
                this.setFluid(SoftFluidRegistry.fromVanillaFluid(drainable.getFluid()), fsTag);
                transfer = true;
            } else if (this.isSameFluidAs(drainable, fsTag)) {
                transfer = true;
            }
            if (transfer) {
                fluidSource.drain(milliBuckets, IFluidHandler.FluidAction.EXECUTE);
                this.grow(bottles);
                return true;
            }
        }
        return false;
    }

    public boolean drainFluidTank(IFluidHandler fluidSource) {
        return this.drainFluidTank(fluidSource, BOTTLE_COUNT);
    }

    /**
     * gets the equivalent forge fluid without draining the tank. returned stack might be empty
     *
     * @param mb forge minecraft buckets
     * @return forge fluid stacks
     */
    public FluidStack toEquivalentVanillaFluid(int mb) {
        FluidStack stack = new FluidStack(this.fluid.getVanillaFluid(), mb);
        this.applyNBTtoFluidStack(stack);
        return stack;
    }

    @Deprecated(forRemoval = true)
    public FluidStack toEquivalentForgeFluid(int mb) {
        return toEquivalentVanillaFluid(mb);
    }

    private void applyNBTtoFluidStack(FluidStack fluidStack) {
        List<String> nbtKey = this.fluid.getNbtKeyFromItem();
        if (this.nbt != null && !this.nbt.isEmpty() && !fluidStack.isEmpty() && nbtKey != null) {
            CompoundTag newCom = new CompoundTag();
            for (String k : nbtKey) {
                //special case to convert to IE pot fluid
                if (k.equals(POTION_TYPE_KEY) && Utils.getID(fluidStack.getFluid()).getNamespace().equals("immersiveengineering")) {
                    continue;
                }
                Tag c = this.nbt.get(k);
                if (c != null) {
                    newCom.put(k, c);
                }
            }
            if (!newCom.isEmpty()) fluidStack.setTag(newCom);
        }
    }

    /**
     * copies the content of a fluid tank into this
     *
     * @param other forge fluid tank
     */
    public void copy(IFluidHandler other) {
        FluidStack drainable = other.getFluidInTank(0).copy();// 250, IFluidHandler.FluidAction.SIMULATE);
        CompoundTag nbt = drainable.isEmpty() ? null : drainable.getTag();
        this.setFluid(SoftFluidRegistry.fromVanillaFluid(drainable.getFluid()), nbt);
        this.setCount((int) Math.min(this.capacity, other.getTankCapacity(0)));
    }

    /**
     * fills to max capacity with provided forge fluid
     *
     * @param fluidStack forge fluid
     */
    public void fill(FluidStack fluidStack) {
        this.setFluid(fluidStack);
        this.fillCount();
    }

    /**
     * sets current fluid to provided forge fluid equivalent
     *
     * @param fluidStack forge fluid
     */
    public void setFluid(FluidStack fluidStack) {
        SoftFluid s = SoftFluidRegistry.fromVanillaFluid(fluidStack.getFluid());
        this.setFluid(s, fluidStack.getTag());
    }


    /**
     * @return tint color to be applied on the fluid texture
     */
    public int getTintColor(@Nullable BlockAndTintGetter world, @Nullable BlockPos pos) {
        SoftFluid.TintMethod method = this.fluid.getTintMethod();
        if (method == SoftFluid.TintMethod.NO_TINT) return -1;
        if (this.needsColorRefresh) {
            this.refreshSpecialColor(world, pos);
            this.needsColorRefresh = false;
        }
        if (this.specialColor != 0) return this.specialColor;
        return this.fluid.getTintColor();
    }

    /**
     * @return tint color to be applied on the fluid texture
     */
    public int getFlowingTint(@Nullable BlockAndTintGetter world, @Nullable BlockPos pos) {
        SoftFluid.TintMethod method = this.fluid.getTintMethod();
        if (method == SoftFluid.TintMethod.FLOWING) return this.getParticleColor(world, pos);
        else return this.getTintColor(world, pos);
    }

    /**
     * @return tint color to be used on particle. Differs from getTintColor since it returns an mixWith color extrapolated from their fluid textures
     */
    public int getParticleColor(@Nullable BlockAndTintGetter world, @Nullable BlockPos pos) {
        if (this.isEmpty()) return -1;
        int tintColor = this.getTintColor(world, pos);
        //if tint color is white gets averaged color
        if (tintColor == -1) return SoftFluidParticleColors.getParticleColor(this.fluid);
        return tintColor;
    }

    //grabs world/ fluid stack dependent tint color if fluid has associated forge fluid. overrides normal tint color
    private void refreshSpecialColor(@Nullable BlockAndTintGetter world, @Nullable BlockPos pos) {
        //yay hardcoding
        //at least this works for any fluid
        if(this.nbt != null && this.nbt.contains("color")){
            this.specialColor = this.nbt.getInt("color");
        }
        if (fluid == BuiltInSoftFluids.POTION.get()) {
            this.specialColor = PotionNBTHelper.getColorFromNBT(this.nbt);
        } else {
            Fluid f = this.fluid.getVanillaFluid();
            if (f != Fluids.EMPTY) {
                var prop = IClientFluidTypeExtensions.of(f);
                if (prop != IClientFluidTypeExtensions.DEFAULT) {
                    //world accessor
                    int w;
                    //stack accessor
                    w = prop.getTintColor(this.toEquivalentVanillaFluid(1));
                    if (w != -1) this.specialColor = w;
                    else {
                        w = prop.getTintColor(f.defaultFluidState(),world, pos);
                        if (w != -1) this.specialColor = w;
                    }
                }
            }
        }
    }


}

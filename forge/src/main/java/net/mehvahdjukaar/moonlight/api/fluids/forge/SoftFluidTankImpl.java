package net.mehvahdjukaar.moonlight.api.fluids.forge;

import com.google.common.base.Objects;
import net.mehvahdjukaar.moonlight.api.fluids.*;
import net.mehvahdjukaar.moonlight.api.util.PotionNBTHelper;
import net.mehvahdjukaar.moonlight.api.util.Utils;
import net.mehvahdjukaar.moonlight.core.client.SoftFluidParticleColors;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
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

import static net.mehvahdjukaar.moonlight.api.fluids.SoftFluidStack.POTION_TYPE_KEY;

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
        return this.fluid.isEquivalent(fluidStack.getFluid()) && Objects.equal(com, this.fluid.getTag());
    }

    /**
     * try adding provided forge fluid to the tank
     *
     * @param fluidStack forge fluid stack
     * @return success
     */
    public boolean addVanillaFluid(FluidStack fluidStack) {
        Holder<SoftFluid> s = SoftFluidRegistry.fromVanillaFluid(fluidStack.getFluid());
        if (s == null) return false;
        return addFluid(new SoftFluidStack(s, fluidStack.getAmount(), fluidStack.getTag().copy()));
    }

    //TODO: re check all the ones below here. I blindly ported

    /**
     * empties n bottle of content into said forge fluid tank
     *
     * @param fluidDestination forge fluid tank handler
     * @param bottles          number of bottles to empty (1blt = 250mb)
     * @return success
     */
    public boolean tryTransferToFluidTank(IFluidHandler fluidDestination, int bottles) {
        if (this.getFluidCount() < bottles) return false;
        int milliBuckets = bottlesToMB(bottles);
        FluidStack stack = this.toEquivalentVanillaFluid(milliBuckets);
        if (!stack.isEmpty()) {
            int fillableAmount = fluidDestination.fill(stack, IFluidHandler.FluidAction.SIMULATE);
            if (fillableAmount == milliBuckets) {
                fluidDestination.fill(stack, IFluidHandler.FluidAction.EXECUTE);
                this.fluid.shrink(bottles);
                return true;
            }
        }
        return false;
    }

    public static int bottlesToMB(int bottles) {
        return bottles * 250;
    }

    public static int MBtoBottles(int milliBuckets) {
        return (int) (milliBuckets / 250f);
    }

    public boolean tryTransferToFluidTank(IFluidHandler fluidDestination) {
        return this.tryTransferToFluidTank(fluidDestination, BOTTLE_COUNT);
    }

    //drains said fluid tank of 250mb (1 bottle) of fluid
    public boolean drainFluidTank(IFluidHandler fluidSource, int bottles) {
        if (this.getSpace()<bottles) return false;
        int milliBuckets = bottlesToMB(bottles);
        FluidStack drainable = fluidSource.drain(milliBuckets, IFluidHandler.FluidAction.SIMULATE);
        if (!drainable.isEmpty() && drainable.getAmount() == milliBuckets) {
            boolean transfer = false;
            CompoundTag fsTag = drainable.getTag();
            if (this.fluid.isEmpty()) {
                this.setFluid(drainable);
                transfer = true;
            } else if (this.isSameFluidAs(drainable, fsTag)) {
                transfer = true;
            }
            if (transfer) {
                fluidSource.drain(milliBuckets, IFluidHandler.FluidAction.EXECUTE);
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

    private void applyNBTtoFluidStack(FluidStack fluidStack) {
        List<String> nbtKey = this.fluid.getFluid().value().getNbtKeyFromItem();
        CompoundTag tag = this.fluid.getTag();
        if (tag != null && !tag.isEmpty() && !fluidStack.isEmpty() && nbtKey != null) {
            CompoundTag newCom = new CompoundTag();
            for (String k : nbtKey) {
                //special case to convert to IE pot fluid
                if (k.equals(POTION_TYPE_KEY) && Utils.getID(fluidStack.getFluid()).getNamespace().equals("immersiveengineering")) {
                    continue;
                }
                Tag c = tag.get(k);
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
        FluidStack forgeFluid = other.getFluidInTank(0).copy();// 250, IFluidHandler.FluidAction.SIMULATE);
        this.setFluid(forgeFluid);
        this.capCapacity();
    }

    /**
     * sets current fluid to provided forge fluid equivalent
     *
     * @param fluidStack forge fluid
     */
    public void setFluid(FluidStack fluidStack) {
        var s = SoftFluidRegistry.fromVanillaFluid(fluidStack.getFluid());
        if (s != null) {
            int amount = MBtoBottles(fluidStack.getAmount());
            this.setFluid(new SoftFluidStack(s, amount, fluidStack.getTag().copy()));
        }
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
        if (tintColor == -1) return SoftFluidParticleColors.getParticleColor(this.fluid.getFluid());
        return tintColor;
    }

    //grabs world/ fluid stack dependent tint color if fluid has associated forge fluid. overrides normal tint color
    private void refreshSpecialColor(@Nullable BlockAndTintGetter world, @Nullable BlockPos pos) {
        //yay hardcoding
        //at least this works for any fluid
        CompoundTag fluidTag = this.fluid.getTag();
        if (fluidTag != null && fluidTag.contains("color")) {
            this.specialColor = fluidTag.getInt("color");
        }
        if (fluid.is(BuiltInSoftFluids.POTION.get())) {
            this.specialColor = PotionNBTHelper.getColorFromNBT(fluidTag);
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
                        w = prop.getTintColor(f.defaultFluidState(), world, pos);
                        if (w != -1) this.specialColor = w;
                    }
                }
            }
        }
    }


}

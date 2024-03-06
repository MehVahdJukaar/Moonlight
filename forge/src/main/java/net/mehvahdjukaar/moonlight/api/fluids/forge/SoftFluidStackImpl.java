package net.mehvahdjukaar.moonlight.api.fluids.forge;

import net.mehvahdjukaar.moonlight.api.fluids.SoftFluid;
import net.mehvahdjukaar.moonlight.api.fluids.SoftFluidStack;
import net.mehvahdjukaar.moonlight.api.util.PotionNBTHelper;
import net.mehvahdjukaar.moonlight.api.util.Utils;
import net.minecraft.core.Holder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraftforge.fluids.FluidStack;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class SoftFluidStackImpl extends SoftFluidStack {

    public SoftFluidStackImpl(Holder<SoftFluid> fluid, int count, CompoundTag tag) {
        super(fluid, count, tag);
    }

    public static SoftFluidStack of(Holder<SoftFluid> fluid, int count, @Nullable CompoundTag tag) {
        return new SoftFluidStackImpl(fluid, count, tag);
    }

    public boolean isFluidEqual(FluidStack fluidStack) {
        return this.isFluidEqual(SoftFluidStackImpl.fromForgeFluid(fluidStack));
    }

    /**
     * gets the equivalent forge fluid without draining the tank. returned stack might be empty
     *
     * @return forge fluid stacks
     */
    public FluidStack toForgeFluid() {
        FluidStack stack = new FluidStack(this.fluid().getVanillaFluid(), bottlesToMB(this.getCount()));

        // tag stuff
        List<String> nbtKey = this.fluid().getNbtKeyFromItem();
        CompoundTag tag = this.getTag();
        if (tag != null && !tag.isEmpty() && !stack.isEmpty() && nbtKey != null) {
            CompoundTag newCom = new CompoundTag();
            for (String k : nbtKey) {
                //special case to convert to IE pot fluid
                if (k.equals(PotionNBTHelper.POTION_TYPE_KEY) && Utils.getID(stack.getFluid()).getNamespace().equals("immersiveengineering")) {
                    continue;
                }
                Tag c = tag.get(k);
                if (c != null) {
                    newCom.put(k, c);
                }
            }
            if (!newCom.isEmpty()) stack.setTag(newCom);
        }
        return stack;
    }

    public static SoftFluidStack fromForgeFluid(FluidStack fluidStack) {
        int amount = MBtoBottles(fluidStack.getAmount());
        return SoftFluidStack.fromFluid(fluidStack.getFluid(), amount,
                fluidStack.hasTag() ? fluidStack.getTag().copy() : null);
    }

    public static int bottlesToMB(int bottles) {
        return bottles * 250;
    }

    public static int MBtoBottles(int milliBuckets) {
        return (int) (milliBuckets / 250f);
    }

}

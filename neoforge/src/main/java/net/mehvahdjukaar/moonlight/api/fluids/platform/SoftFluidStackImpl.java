package net.mehvahdjukaar.moonlight.api.fluids.platform;

import com.mojang.serialization.JavaOps;
import net.mehvahdjukaar.moonlight.api.MoonlightRegistry;
import net.mehvahdjukaar.moonlight.api.fluids.MLBuiltinSoftFluids;
import net.mehvahdjukaar.moonlight.api.fluids.SoftFluid;
import net.mehvahdjukaar.moonlight.api.fluids.SoftFluidStack;
import net.mehvahdjukaar.moonlight.api.misc.OptRegSupplier;
import net.mehvahdjukaar.moonlight.api.util.PotionBottleType;
import net.mehvahdjukaar.moonlight.api.util.Utils;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.StringRepresentable;
import net.neoforged.neoforge.fluids.FluidStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SoftFluidStackImpl extends SoftFluidStack {

    public SoftFluidStackImpl(Holder<SoftFluid> fluid, int count, DataComponentPatch comp) {
        super(fluid, count, comp);
    }

    public static SoftFluidStack of(Holder<SoftFluid> fluid, int count, @NotNull DataComponentPatch components) {
        return new SoftFluidStackImpl(fluid, count, components);
    }

    public boolean isFluidEqual(FluidStack fluidStack, HolderLookup.Provider ra) {
        return this.isSameFluidSameComponents(SoftFluidStackImpl.fromForgeFluid(fluidStack, ra));
    }

    @Deprecated(forRemoval = true)
    public boolean isFluidEqual(FluidStack fluidStack) {
        return this.isSameFluidSameComponents(SoftFluidStackImpl.fromForgeFluid(fluidStack, Utils.hackyGetRegistryAccess()));
    }

    public static FluidStack toForgeFluid(SoftFluidStack softFluid) {
        FluidStack stack = new FluidStack(softFluid.fluid().getVanillaFluid(), bottlesToMB(softFluid.getCount()));
        if (!stack.isEmpty()) {
            softFluid.copyComponentsTo(stack);
            if (MLBuiltinSoftFluids.POTION.is(softFluid.getHolder())) {
                //create always sets its bottle type, so ours has to go or the stacks wont match
                boolean wroteCreateBottle = writeCreateBottleType(stack, PotionBottleType.getOrDefault(softFluid));
                if (wroteCreateBottle) stack.remove(MoonlightRegistry.BOTTLE_TYPE.get());
            }
        }
        return stack;
    }

    /**
     * gets the equivalent forge fluid without draining the tank. returned stack might be empty
     *
     * @return forge fluid stacks
     */
    public FluidStack toForgeFluid() {
        return toForgeFluid(this);
    }

    @Deprecated(forRemoval = true)
    public static SoftFluidStack fromForgeFluid(FluidStack fluidStack) {
        return fromForgeFluid(fluidStack, Utils.hackyGetRegistryAccess());
    }

    public static int bottlesToMB(int bottles) {
        return bottles * 250;
    }

    public static int MBtoBottles(int milliBuckets) {
        return (int) (milliBuckets / 250f);
    }

    public static SoftFluidStack fromForgeFluid(FluidStack fluidStack, HolderLookup.Provider ra) {
        int amount = MBtoBottles(fluidStack.getAmount());
        SoftFluidStack sf = SoftFluidStack.fromFluid(fluidStack.getFluid(), amount, ra);
        if (sf.isEmpty()) return sf;
        SoftFluidStack.copyComponentsTo(fluidStack, sf, sf.fluid().getPreservedComponents());
        PotionBottleType bottle = readCreateBottleType(fluidStack);
        if (bottle != null) sf.set(MoonlightRegistry.BOTTLE_TYPE.get(), bottle);
        return sf;
    }

    private static final OptRegSupplier<DataComponentType<?>> CREATE_BOTTLE_TYPE = OptRegSupplier.of(
            ResourceLocation.fromNamespaceAndPath("create","potion_fluid_bottle_type"),
            Registries.DATA_COMPONENT_TYPE);

    @Nullable
    private static PotionBottleType readCreateBottleType(FluidStack stack) {
        var type = CREATE_BOTTLE_TYPE.get();
        if (type == null) return null;
        if (stack.get(type) instanceof StringRepresentable s) {
            return PotionBottleType.CODEC.parse(JavaOps.INSTANCE, s.getSerializedName()).result().orElse(null);
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private static <T> boolean writeCreateBottleType(FluidStack stack, PotionBottleType bottle) {
        var type = (DataComponentType<T>) CREATE_BOTTLE_TYPE.get();
        if (type == null || type.codec() == null) return false;
        var createBottle = type.codec().parse(JavaOps.INSTANCE, bottle.getSerializedName()).result();
        if (createBottle.isEmpty()) return false;
        stack.set(type, createBottle.get());
        return true;
    }


}

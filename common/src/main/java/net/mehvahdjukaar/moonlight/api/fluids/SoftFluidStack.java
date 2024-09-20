package net.mehvahdjukaar.moonlight.api.fluids;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.architectury.injectables.annotations.ExpectPlatform;
import net.mehvahdjukaar.moonlight.api.MoonlightRegistry;
import net.mehvahdjukaar.moonlight.api.platform.PlatHelper;
import net.mehvahdjukaar.moonlight.api.util.PotionBottleType;
import net.mehvahdjukaar.moonlight.core.fluid.SoftFluidInternal;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderSet;
import net.minecraft.core.component.*;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

// do NOT have these in a static field as they contain registry holders
public class SoftFluidStack implements DataComponentHolder {

    public static final Codec<SoftFluidStack> CODEC = RecordCodecBuilder.create(i -> i.group(
            SoftFluid.HOLDER_CODEC.fieldOf("id").forGetter(SoftFluidStack::getHolder),
            ExtraCodecs.NON_NEGATIVE_INT.optionalFieldOf("count", 1).forGetter(SoftFluidStack::getCount),
            DataComponentPatch.CODEC.optionalFieldOf("components", DataComponentPatch.EMPTY)
                    .forGetter(stack -> stack.components.asPatch())
    ).apply(i, SoftFluidStack::of));

    public static final StreamCodec<RegistryFriendlyByteBuf, SoftFluidStack> STREAM_CODEC = StreamCodec.composite(
            SoftFluid.STREAM_CODEC, SoftFluidStack::getFluid,
            ByteBufCodecs.VAR_INT, SoftFluidStack::getCount,
            DataComponentPatch.STREAM_CODEC, s -> s.components.asPatch(),
            SoftFluidStack::of
    );

    // this is not a singleton. Many empty instances might exist (due to world reload). We keep this just as a minor optimization
    private static SoftFluidStack cachedEmptyInstance = null;

    // dont access directly
    private final Holder<SoftFluid> fluidHolder;
    private final SoftFluid fluid; //reference to avoid calling value all the times. these 2 should always match
    private int count;
    private final PatchedDataComponentMap components;
    private boolean isEmptyCache;

    protected SoftFluidStack(Holder<SoftFluid> fluid, int count, DataComponentPatch components) {
        this.fluidHolder = fluid;
        this.fluid = this.fluidHolder.value();
        this.components = PatchedDataComponentMap.fromPatch(DataComponentMap.EMPTY, components);
        this.setCount(count);
    }

    @ExpectPlatform
    public static SoftFluidStack of(Holder<SoftFluid> fluid, int count, @NotNull DataComponentPatch tag) {
        throw new AssertionError();
    }

    public static SoftFluidStack of(Holder<SoftFluid> fluid, int count) {
        return of(fluid, count, DataComponentPatch.EMPTY);
    }

    public static SoftFluidStack of(Holder<SoftFluid> fluid) {
        return of(fluid, 1);
    }

    public static SoftFluidStack bucket(Holder<SoftFluid> fluid) {
        return of(fluid, SoftFluid.BUCKET_COUNT);
    }

    public static SoftFluidStack bowl(Holder<SoftFluid> fluid) {
        return of(fluid, SoftFluid.BOWL_COUNT);
    }

    public static SoftFluidStack bottle(Holder<SoftFluid> fluid) {
        return of(fluid, SoftFluid.BOTTLE_COUNT);
    }

    public static SoftFluidStack fromFluid(Fluid fluid, int amount) {
        return fromFluid(fluid, amount, DataComponentPatch.EMPTY);
    }

    @NotNull
    public static SoftFluidStack fromFluid(Fluid fluid, int amount, DataComponentPatch component) {
        Holder<SoftFluid> f = SoftFluidInternal.FLUID_MAP.get(fluid);
        if (f == null) return empty();
        return of(f, amount, component);
    }

    @NotNull
    public static SoftFluidStack fromFluid(FluidState fluid) {
        if (fluid.is(FluidTags.WATER)) {
            return fromFluid(fluid.getType(), SoftFluid.WATER_BUCKET_COUNT, DataComponentPatch.EMPTY);
        }
        return fromFluid(fluid.getType(), SoftFluid.BUCKET_COUNT, DataComponentPatch.EMPTY);
    }

    public static SoftFluidStack empty() {
        if (cachedEmptyInstance == null) {
            cachedEmptyInstance = of(SoftFluidRegistry.getEmpty(), 0);
        }
        return cachedEmptyInstance;
    }


    @ApiStatus.Internal
    public static void invalidateEmptyInstance() {
        cachedEmptyInstance = null;
    }

    public Tag save(HolderLookup.Provider lookupProvider) {
        return CODEC.encodeStart(lookupProvider.createSerializationContext(NbtOps.INSTANCE), this).getOrThrow();
    }

    public static SoftFluidStack load(HolderLookup.Provider lookupProvider, Tag tag) {
        //TODO: add components backwards compat
        return CODEC.parse(lookupProvider.createSerializationContext(NbtOps.INSTANCE), tag).getOrThrow();
    }

    public boolean is(TagKey<SoftFluid> tag) {
        return getHolder().is(tag);
    }

    public boolean is(SoftFluid fluid) {
        return this.fluid() == fluid;
    }

    public boolean is(Holder<SoftFluid> fluid) {
        return is(fluid.value());
    }

    private Holder<SoftFluid> getFluid() {
        return isEmptyCache ? SoftFluidRegistry.getEmpty() : fluidHolder;
    }

    public final Holder<SoftFluid> getHolder() {
        return isEmptyCache ? SoftFluidRegistry.getEmpty() : fluidHolder;
    }

    public final SoftFluid fluid() {
        return isEmptyCache ? SoftFluidRegistry.empty() : fluid;
    }

    public boolean isEmpty() {
        return isEmptyCache;
    }

    protected void updateEmpty() {
        isEmptyCache = fluid.isEmptyFluid() || count <= 0;
    }

    public int getCount() {
        return isEmptyCache ? 0 : count;
    }

    public void setCount(int count) {
        if (this == cachedEmptyInstance) {
            if (PlatHelper.isDev()) throw new AssertionError();
            return;
        }
        this.count = count;
        updateEmpty();
    }

    public void grow(int amount) {
        setCount(this.count + amount);
    }

    public void shrink(int amount) {
        setCount(this.count - amount);
    }

    public void consume(int amount, @Nullable LivingEntity entity) {
        if (entity == null || !entity.hasInfiniteMaterials()) {
            this.shrink(amount);
        }
    }

    public SoftFluidStack copy() {
        return of(getHolder(), count, components.copy().asPatch());
    }

    public SoftFluidStack copyWithCount(int count) {
        SoftFluidStack stack = this.copy();
        if (!stack.isEmpty()) {
            stack.setCount(count);
        }
        return stack;
    }

    public SoftFluidStack split(int amount) {
        int i = Math.min(amount, this.getCount());
        SoftFluidStack stack = this.copyWithCount(i);
        if (!this.isEmpty()) this.shrink(i);
        return stack;
    }

    /**
     * Checks if the fluids and NBT Tags are equal. This does not check amounts.
     */
    public boolean isSameFluidSameComponents(SoftFluidStack other) {
        if (!this.is(other.getFluid())) {
            return false;
        } else {
            return this.isEmpty() && other.isEmpty() || Objects.equals(this.components, other.components);
        }
    }

    /**
     * Hashes the fluid and components of this stack, ignoring the amount.
     */
    public static int hashFluidAndComponents(@Nullable SoftFluidStack stack) {
        if (stack != null) {
            int i = 31 + stack.getFluid().hashCode();
            return 31 * i + stack.getComponents().hashCode();
        } else {
            return 0;
        }
    }

    @Override
    public String toString() {
        return this.getCount() + " " + this.getFluid();
    }


    // item conversion

    @Nullable
    public static Pair<SoftFluidStack, FluidContainerList.Category> fromItem(ItemStack itemStack) {
        Item filledContainer = itemStack.getItem();
        Holder<SoftFluid> fluid = SoftFluidInternal.ITEM_MAP.get(filledContainer);

        if (fluid != null && !fluid.value().isEmptyFluid()) {
            var category = fluid.value().getContainerList()
                    .getCategoryFromFilled(filledContainer);

            if (category.isPresent()) {

                int count = category.get().getAmount();

                DataComponentPatch.Builder fluidComponents = DataComponentPatch.builder();

                //convert potions to water bottles
                PotionContents potion = itemStack.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY);
                if (potion.is(Potions.WATER)) {
                    fluid = BuiltInSoftFluids.WATER;
                }
                //add tags to splash and lingering potions
                else if (potion.hasEffects()) {
                    PotionBottleType bottleType = PotionBottleType.getOrDefault(filledContainer);
                    fluidComponents.set(MoonlightRegistry.BOTTLE_TYPE.get(), bottleType);
                }
                SoftFluidStack sfStack = SoftFluidStack.of(fluid, count, fluidComponents.build());

                copyComponentsTo(itemStack, sfStack, fluid.value().getPreservedComponents());

                return Pair.of(sfStack, category.get());
            }
        }
        return null;
    }


    /**
     * Fills the item if possible. Returns empty stack if it fails
     *
     * @param emptyContainer  empty bottle item
     * @param dontModifyStack if the stack count actually is decremented.
     * @return null if it fails, filled stack otherwise
     */
    @Nullable
    public Pair<ItemStack, FluidContainerList.Category> toItem(ItemStack emptyContainer, boolean dontModifyStack) {
        var opt = fluid().getContainerList().getCategoryFromEmpty(emptyContainer.getItem());
        if (opt.isPresent()) {
            var category = opt.get();
            int shrinkAmount = category.getAmount();
            if (shrinkAmount <= this.getCount()) {

                ItemStack filledStack = new ItemStack(category.getFirstFilled().get());

                //converts water bottles into potions
                if (emptyContainer.is(Items.GLASS_BOTTLE) && this.is(BuiltInSoftFluids.WATER)) {
                    filledStack = PotionContents.createItemStack(Items.POTION, Potions.WATER);
                }

                this.copyComponentsTo(filledStack);

                if (!dontModifyStack) this.shrink(shrinkAmount);

                return Pair.of(filledStack, category);
            }
        }
        return null;
    }

    public void copyComponentsTo(DataComponentHolder to) {
        copyComponentsTo(this, to, this.fluid.getPreservedComponents());
    }

    //handles special nbt items such as potions or soups
    protected static void copyComponentsTo(DataComponentHolder from,
                                           DataComponentHolder to,
                                           HolderSet<DataComponentType<?>> types) {
        for (Holder<DataComponentType<?>> h : types) {
            //ignores bottle tag, handled separately since it's a diff item
            DataComponentType<?> type = h.value();
            copyComponentTo(from, to, type);
        }
    }

    private static <A> void copyComponentTo(DataComponentHolder from, DataComponentHolder to, DataComponentType<A> comp) {
        var componentValue = from.get(comp);
        if (componentValue != null) {
            if (to instanceof ItemStack is)
                is.set(comp, componentValue);
            else if (to instanceof SoftFluidStack sf)
                sf.set(comp, componentValue);
            else PlatHelper.setComponent(to, comp, componentValue);
        }
    }


    // fluid delegates

    public FluidContainerList getContainerList() {
        return fluid().getContainerList();
    }

    public FoodProvider getFoodProvider() {
        return fluid().getFoodProvider();
    }

    public boolean isEquivalent(Holder<Fluid> fluid) {
        return this.isEquivalent(fluid, DataComponentPatch.EMPTY);
    }

    public boolean isEquivalent(Holder<Fluid> fluid, DataComponentPatch componentPatch) {
        return this.fluid().isEquivalent(fluid) && Objects.equals(this.components.asPatch(), componentPatch);
    }

    public Holder<Fluid> getVanillaFluid() {
        return this.fluid().getVanillaFluid();
    }

    /**
     * Client only
     *
     * @return tint color to be applied on the fluid texture
     */
    public int getStillColor(@Nullable BlockAndTintGetter world, @Nullable BlockPos pos) {
        SoftFluid fluid = this.fluid();
        SoftFluid.TintMethod method = fluid.getTintMethod();
        if (method == SoftFluid.TintMethod.NO_TINT) return -1;
        int specialColor = SoftFluidColors.getSpecialColor(this, world, pos);

        if (specialColor != 0) return specialColor;
        return fluid.getTintColor();
    }

    /**
     * Client only
     *
     * @return tint color to be applied on the fluid texture
     */
    public int getFlowingColor(@Nullable BlockAndTintGetter world, @Nullable BlockPos pos) {
        SoftFluid.TintMethod method = this.fluid().getTintMethod();
        if (method == SoftFluid.TintMethod.FLOWING) return this.getParticleColor(world, pos);
        else return this.getStillColor(world, pos);
    }

    /**
     * Client only
     *
     * @return tint color to be used on particle. Differs from getTintColor since it returns an mixWith color extrapolated from their fluid textures
     */
    public int getParticleColor(@Nullable BlockAndTintGetter world, @Nullable BlockPos pos) {
        int tintColor = getStillColor(world, pos);
        //if tint color is white gets averaged color
        if (tintColor == -1) return this.fluid().getAverageTextureTintColor();
        return tintColor;
    }

    @Override
    public PatchedDataComponentMap getComponents() {
        return this.components;
    }

    @Nullable
    public <T> T set(DataComponentType<? super T> type, @Nullable T component) {
        return this.components.set(type, component);
    }
}

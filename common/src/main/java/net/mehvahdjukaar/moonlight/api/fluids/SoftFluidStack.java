package net.mehvahdjukaar.moonlight.api.fluids;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.architectury.injectables.annotations.ExpectPlatform;
import net.mehvahdjukaar.moonlight.api.platform.PlatHelper;
import net.mehvahdjukaar.moonlight.api.util.PotionNBTHelper;
import net.mehvahdjukaar.moonlight.api.util.Utils;
import net.mehvahdjukaar.moonlight.core.fluid.SoftFluidInternal;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.ExtraCodecs;
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

import java.util.List;
import java.util.Objects;

// do NOT have these in a static field as they contain registry holders
public class SoftFluidStack implements DataComponentHolder {

    public static final Codec<SoftFluidStack> CODEC = RecordCodecBuilder.create(i -> i.group(
            SoftFluid.HOLDER_CODEC.fieldOf("id").forGetter(SoftFluidStack::getHolder),
            ExtraCodecs.POSITIVE_INT.optionalFieldOf("count", 1).forGetter(SoftFluidStack::getCount),
            DataComponentPatch.CODEC.optionalFieldOf("components", DataComponentPatch.EMPTY)
                    .forGetter(stack -> stack.components.asPatch())
    ).apply(i, SoftFluidStack::of));

    // this is not a singleton. Many empty instances might exist. We keep this just as a minor optimization
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

        //even more hardcoded shit
        if (fluid.is(BuiltInSoftFluids.POTION.getID())) {
            if (this.tag == null || PotionNBTHelper.getPotionType(this.tag) == null) {
                PotionNBTHelper.Type.REGULAR.applyToTag(this.getOrCreateTag());
            }
        }
    }

    protected SoftFluidStack(Holder<SoftFluid> fluid, int count) {
        this(fluid, count, DataComponentPatch.EMPTY);
    }

    protected SoftFluidStack(Holder<SoftFluid> fluid) {
        this(fluid, 1);
    }

    @ExpectPlatform
    public static SoftFluidStack of(Holder<SoftFluid> fluid, int count, DataComponentPatch tag) {
        throw new AssertionError();
    }

    public static SoftFluidStack of(Holder<SoftFluid> fluid, int count) {
        return of(fluid, count, null);
    }

    public static SoftFluidStack of(Holder<SoftFluid> fluid) {
        return of(fluid, 1, null);
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

    public static SoftFluidStack empty() {
        if (cachedEmptyInstance == null) {
            cachedEmptyInstance = of(SoftFluidRegistry.getEmpty(), 0, null);
        }
        return cachedEmptyInstance;
    }

    @ApiStatus.Internal
    public static void invalidateEmptyInstance() {
        cachedEmptyInstance = null;
    }

    public CompoundTag save(CompoundTag compoundTag) {
        compoundTag.putString("id", getHolder().unwrapKey().get().location().toString());
        compoundTag.putByte("count", (byte) this.count);
        if (this.tag != null) {
            compoundTag.put("tag", this.tag.copy());
        }
        return compoundTag;
    }

    public static SoftFluidStack load(HolderLookup.Provider lookupProvider, CompoundTag tag) {

        //TODO: add components backwards compat

        return CODEC.parse(lookupProvider.createSerializationContext(NbtOps.INSTANCE), tag)
                .resultOrPartial(error -> LOGGER.error("Tried to load invalid soft fluid stack: '{}'", error));
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

    public SoftFluidStack copy() {
        return of(getHolder(), count, tag == null ? null : tag.copy());
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
    public boolean isFluidEqual(SoftFluidStack other) {
        return fluid() == other.fluid() && isFluidStackTagEqual(other);
    }

    /**
     * Just checks if nbt is the same
     */
    public boolean isFluidStackTagEqual(SoftFluidStack other) {
        return Objects.equals(this.tag, other.tag);
    }

    // these do not take count into account for some reason
    @Override
    public final int hashCode() {
        int code = 1;
        code = 31 * code + fluid().hashCode();
        if (tag != null)
            code = 31 * code + tag.hashCode();
        return code;
    }

    /**
     * Default equality comparison for a FluidStack. Same functionality as isFluidEqual().
     * <p>
     * This is included for use in data structures.
     */
    @Override
    public final boolean equals(Object o) {
        if (o instanceof SoftFluidStack ss) {
            return isFluidEqual(ss);
        }
        return false;
    }

    @Override
    public String toString() {
        String s = count + " " + getHolder().unwrapKey().get().location();
        if (tag != null) s += " [" + tag + "]";
        return s;
    }

    @NotNull
    public static SoftFluidStack fromFluid(Fluid fluid, int amount, @Nullable CompoundTag tag) {
        Holder<SoftFluid> f = SoftFluidInternal.FLUID_MAP.get(fluid);
        if (f == null) return empty();
        return of(f, amount, tag);
    }

    @NotNull
    public static SoftFluidStack fromFluid(FluidState fluid) {
        if (fluid.is(FluidTags.WATER)) {
            return fromFluid(fluid.getType(), SoftFluid.WATER_BUCKET_COUNT, null);
        }
        return fromFluid(fluid.getType(), SoftFluid.BUCKET_COUNT, null);
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

                CompoundTag fluidTag = new CompoundTag();

                CompoundTag itemTag = itemStack.getTag();

                //convert potions to water bottles
                PotionContents potion = itemStack.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY);
                boolean hasCustomPot = (itemTag != null && itemTag.contains("CustomPotionEffects"));
                if (potion.is(Potions.WATER) && !hasCustomPot) {
                    fluid = BuiltInSoftFluids.WATER.getHolder();
                }
                //add tags to splash and lingering potions
                else if (potion != PotionContents.EMPTY || hasCustomPot) {
                    PotionNBTHelper.Type type = PotionNBTHelper.getPotionType(filledContainer);
                    if (type == null) type = PotionNBTHelper.Type.REGULAR;
                    type.applyToTag(fluidTag);
                }

                //copy nbt from item
                if (itemTag != null) {
                    for (String k : fluid.value().getNbtKeyFromItem()) {
                        Tag c = itemTag.get(k);
                        if (c != null) {
                            fluidTag.put(k, c);
                        }
                    }
                }

                if (fluidTag.isEmpty()) fluidTag = null;

                return Pair.of(SoftFluidStack.of(fluid, count, fluidTag), category.get());
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
                //case for lingering potions
                if (this.is(BuiltInSoftFluids.POTION.getHolder()) && this.tag != null) {
                    var type = PotionNBTHelper.getPotionType(this.tag);
                    if (type != null && !Utils.getID(emptyContainer.getItem()).getNamespace().equals("inspirations")) {
                        if (type != PotionNBTHelper.Type.REGULAR) {
                            filledStack = type.getDefaultItem();
                        }
                    }
                }

                //converts water bottles into potions
                if (emptyContainer.is(Items.GLASS_BOTTLE) && this.is(BuiltInSoftFluids.WATER.get())) {
                    filledStack = PotionContents.createItemStack(Items.POTION, Potions.WATER);
                }

                this.applyNBTtoItemStack(filledStack);

                if (!dontModifyStack) this.shrink(shrinkAmount);

                return Pair.of(filledStack, category);
            }
        }
        return null;
    }

    //TODO: clean this nbt hardcoded stuff up

    //handles special nbt items such as potions or soups
    protected void applyNBTtoItemStack(ItemStack stack) {
        List<String> nbtKey = this.fluid().getNbtKeyFromItem();
        if (this.tag != null && !this.tag.isEmpty()) {
            CompoundTag newCom = new CompoundTag();
            for (String s : nbtKey) {
                //ignores bottle tag, handled separately since it's a diff item
                Tag c = this.tag.get(s);
                if (c != null && !s.equals(PotionNBTHelper.POTION_TYPE_KEY)) {
                    newCom.put(s, c);
                }
            }
            if (!newCom.isEmpty()) stack.setTag(newCom);
        }
    }


    // fluid delegates

    public FluidContainerList getContainerList() {
        return fluid().getContainerList();
    }

    public FoodProvider getFoodProvider() {
        return fluid().getFoodProvider();
    }

    public boolean isEquivalent(Fluid fluid) {
        return this.fluid().isEquivalent(fluid);
    }

    public Fluid getVanillaFluid() {
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
    public DataComponentMap getComponents() {
        return this.isEmpty() ? DataComponentMap.EMPTY : this.components;
    }
}

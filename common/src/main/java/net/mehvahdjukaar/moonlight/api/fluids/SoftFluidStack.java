package net.mehvahdjukaar.moonlight.api.fluids;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.mehvahdjukaar.moonlight.api.platform.PlatHelper;
import net.mehvahdjukaar.moonlight.api.util.PotionNBTHelper;
import net.mehvahdjukaar.moonlight.api.util.Utils;
import net.minecraft.core.Holder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.levelgen.structure.templatesystem.BlockMatchTest;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;

// do NOT have these in a static field as they contain registry holders
public class SoftFluidStack {

    public static final Codec<SoftFluidStack> CODEC = RecordCodecBuilder.create(i -> i.group(
            SoftFluid.HOLDER_CODEC.fieldOf("id").forGetter(SoftFluidStack::getFluid),
            Codec.INT.optionalFieldOf("count", 1).forGetter(SoftFluidStack::getCount),
            CompoundTag.CODEC.optionalFieldOf("tag", null).forGetter(SoftFluidStack::getTag)
    ).apply(i, SoftFluidStack::new));

    // this is not a singleton. Many empty instances might exist. We keep this just as a minor optimization
    private static SoftFluidStack cachedEmptyInstance = null;

    // dont access directly
    private final Holder<SoftFluid> fluid;
    private int count;
    private CompoundTag tag;
    private boolean isEmptyCache;


    public SoftFluidStack(Holder<SoftFluid> fluid, int count, CompoundTag tag) {
        this.fluid = fluid;
        this.tag = tag;
        this.setCount(count);

        //even more hardcoded shit
        if (fluid.is(BuiltInSoftFluids.POTION.getID())) {
            if (this.tag == null || PotionNBTHelper.getPotionType(this.tag) == null) {
                PotionNBTHelper.Type.REGULAR.applyToTag(this.getOrCreateTag());
            }
        }
    }

    public SoftFluidStack(Holder<SoftFluid> fluid, int count) {
        this(fluid, count, null);
    }

    public SoftFluidStack(Holder<SoftFluid> fluid) {
        this(fluid, 1, null);
    }

    public SoftFluidStack bucket(Holder<SoftFluid> fluid) {
        return new SoftFluidStack(fluid, SoftFluid.BUCKET_COUNT);
    }

    public SoftFluidStack bowl(Holder<SoftFluid> fluid) {
        return new SoftFluidStack(fluid, SoftFluid.BOWL_COUNT);
    }

    public SoftFluidStack bottle(Holder<SoftFluid> fluid) {
        return new SoftFluidStack(fluid, SoftFluid.BOTTLE_COUNT);
    }

    public static SoftFluidStack empty() {
        if (cachedEmptyInstance == null) {
            cachedEmptyInstance = new SoftFluidStack(SoftFluidRegistry.getEmpty(), 0, null);
        }
        return cachedEmptyInstance;
    }

    @ApiStatus.Internal
    public static void invalidateEmptyInstance() {
        cachedEmptyInstance = null;
    }

    public CompoundTag save(CompoundTag compoundTag) {
        compoundTag.putString("id", getFluid().unwrapKey().get().location().toString());
        compoundTag.putByte("count", (byte) this.count);
        if (this.tag != null) {
            compoundTag.put("tag", this.tag.copy());
        }
        return compoundTag;
    }

    public static SoftFluidStack load(CompoundTag tag) {
        //backwards compat
        if (tag.contains("Fluid")) {
            tag.putString("id", tag.getString("Fluid"));
            tag.remove("Fluid");
        }
        if (tag.contains("NBT")) {
            tag.put("tag", tag.get("NBT"));
            tag.remove("NBT");
        }
        if (tag.contains("Count")) {
            tag.putByte("count", (byte) tag.getInt("Count"));
            tag.remove("count");
        }

        var fluid = SoftFluidRegistry.getHolder(new ResourceLocation(tag.getString("id")));
        var amount = tag.getByte("count");
        CompoundTag nbt = null;
        if (tag.contains("tag", 10)) {
            nbt = tag.getCompound("tag");
        }
        return new SoftFluidStack(fluid, amount, nbt);
    }

    public boolean is(TagKey<SoftFluid> tag) {
        return getFluid().is(tag);
    }

    public boolean is(SoftFluid fluid) {
        return this.getFluid().value() == fluid;
    }

    public boolean is(Holder<SoftFluid> fluid) {
        return is(fluid.value());
    }

    public final Holder<SoftFluid> getFluid() {
        return isEmptyCache ? SoftFluidRegistry.getEmpty() : fluid;
    }

    public boolean isEmpty() {
        return isEmptyCache;
    }

    protected void updateEmpty() {
        isEmptyCache = fluid.value().isEmpty() || count <= 0;
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

    public boolean hasTag() {
        return tag != null;
    }

    public CompoundTag getTag() {
        return tag;
    }

    public void setTag(CompoundTag tag) {
        if (this == cachedEmptyInstance) {
            if (PlatHelper.isDev()) throw new AssertionError();
            return;
        }
        this.tag = tag;
    }

    public CompoundTag getOrCreateTag() {
        if (tag == null) setTag(new CompoundTag());
        return tag;
    }

    public CompoundTag getOrCreateTagElement(String key) {
        if (this.tag != null && this.tag.contains(key, 10)) {
            return this.tag.getCompound(key);
        } else {
            CompoundTag compoundTag = new CompoundTag();
            this.addTagElement(key, compoundTag);
            return compoundTag;
        }
    }

    public void addTagElement(String key, Tag tag) {
        this.getOrCreateTag().put(key, tag);
    }

    public SoftFluidStack copy() {
        return new SoftFluidStack(getFluid(), count, tag == null ? null : tag.copy());
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
        return getFluid() == other.getFluid() && isFluidStackTagEqual(other);
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
        code = 31 * code + getFluid().hashCode();
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
        return count + " " + getFluid().unwrapKey().get().location() + " [" + tag.toString() + "]";
    }

    public static SoftFluidStack fromFluid(Fluid fluid, int amount, @Nullable CompoundTag tag) {
        Holder<SoftFluid> f = SoftFluidRegistry.FLUID_MAP.get(fluid);
        if (f == null) return null;
        return new SoftFluidStack(f, amount, tag);
    }

    public static SoftFluidStack fromFluid(FluidState fluid) {
        if (fluid.getType().is(FluidTags.WATER)) {
            return fromFluid(fluid.getType(), SoftFluid.WATER_BUCKET_COUNT, null);
        }
        return fromFluid(fluid.getType(), SoftFluid.BUCKET_COUNT, null);
    }


    // item conversion

    @Nullable
    public static Pair<SoftFluidStack, FluidContainerList.Category> fromItem(ItemStack itemStack) {
        Item filledContainer = itemStack.getItem();
        Holder<SoftFluid> fluid = SoftFluidRegistry.ITEM_MAP.get(filledContainer);

        if (fluid != null && !fluid.value().isEmpty()) {
            var category = fluid.value().getContainerList()
                    .getCategoryFromFilled(filledContainer);

            if (category.isPresent()) {

                int count = category.get().getAmount();

                CompoundTag fluidTag = new CompoundTag();

                CompoundTag itemTag = itemStack.getTag();

                //convert potions to water bottles
                Potion potion = PotionUtils.getPotion(itemStack);
                boolean hasCustomPot = (itemTag != null && itemTag.contains("CustomPotionEffects"));
                if (potion == Potions.WATER && !hasCustomPot) {
                    fluid = BuiltInSoftFluids.WATER.getHolder();
                }
                //add tags to splash and lingering potions
                else if (potion != Potions.EMPTY || hasCustomPot) {
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

                return Pair.of(new SoftFluidStack(fluid, count, fluidTag), category.get());
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
        var opt = getFluid().value().getContainerList().getCategoryFromEmpty(emptyContainer.getItem());
        if (opt.isPresent()) {
            var category = opt.get();
            int shrinkAmount = category.getAmount();
            if (shrinkAmount <= this.getCount()) {

                ItemStack filledStack = new ItemStack(category.getFirstFilled().get());
                //case for lingering potions
                if (this.getFluid().is(BuiltInSoftFluids.POTION.getID()) && this.tag != null) {
                    var type = PotionNBTHelper.getPotionType(this.tag);
                    if (type != null && !Utils.getID(emptyContainer.getItem()).getNamespace().equals("inspirations")) {
                        if (type != PotionNBTHelper.Type.REGULAR) {
                            filledStack = type.getDefaultItem();
                        }
                    }
                }

                //converts water bottles into potions
                if (emptyContainer.is(Items.GLASS_BOTTLE) && getFluid().value() == BuiltInSoftFluids.WATER.get()) {
                    filledStack = PotionUtils.setPotion(new ItemStack(Items.POTION), Potions.WATER);
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
        List<String> nbtKey = this.getFluid().value().getNbtKeyFromItem();
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
        return getFluid().value().getContainerList();
    }

    public FoodProvider getFoodProvider() {
        return getFluid().value().getFoodProvider();
    }

    public boolean isEquivalent(Fluid fluid) {
        return this.getFluid().value().isEquivalent(fluid);
    }

    public Fluid getVanillaFluid() {
        return this.getFluid().value().getVanillaFluid();
    }

    public SoftFluid.TintMethod getTintMethod() {
        return this.getFluid().value().getTintMethod();
    }

}

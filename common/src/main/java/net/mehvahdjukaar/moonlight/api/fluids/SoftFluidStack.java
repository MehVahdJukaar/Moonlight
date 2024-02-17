package net.mehvahdjukaar.moonlight.api.fluids;

import com.mojang.datafixers.util.Pair;
import net.mehvahdjukaar.moonlight.api.util.Utils;
import net.minecraft.core.Holder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.*;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import org.jetbrains.annotations.Nullable;

import java.util.List;

// do NOT have these in a static field as they contain registry holders
public class SoftFluidStack {

    public static final String POTION_TYPE_KEY = "Bottle";

    private final Holder<SoftFluid> fluid;
    private int count;
    private CompoundTag tag;
    private boolean isEmptyCache;

    public SoftFluidStack(Holder<SoftFluid> fluid, int count, CompoundTag tag) {
        this.fluid = fluid;
        this.count = count;
        this.tag = tag;
        updateEmpty();

        //even more hardcoded shit
        if (fluid.is(BuiltInSoftFluids.POTION.getID())) {
            if (this.tag == null || !this.tag.contains(POTION_TYPE_KEY)) {
                this.getOrCreateTag().putString(POTION_TYPE_KEY, "REGULAR");
            }
        }
    }

    public SoftFluidStack(Holder<SoftFluid> fluid, int count) {
        this(fluid, count, null);
    }

    public SoftFluidStack(Holder<SoftFluid> fluid) {
        this(fluid, 1, null);
    }

    public static SoftFluidStack empty() {
        return new SoftFluidStack(SoftFluidRegistry.getEmpty(), 0, null);
    }

    public CompoundTag save(CompoundTag compoundTag) {
        compoundTag.putString("id", fluid.unwrapKey().get().location().toString());
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
        }
        if (tag.contains("NBT")) {
            tag.putString("tag", tag.getString("NBT"));
        }
        if (tag.contains("Count")) {
            tag.putString("count", tag.getString("Count"));
        }

        var fluid = SoftFluidRegistry.getHolder(new ResourceLocation(tag.getString("id")));
        var amount = tag.getByte("count");
        if (tag.contains("tag", 10)) {
            tag = tag.getCompound("tag");
        }
        return new SoftFluidStack(fluid, amount, tag);
    }

    public boolean is(TagKey<SoftFluid> tag) {
        return fluid.is(tag);
    }

    public boolean is(SoftFluid fluid) {
        return this.fluid.value() == fluid;
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
        return new SoftFluidStack(getFluid(), count, tag.copy());
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
        return tag == null ? other.tag == null : other.tag != null && tag.equals(other.tag);
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


    // fluid delegates

    public FluidContainerList getContainerList() {
        return getFluid().value().getContainerList();
    }

    public FoodProvider getFoodProvider() {
        return getFluid().value().getFoodProvider();
    }


    // item conversion

    @Nullable
    public static Pair<SoftFluidStack, FluidContainerList.Category> fromItem(ItemStack itemStack) {
        Item filledContainer = itemStack.getItem();
        Holder<SoftFluid> fluid = SoftFluidRegistry.fromItem(filledContainer);

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
                    applyPotionNBT(filledContainer, fluidTag);
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
     * @param emptyContainer empty bottle item
     * @param modifyStack    if the stack count actually is decremented.
     * @return null if it fails, filled stack otherwise
     */
    @Nullable
    public Pair<ItemStack, FluidContainerList.Category> toItem(ItemStack emptyContainer, boolean modifyStack) {
        var opt = fluid.value().getContainerList().getCategoryFromEmpty(emptyContainer.getItem());
        if (opt.isPresent()) {
            var category = opt.get();
            int shrinkAmount = category.getAmount();
            if (shrinkAmount <= this.getCount()) {

                ItemStack filledStack = new ItemStack(category.getFirstFilled().get());
                //case for lingering potions
                if (this.fluid.value() == BuiltInSoftFluids.POTION.get()) {
                    if (this.tag != null && this.tag.contains(POTION_TYPE_KEY) && !Utils.getID(emptyContainer).getNamespace().equals("inspirations")) {
                        String bottle = this.tag.getString(POTION_TYPE_KEY);
                        if (bottle.equals("SPLASH")) filledStack = new ItemStack(Items.SPLASH_POTION);
                        else if (bottle.equals("LINGERING")) filledStack = new ItemStack(Items.LINGERING_POTION);
                    }
                }

                //converts water bottles into potions
                if (emptyContainer.is(Items.GLASS_BOTTLE) && fluid.value() == BuiltInSoftFluids.WATER.get()) {
                    filledStack = PotionUtils.setPotion(new ItemStack(Items.POTION), Potions.WATER);
                }

                this.applyNBTtoItemStack(filledStack);

                if(modifyStack) this.shrink(shrinkAmount);

                return Pair.of(filledStack, category);
            }
        }
        return null;
    }

    //TODO: clean this nbt hardcoded stuff up

    //same syntax as merge
    protected static void applyPotionNBT(Item item, CompoundTag com) {
        String type = "REGULAR";
        if (item instanceof SplashPotionItem) type = "SPLASH";
        else if (item instanceof LingeringPotionItem) type = "LINGERING";
        com.putString(POTION_TYPE_KEY, type);
    }

    //handles special nbt items such as potions or soups
    protected void applyNBTtoItemStack(ItemStack stack) {
        List<String> nbtKey = this.fluid.value().getNbtKeyFromItem();
        if (this.tag != null && !this.tag.isEmpty()) {
            CompoundTag newCom = new CompoundTag();
            for (String s : nbtKey) {
                //ignores bottle tag, handled separately since it's a diff item
                Tag c = this.tag.get(s);
                if (c != null && !s.equals(POTION_TYPE_KEY)) {
                    newCom.put(s, c);
                }
            }
            if (!newCom.isEmpty()) stack.setTag(newCom);
        }
    }


}

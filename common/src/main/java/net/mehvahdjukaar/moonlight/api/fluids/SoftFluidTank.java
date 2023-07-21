package net.mehvahdjukaar.moonlight.api.fluids;

import com.google.common.base.Preconditions;
import dev.architectury.injectables.annotations.ExpectPlatform;
import net.mehvahdjukaar.moonlight.api.util.Utils;
import net.mehvahdjukaar.moonlight.core.Moonlight;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import org.jetbrains.annotations.NotNull;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.util.List;

/**
 * instance this fluid tank in your tile entity
 */
@SuppressWarnings("unused")
public abstract class SoftFluidTank {

    public static final String POTION_TYPE_KEY = "Bottle";
    public static final int BOTTLE_COUNT = 1;
    public static final int BOWL_COUNT = 2;
    public static final int BUCKET_COUNT = 4;

    //count in bottles
    protected int count = 0;
    protected final int capacity;
    @Nullable
    protected CompoundTag nbt = null;
    protected SoftFluid fluid = BuiltInSoftFluids.EMPTY.get(); //not null
    //special tint color. Used for dynamic tint fluids like water and potions
    protected int specialColor = 0;
    protected boolean needsColorRefresh = true;

    protected SoftFluidTank(int capacity) {
        this.capacity = capacity;
    }

    @ExpectPlatform
    public static SoftFluidTank create(int capacity) {
        throw new AssertionError();
    }

    //TODO: add default methods here

    /**
     * call this method from your block when player interacts. tries to fill or empty current held item in tank
     *
     * @param player player
     * @param hand   hand
     * @return interaction successful
     */
    public boolean interactWithPlayer(Player player, InteractionHand hand, @Nullable Level world, @Nullable BlockPos pos) {
        ItemStack handStack = player.getItemInHand(hand);

        ItemStack returnStack = this.interactWithItem(handStack, world, pos, false);
        //for items that have no bottle
        if (returnStack != null) {
            Utils.swapItem(player, hand, returnStack);

            if (!handStack.isEmpty()) player.awardStat(Stats.ITEM_USED.get(handStack.getItem()));
            return true;
        }
        return false;
    }

    /**
     * makes current item interact with fluid tank. returns empty stack if
     *
     * @param stack ItemStack to be interacted with
     * @param world world. null if no sound is to be played
     * @param pos   position. null if no sound is to be played
     * @return resulting ItemStack: empty for empty hand return, null if it failed
     */
    @Nullable
    public ItemStack interactWithItem(ItemStack stack, @Nullable Level world, @Nullable BlockPos pos, boolean simulate) {
        ItemStack returnStack;
        //try filling
        returnStack = this.tryFillingItem(stack.getItem(), world, pos, simulate);
        if (returnStack != null) return returnStack;
        //try emptying
        returnStack = this.tryDrainItem(stack, world, pos, simulate);

        return returnStack;
    }

    public ItemStack tryDrainItem(ItemStack filledContainerStack, @Nullable Level world, @Nullable BlockPos pos, boolean simulate) {
        return tryDrainItem(filledContainerStack, world, pos, simulate, true);
    }

    //TODO: use interaction result holder
    /**
     * tries pouring the content of provided item in the tank
     * also plays sound
     *
     * @return empty container item, null if it failed
     */
    @Nullable
    public ItemStack tryDrainItem(ItemStack filledContainerStack, @Nullable Level world, @Nullable BlockPos pos, boolean simulate, boolean playSound) {

        //TODO: generalize this adding a function list that converts items in compounds and fluid pair or a compound whitelist
        //TODO: all of this  is horrible

        Item filledContainer = filledContainerStack.getItem();

        /*
        if(filledContainer instanceof ISoftFluidContainerItem){
            ISoftFluidContainerItem p = ((ISoftFluidContainerItem) filledContainer);
            ResourceLocation r = p.getSoftFluid();
            SoftFluid s = SoftFluidRegistry.get(r.toString());
            if(!s.isEmpty()) {
                CompoundNBT nbt = p.getFluidNBT();
                int am = p.getAmount();
                if (this.isEmpty()) {
                    this.setFluid(s, nbt);
                }
                if (this.canAddSoftFluid(s, am, nbt)) {
                    this.grow(am);
                    SoundEvent sound = p.getEmptySound();
                    if (sound != null && world != null && pos != null)
                        world.playSound(null, pos, sound, SoundCategory.BLOCKS, 1, 1);
                    return p.getEmptyContainer();
                }
            }
            return null
        }*/


        SoftFluid s = SoftFluidRegistry.fromItem(filledContainer);

        if (s.isEmpty()) return null;

        CompoundTag com = filledContainerStack.getTag();
        CompoundTag newCom = new CompoundTag();

        //convert potions to water bottles
        Potion potion = PotionUtils.getPotion(filledContainerStack);
        boolean hasCustomPot = (com != null && com.contains("CustomPotionEffects"));
        if (potion == Potions.WATER && !hasCustomPot) {
            s = BuiltInSoftFluids.WATER.get();
        }
        //add tags to splash and lingering potions
        else if (potion != Potions.EMPTY || hasCustomPot) {
            addPotionTag(filledContainer, newCom);
        }

        //copy nbt from item
        if (com != null) {
            for (String k : s.getNbtKeyFromItem()) {
                Tag c = com.get(k);
                if (c != null) {
                    newCom.put(k, c);
                }
            }
        }

        //set new fluid if empty
        if (this.isEmpty()) {
            this.setFluid(s, newCom.isEmpty() ? null : newCom);
        }

        var optionalCategory = s.getContainerList().getCategoryFromFilled(filledContainer);

        if (optionalCategory.isPresent()) {
            var category = optionalCategory.get();
            int amount = category.getAmount();
            if (this.canAddSoftFluid(s, amount, newCom)) {
                if (simulate) return ItemStack.EMPTY;
                this.grow(amount);

                SoundEvent sound = category.getEmptySound();
                if (sound != null && world != null && pos != null)
                    world.playSound(null, pos, sound, SoundSource.BLOCKS, 1, 1);

                return new ItemStack(category.getEmptyContainer());
            }
        }
        return null;
    }

    public ItemStack tryFillingItem(Item emptyContainer, @Nullable Level world, @Nullable BlockPos pos, boolean simulate) {
        return tryFillingItem(emptyContainer, world, pos, simulate, true);
    }


    /**
     * tries removing said amount of fluid and returns filled item
     * also plays sound
     *
     * @return filled bottle item. null if it failed or if simulated is true and failed
     */
    @Nullable
    public ItemStack tryFillingItem(Item emptyContainer, @Nullable Level world, @Nullable BlockPos pos, boolean simulate, boolean playSound) {
        var opt = fluid.getContainerList().getCategoryFromEmpty(emptyContainer);
        if (opt.isPresent()) {
            var category = opt.get();
            int amount = category.getAmount();
            if (this.canRemove(amount)) {
                if (simulate) return ItemStack.EMPTY;
                ItemStack stack = new ItemStack(category.getFirstFilled().get());
                //case for lingering potions
                if (this.fluid == BuiltInSoftFluids.POTION.get()) {
                    if (this.nbt != null && this.nbt.contains(POTION_TYPE_KEY) && !Utils.getID(emptyContainer).getNamespace().equals("inspirations")) {
                        String bottle = this.nbt.getString(POTION_TYPE_KEY);
                        if (bottle.equals("SPLASH")) stack = new ItemStack(Items.SPLASH_POTION);
                        else if (bottle.equals("LINGERING")) stack = new ItemStack(Items.LINGERING_POTION);
                    }
                }

                //converts water bottles into potions
                if (emptyContainer == Items.GLASS_BOTTLE && fluid == BuiltInSoftFluids.WATER.get())
                    stack = PotionUtils.setPotion(new ItemStack(Items.POTION), Potions.WATER);

                this.applyNBTtoItemStack(stack);
                this.shrink(amount);

                SoundEvent sound = category.getEmptySound();
                if (sound != null && world != null && pos != null)
                    world.playSound(null, pos, sound, SoundSource.BLOCKS, 1, 1);

                return stack;
            }
        }
        return null;
    }

    /**
     * tries removing bottle amount and returns filled bottle
     *
     * @return filled bottle item. null if it failed
     */
    @Nullable
    public ItemStack tryFillingBottle(Level world, BlockPos pos) {
        return tryFillingItem(Items.GLASS_BOTTLE, world, pos, false);
    }

    /**
     * tries removing bucket amount and returns filled bucket
     *
     * @return filled bucket item. null if it failed
     */
    @Nullable
    public ItemStack tryFillingBucket(Level world, BlockPos pos) {
        return tryFillingItem(Items.BUCKET, world, pos, false);
    }

    /**
     * tries removing bowl amount and returns filled bowl
     *
     * @return filled bowl item. null if it failed
     */
    @Nullable
    public ItemStack tryFillingBowl(Level world, BlockPos pos) {
        return tryFillingItem(Items.BOWL, world, pos, false);
    }

    /**
     * checks if current tank holds equivalent fluid as provided soft fluid
     *
     * @param other soft fluid
     * @return is same
     */
    public boolean isSameFluidAs(SoftFluid other) {
        return isSameFluidAs(other, null);
    }

    /**
     * checks if current tank holds equivalent fluid as provided soft fluid
     *
     * @param other soft fluid
     * @param com   fluid nbt
     * @return is same
     */
    public boolean isSameFluidAs(SoftFluid other, @Nullable CompoundTag com) {
        return this.getFluid().equals(other) && areNbtEquals(this.getNbt(), com);
    }

    /**
     * try adding provided soft fluid to the tank
     *
     * @param s     soft fluid to add
     * @param count count to add
     * @param com   fluid nbt
     * @return success
     */
    public boolean tryAddingFluid(SoftFluid s, int count, @Nullable CompoundTag com) {
        if (this.canAdd(count)) {
            if (this.isEmpty()) {
                this.setFluid(s, com);
                this.setCount(count);
                return true;
            } else if (this.isSameFluidAs(s, com)) {
                this.grow(count);
                return true;
            }
        }
        return false;
    }

    /**
     * try adding provided soft fluid to the tank
     *
     * @param s     soft fluid to add
     * @param count count to add
     * @return success
     */
    public boolean tryAddingFluid(SoftFluid s, int count) {
        return tryAddingFluid(s, count, null);
    }

    /**
     * try adding 1 bottle of provided soft fluid to the tank
     *
     * @param s soft fluid to add
     * @return success
     */
    public boolean tryAddingFluid(SoftFluid s) {
        return this.tryAddingFluid(s, 1);
    }

    /**
     * Transfers between 2 soft fluid tanks
     */
    public boolean tryTransferFluid(SoftFluidTank destination) {
        return this.tryTransferFluid(destination, BOTTLE_COUNT);
    }

    //transfers between two fluid holders
    public boolean tryTransferFluid(SoftFluidTank destination, int amount) {
        if (destination.canAdd(amount) && this.canRemove(amount)) {
            if (destination.isEmpty()) {
                destination.setFluid(this.getFluid(), this.getNbt());
                this.shrink(amount);
                destination.grow(amount);
                return true;
            } else if (this.isSameFluidAs(destination.getFluid(), destination.getNbt())) {
                this.shrink(amount);
                destination.grow(amount);
                return true;
            }
        }
        return false;
    }

    /**
     * can I remove n bottles of fluid
     *
     * @param n bottles amount
     * @return can remove
     */
    public boolean canRemove(int n) {
        return this.count >= n && !this.isEmpty();
    }

    /**
     * can I add n bottles of fluid
     *
     * @param n bottles amount
     * @return can add
     */
    public boolean canAdd(int n) {
        return this.count + n <= this.capacity;
    }

    /**
     * can provide soft fluid be added to tank
     *
     * @param s     soft fluid to add
     * @param count bottles amount
     * @return can add
     */
    public boolean canAddSoftFluid(SoftFluid s, int count) {
        return canAddSoftFluid(s, count, null);
    }

    /**
     * can provide soft fluid be added to tank
     *
     * @param s     soft fluid to add
     * @param count bottles amount
     * @param nbt   soft fluid nbt
     * @return can add
     */
    public boolean canAddSoftFluid(SoftFluid s, int count, @Nullable CompoundTag nbt) {
        return this.canAdd(count) && this.isSameFluidAs(s, nbt);
    }

    public boolean isFull() {
        return this.count == this.capacity;
    }

    public boolean isEmpty() {
        //count 0 should always = to fluid.empty
        return this.fluid.isEmpty() || this.count <= 0;
    }


    /**
     * grows contained fluid by at most inc bottles. doesn't need checking
     *
     * @param inc maximum increment
     */
    public void lossyAdd(int inc) {
        this.count = Math.min(this.capacity, this.count + inc);
    }

    /**
     * unchecked sets the tank fluid count
     *
     * @param count bottles count
     */
    public void setCount(int count) {
        this.count = count;
    }

    /**
     * fills out tank to the maximum capacity
     */
    public void fillCount() {
        this.setCount(capacity);
    }

    /**
     * unchecked grows the tank by inc bottles. Check with canAdd()
     *
     * @param inc bottles increment
     */
    public void grow(int inc) {
        this.setCount((this.count + inc));
    }

    /**
     * unchecked shrinks the tank by inc bottles
     *
     * @param inc bottles increment
     */
    public void shrink(int inc) {
        this.grow(-inc);
        if (this.count == 0) {
            this.clear();
        }
    }

    /**
     * gets liquid height for renderer
     *
     * @param maxHeight maximum height in blocks
     * @return fluid height
     */
    public float getHeight(float maxHeight) {
        return maxHeight * this.count / this.capacity;
    }

    /**
     * @return comparator block redstone power
     */
    public int getComparatorOutput() {
        float f = this.count / (float) this.capacity;
        return Mth.floor(f * 14.0F) + 1;
    }

    public int getCount() {
        return count;
    }

    @NotNull
    public SoftFluid getFluid() {
        return fluid;
    }

    @Nullable
    public CompoundTag getNbt() {
        return nbt;
    }

    public void setNbt(@Nullable CompoundTag nbt) {
        this.nbt = nbt;
    }

    /**
     * resets & clears the tank
     */
    public void clear() {
        this.fluid = BuiltInSoftFluids.EMPTY.get();
        this.setCount(0);
        this.nbt = null;
        this.specialColor = 0;
    }

    /**
     * copies the content of a fluid tank into this
     *
     * @param other other tank
     */
    public void copy(SoftFluidTank other) {
        this.setFluid(other.getFluid(), other.getNbt());
        this.setCount(Math.min(this.capacity, other.getCount()));
    }

    /**
     * fills to max capacity with provided soft fluid
     *
     * @param fluid forge fluid
     */
    public void fill(SoftFluid fluid) {
        this.fill(fluid, null);
    }

    /**
     * fills to max capacity with provided soft fluid
     *
     * @param fluid soft fluid
     * @param nbt   soft fluid nbt
     */
    public void fill(SoftFluid fluid, @Nullable CompoundTag nbt) {
        this.setFluid(fluid, nbt);
        this.fillCount();
    }

    /**
     * sets current fluid to provided soft fluid equivalent
     *
     * @param fluid soft fluid
     */
    public void setFluid(@NotNull SoftFluid fluid) {
        this.setFluid(fluid, null);
    }

    //called when it goes from empty to full
    public void setFluid(@NotNull SoftFluid fluid, @Nullable CompoundTag nbt) {
        Preconditions.checkNotNull(fluid,"Tried to add a null fluid. How?");
        this.fluid = fluid;
        this.nbt = null;
        if (nbt != null) {
            this.nbt = nbt.copy();
            //even more hardcoded shit
            if (fluid.equals(BuiltInSoftFluids.POTION.get()) && !this.nbt.contains(POTION_TYPE_KEY)) {
                this.nbt.putString(POTION_TYPE_KEY, "REGULAR");
            }
        }
        this.specialColor = 0;
        if (this.fluid.isEmpty()) this.setCount(0);
        this.needsColorRefresh = true;
    }

    /**
     * @return tint color to be applied on the fluid texture
     */
    //works on both side
    public abstract int getTintColor(@Nullable BlockAndTintGetter world, @Nullable BlockPos pos);

    /**
     * @return tint color to be applied on the fluid texture
     */
    public abstract int getFlowingTint(@Nullable BlockAndTintGetter world, @Nullable BlockPos pos);

    /**
     * @return tint color to be used on particle. Differs from getTintColor since it returns an mixWith color extrapolated from their fluid textures
     */
    public abstract int getParticleColor(@Nullable BlockAndTintGetter world, @Nullable BlockPos pos);

    /**
     * @return true if contained fluid has associated food
     */
    public boolean containsFood() {
        return this.fluid.isFood();
    }

    /**
     * call from tile entity. loads tank from nbt
     *
     * @param compound nbt
     */
    public void load(CompoundTag compound) {
        if (compound.contains("FluidHolder")) {
            CompoundTag cmp = compound.getCompound("FluidHolder");
            this.setCount(cmp.getInt("Count"));
            String id = cmp.getString("Fluid");
            SoftFluid sf = SoftFluidRegistry.get(id);
            this.setFluid(sf, cmp.getCompound("NBT"));
        }
    }

    /**
     * call from tile entity. saves to nbt
     *
     * @param compound nbt
     * @return nbt
     */
    public CompoundTag save(CompoundTag compound) {
        CompoundTag cmp = new CompoundTag();
        cmp.putInt("Count", this.count);
        if (this.fluid == null) this.fluid = BuiltInSoftFluids.EMPTY.get();
        var id = Utils.getID(fluid);
        if (id == null) {
            Moonlight.LOGGER.warn("Failed to save fluid in container: {} is not registered", fluid);
            cmp.putString("Fluid", SoftFluidRegistry.EMPTY_ID.toString());
        } else {
            cmp.putString("Fluid", id.toString());
        }
        //for item render. needed for potion colors. could be done better taking pos and level into account
        cmp.putInt("CachedColor", this.getTintColor(null, null));
        if (nbt != null && !nbt.isEmpty()) cmp.put("NBT", nbt);
        compound.put("FluidHolder", cmp);
        return compound;
    }

    /**
     * makes player drink 1 bottle and removes it from the tank
     *
     * @param player player
     * @param world  world
     * @return success
     */
    public boolean tryDrinkUpFluid(Player player, Level world) {
        if (!this.isEmpty() && this.containsFood()) {
            if (this.fluid.getFoodProvider().consume(player, world, this::applyNBTtoItemStack)) {
                this.shrink(1);
                return true;
            }
        }
        return false;
    }

    protected static boolean areNbtEquals(CompoundTag nbt, CompoundTag nbt1) {
        if ((nbt == null || nbt.isEmpty()) && (nbt1 == null || nbt1.isEmpty())) return true;
        if (nbt == null || nbt1 == null) return false;
        if (nbt1.contains(POTION_TYPE_KEY) && !nbt.contains(POTION_TYPE_KEY)) {
            var n1 = nbt1.copy();
            n1.remove(POTION_TYPE_KEY);
            return n1.equals(nbt);
        }
        if (nbt.contains(POTION_TYPE_KEY) && !nbt1.contains(POTION_TYPE_KEY)) {
            var n = nbt.copy();
            n.remove(POTION_TYPE_KEY);
            return n.equals(nbt1);
        }
        return nbt1.equals(nbt);
    }


    //handles special nbt items such as potions or soups
    protected void applyNBTtoItemStack(ItemStack stack) {
        List<String> nbtKey = this.fluid.getNbtKeyFromItem();
        if (this.nbt != null && !this.nbt.isEmpty()) {
            CompoundTag newCom = new CompoundTag();
            for (String s : nbtKey) {
                //ignores bottle tag, handled separately since it's a diff item
                Tag c = this.nbt.get(s);
                if (c != null && !s.equals(POTION_TYPE_KEY)) {
                    newCom.put(s, c);
                }
            }
            if (!newCom.isEmpty()) stack.setTag(newCom);
        }
    }

    //same syntax as merge
    protected void addPotionTag(Item i, CompoundTag com) {
        String type = "REGULAR";
        if (i instanceof SplashPotionItem) type = "SPLASH";
        else if (i instanceof LingeringPotionItem) type = "LINGERING";
        com.putString(POTION_TYPE_KEY, type);
    }


    //util functions
    public static int getLiquidCountFromItem(Item i) {
        if (i == Items.GLASS_BOTTLE) {
            return BOTTLE_COUNT;
        } else if (i == Items.BOWL) {
            return BOWL_COUNT;
        } else if (i == Items.BUCKET) {
            return BUCKET_COUNT;
        }
        return 0;
    }

}

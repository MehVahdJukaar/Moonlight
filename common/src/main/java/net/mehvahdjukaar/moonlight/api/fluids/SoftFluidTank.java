package net.mehvahdjukaar.moonlight.api.fluids;

import dev.architectury.injectables.annotations.ExpectPlatform;
import net.mehvahdjukaar.moonlight.api.util.Utils;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

/**
 * instance this fluid tank in your tile entity
 */
@SuppressWarnings("unused")
public abstract class SoftFluidTank {

    public static final int BOTTLE_COUNT = 1;
    public static final int BOWL_COUNT = 2;
    public static final int BUCKET_COUNT = 4;

    protected final int capacity;
    protected SoftFluidStack fluid = SoftFluidStack.empty();

    //Special tint color. Used for dynamic tint fluids like water and potions
    protected int specialColor = 0;
    protected boolean needsColorRefresh = true;

    protected SoftFluidTank(int capacity) {
        this.capacity = capacity;
    }

    @ExpectPlatform
    public static SoftFluidTank create(int capacity) {
        throw new AssertionError();
    }

    /**
     * call this method from your block when the player interacts. tries to fill or empty current held item in tank
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
    public ItemStack interactWithItem(ItemStack stack, Level world, @Nullable BlockPos pos, boolean simulate) {
        ItemStack returnStack;
        //try filling
        var fillResult = this.fillItem(stack, world, pos, simulate);
        if (fillResult.getResult().consumesAction()) return fillResult.getObject();
        //try emptying
        var drainResult = this.drainItem(stack, world, pos, simulate);
        if (drainResult.getResult().consumesAction()) return drainResult.getObject();

        return null;
    }

    public InteractionResultHolder<ItemStack> drainItem(ItemStack filledContainerStack, @Nullable Level world, @Nullable BlockPos pos, boolean simulate) {
        return drainItem(filledContainerStack, world, pos, simulate, true);
    }

    /**
     * Tries pouring the content of provided item in the tank
     * also plays sound.
     * If simulate is true, it will return the same item as normal but wont alter the container state
     *
     * @return empty container item, PASS if it failed
     */
    public InteractionResultHolder<ItemStack> drainItem(ItemStack filledContainer, Level level, @Nullable BlockPos pos, boolean simulate, boolean playSound) {

        var extracted = SoftFluidStack.fromItem(filledContainer);

        if (extracted != null && this.canAddSoftFluid(extracted.getFirst())) {
            SoftFluidStack fluidStack = extracted.getFirst();
            FluidContainerList.Category category = extracted.getSecond();

            ItemStack emptyContainer = category.getEmptyContainer().getDefaultInstance();
            if (!simulate) {

                //set new fluid if empty
                if (this.isEmpty()) {
                    this.setFluid(fluidStack);
                } else addFluidOntoExisting(fluidStack);

                SoundEvent sound = category.getEmptySound();
                if (sound != null && pos != null) {
                    level.playSound(null, pos, sound, SoundSource.BLOCKS, 1, 1);
                }
            }
            return InteractionResultHolder.sidedSuccess(emptyContainer, level.isClientSide);
        }
        return InteractionResultHolder.pass(ItemStack.EMPTY);
    }


    public InteractionResultHolder<ItemStack> fillItem(ItemStack emptyContainer, @Nullable Level world, @Nullable BlockPos pos, boolean simulate) {
        return fillItem(emptyContainer, world, pos, simulate, true);
    }

    /**
     * tries removing said amount of fluid and returns filled item
     * also plays sound
     *
     * @return filled bottle item. null if it failed or if simulated is true and failed
     */
    public InteractionResultHolder<ItemStack> fillItem(ItemStack emptyContainer, Level level, @Nullable BlockPos pos, boolean simulate, boolean playSound) {
        var pair = this.fluid.toItem(emptyContainer, simulate);

        if (pair != null) {
            var category = pair.getSecond();
            SoundEvent sound = category.getEmptySound();
            if (sound != null && pos != null) {
                level.playSound(null, pos, sound, SoundSource.BLOCKS, 1, 1);
            }
            return InteractionResultHolder.sidedSuccess(pair.getFirst(), level.isClientSide);
        }
        return InteractionResultHolder.pass(ItemStack.EMPTY);
    }

    /**
     * Called when talk is not empty and a new fluid is added. For most uses just increments the existing one but could alter the fluid content
     * You can assume that canAddSoftFluid has been called before
     */
    protected void addFluidOntoExisting(SoftFluidStack stack) {
        this.fluid.grow(stack.getCount());
    }

    /**
     * tries removing bottle amount and returns filled bottle
     *
     * @return filled bottle item. null if it failed
     */
    @Nullable
    public InteractionResultHolder<ItemStack> fillBottle(Level world, BlockPos pos) {
        return fillItem(Items.GLASS_BOTTLE.getDefaultInstance(), world, pos, false);
    }

    /**
     * tries removing bucket amount and returns filled bucket
     *
     * @return filled bucket item. null if it failed
     */
    @Nullable
    public InteractionResultHolder<ItemStack> fillBucket(Level world, BlockPos pos) {
        return fillItem(Items.BUCKET.getDefaultInstance(), world, pos, false);
    }

    /**
     * tries removing bowl amount and returns filled bowl
     *
     * @return filled bowl item. null if it failed
     */
    @Nullable
    public InteractionResultHolder<ItemStack> fillBowl(Level world, BlockPos pos) {
        return fillItem(Items.BOWL.getDefaultInstance(), world, pos, false);
    }

    /**
     * Main method called when checking if fluid can be added to this or not
     */
    public boolean canAddSoftFluid(SoftFluidStack fluidStack) {
        if (this.isEmpty()) return true;
        if (!this.fluid.isFluidEqual(fluidStack)) return false;
        return this.getSpace() >= fluidStack.getCount();
    }

    /**
     * try adding provided soft fluid to the tank
     *
     * @return success
     */
    public boolean addFluid(SoftFluidStack stack) {
        if (this.canAddSoftFluid(stack)) {
            if (this.isEmpty()) {
                this.setFluid(stack);
            } else {
                addFluidOntoExisting(stack);
            }
            return true;
        }
        return false;
    }

    /**
     * Transfers between 2 soft fluid tanks
     */
    public boolean transferFluid(SoftFluidTank destination) {
        return this.transferFluid(destination, BOTTLE_COUNT);
    }

    //transfers between two fluid holders
    public boolean transferFluid(SoftFluidTank destination, int amount) {
        if (this.isEmpty()) return false;
        if (this.getFluidCount() >= amount && destination.addFluid(this.fluid.copyWithCount(amount))) {
            this.fluid.shrink(amount);
            return true;
        }
        return false;
    }

    public int getSpace() {
        return Math.max(0, capacity - fluid.getCount());
    }

    public int getFluidCount() {
        return fluid.getCount();
    }

    public boolean isFull() {
        return fluid.getCount() == this.capacity;
    }

    public boolean isEmpty() {
        //count 0 should always = to fluid.empty
        return this.fluid.isEmpty();
    }

    /**
     * gets liquid height for renderer
     *
     * @param maxHeight maximum height in blocks
     * @return fluid height
     */
    public float getHeight(float maxHeight) {
        return maxHeight * fluid.getCount() / this.capacity;
    }

    /**
     * @return comparator block redstone power
     */
    public int getComparatorOutput() {
        float f = fluid.getCount() / (float) this.capacity;
        return Mth.floor(f * 14.0F) + 1;
    }

    public SoftFluidStack getFluid() {
        return fluid;
    }

    public SoftFluid getFluidValue() {
        return fluid.getFluid().value();
    }

    public void setFluid(SoftFluidStack fluid) {
        this.fluid = fluid;
        refreshTintCache();
    }

    public void refreshTintCache() {
        specialColor = 0;
        needsColorRefresh = true;
    }

    private void fillCount() {
        this.fluid.setCount(this.capacity);
    }

    /**
     * resets & clears the tank
     */
    public void clear() {
        this.setFluid(SoftFluidStack.empty());
    }

    /**
     * copies the content of a fluid tank into this
     *
     * @param other other tank
     */
    public void copyContent(SoftFluidTank other) {
        SoftFluidStack stack = other.getFluid();
        this.setFluid(stack.copyWithCount(Math.min(this.capacity, stack.getCount())));
    }

    public int getCapacity() {
        return capacity;
    }

    public void capCapacity() {
        this.fluid.setCount(Mth.clamp(this.fluid.getCount(), 0, capacity));
    }

    //TODO: move color stuff into fluidStack

    /**
     * @return cached tint color to be applied on the fluid texture
     */
    //works on both side
    public abstract int getTintColor(@Nullable BlockAndTintGetter world, @Nullable BlockPos pos);

    /**
     * @return cached tint color to be applied on the fluid texture
     */
    public abstract int getFlowingTint(@Nullable BlockAndTintGetter world, @Nullable BlockPos pos);

    /**
     * @return cached tint color to be used on particle. Differs from getTintColor since it returns an mixWith color extrapolated from their fluid textures
     */
    public abstract int getParticleColor(@Nullable BlockAndTintGetter world, @Nullable BlockPos pos);

    /**
     * @return true if contained fluid has associated food
     */
    public boolean containsFood() {
        return !this.fluid.getFoodProvider().isEmpty();
    }

    /**
     * call from tile entity. loads tank from nbt
     *
     * @param compound nbt
     */
    public void load(CompoundTag compound) {
        if (compound.contains("FluidHolder")) {
            CompoundTag cmp = compound.getCompound("FluidHolder");
            this.fluid = SoftFluidStack.load(cmp);
            if (this.isEmpty()) this.fluid = SoftFluidStack.empty();
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
        if (this.isEmpty()) this.fluid = SoftFluidStack.empty();
        this.fluid.save(cmp);
        //for item render. needed for potion colors. could be done better taking pos and level into account
        cmp.putInt("CachedColor", this.getTintColor(null, null));

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
            if (this.fluid.getFoodProvider().consume(player, world, this.fluid::applyNBTtoItemStack)) { //crap code right there
                fluid.shrink(1);
                return true;
            }
        }
        return false;
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

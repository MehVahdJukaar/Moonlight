package net.mehvahdjukaar.moonlight.api.fluids;

import dev.architectury.injectables.annotations.ExpectPlatform;
import net.mehvahdjukaar.moonlight.api.util.Utils;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
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
public class SoftFluidTank {

    public static final int BOTTLE_COUNT = 1;
    public static final int BOWL_COUNT = 2;
    public static final int BUCKET_COUNT = 4;

    protected final int capacity;
    protected SoftFluidStack fluidStack = SoftFluidStack.empty();

    //Minor optimization. Caches the tint color for the fluid
    protected int stillTintCache = 0;
    protected int flowingTintCache = 0;
    protected int particleTintCache = 0;
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
        if (extracted == null) return InteractionResultHolder.pass(ItemStack.EMPTY);
        SoftFluidStack fluidStack = extracted.getFirst();

        //if it can add all of it
        if (addFluid(fluidStack, true) == fluidStack.getCount()) {
            FluidContainerList.Category category = extracted.getSecond();

            ItemStack emptyContainer = category.getEmptyContainer().getDefaultInstance();
            if (!simulate) {

                addFluid(fluidStack, false);

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
        var pair = this.fluidStack.toItem(emptyContainer, simulate);

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
        this.fluidStack.grow(stack.getCount());
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
     * Check if fluid TYPE is compatible with content. Does not care about count
     */
    public boolean isFluidCompatible(SoftFluidStack fluidStack) {
        return this.fluidStack.isSameFluidSameComponents(fluidStack) || this.isEmpty();
    }

    /**
     * Like addFluid but can add only a part of the stack
     *
     * @return amount of fluid added. Given stack WILL be modified by subtracting that amount
     */
    public int addFluid(SoftFluidStack stack, boolean simulate) {
        if (!isFluidCompatible(stack)) return 0;
        int space = this.getSpace();
        if (space == 0) return 0;

        int amount = Math.min(space, stack.getCount());

        if (simulate) return amount;

        var toAdd = stack.split(amount);
        if (this.isEmpty()) {
            this.setFluid(toAdd);
        } else {
            this.addFluidOntoExisting(toAdd);
        }
        return amount;
    }

    /**
     * removes fluid from the tank
     *
     * @param amount   amount to remove
     * @param simulate if true, it will not actually remove the fluid
     * @return removed fluid
     */
    public SoftFluidStack removeFluid(int amount, boolean simulate) {
        if (this.isEmpty()) return SoftFluidStack.empty();
        int toRemove = Math.min(amount, this.fluidStack.getCount());
        SoftFluidStack stack = this.fluidStack.copyWithCount(toRemove);
        if (!simulate) {
            this.fluidStack.shrink(toRemove);
        }
        return stack;
    }

    /**
     * Transfers between 2 soft fluid tanks
     */
    @Deprecated(forRemoval = true)
    public boolean transferFluid(SoftFluidTank destination) {
        return this.transferFluid(destination, BOTTLE_COUNT);
    }

    //transfers between two fluid holders
    //I forgot why this was deprecated
    @Deprecated(forRemoval = true)
    public boolean transferFluid(SoftFluidTank destination, int amount) {
        if (this.isEmpty()) return false;
        var removed = this.removeFluid(amount, false);
        if (destination.addFluid(removed, true) == removed.getCount()) {
            destination.addFluid(removed, false);
            return true;
        }
        return false;
    }

    public int getSpace() {
        return Math.max(0, capacity - fluidStack.getCount());
    }

    public int getFluidCount() {
        return fluidStack.getCount();
    }

    public boolean isFull() {
        return fluidStack.getCount() == this.capacity;
    }

    public boolean isEmpty() {
        //count 0 should always = to fluid.empty
        return this.fluidStack.isEmpty();
    }

    /**
     * gets liquid height for renderer
     *
     * @param maxHeight maximum height in blocks
     * @return fluid height
     */
    public float getHeight(float maxHeight) {
        return maxHeight * fluidStack.getCount() / this.capacity;
    }

    /**
     * @return comparator block redstone power
     */
    public int getComparatorOutput() {
        float f = fluidStack.getCount() / (float) this.capacity;
        return Mth.floor(f * 14.0F) + 1;
    }

    public SoftFluidStack getFluid() {
        return fluidStack;
    }

    public SoftFluid getFluidValue() {
        return fluidStack.getHolder().value();
    }

    public void setFluid(SoftFluidStack fluid) {
        this.fluidStack = fluid.isEmpty() ? SoftFluidStack.empty() : fluid;
        refreshTintCache();
    }

    public void refreshTintCache() {
        stillTintCache = 0;
        needsColorRefresh = true;
    }

    private void fillCount() {
        this.fluidStack.setCount(this.capacity);
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
        this.fluidStack.setCount(Mth.clamp(this.fluidStack.getCount(), 0, capacity));
    }

    private void cacheColors(@Nullable BlockAndTintGetter world, @Nullable BlockPos pos) {
        stillTintCache = this.fluidStack.getStillColor(world, pos);
        flowingTintCache = this.fluidStack.getFlowingColor(world, pos);
        particleTintCache = this.fluidStack.getParticleColor(world, pos);
        needsColorRefresh = false;
    }

    public int getCachedStillColor(@Nullable BlockAndTintGetter world, @Nullable BlockPos pos) {
        if (needsColorRefresh) cacheColors(world, pos);
        return stillTintCache;
    }

    public int getCachedFlowingColor(@Nullable BlockAndTintGetter world, @Nullable BlockPos pos) {
        if (needsColorRefresh) cacheColors(world, pos);
        return flowingTintCache;
    }

    public int getCachedParticleColor(@Nullable BlockAndTintGetter world, @Nullable BlockPos pos) {
        if (needsColorRefresh) cacheColors(world, pos);
        return particleTintCache;
    }

    /**
     * @return true if contained fluid has associated food
     */
    public boolean containsFood() {
        return !this.fluidStack.getFoodProvider().isEmpty();
    }

    /**
     * call from tile entity. loads tank from nbt
     *
     * @param compound nbt
     */
    public void load(CompoundTag compound) {
        //backward compat
        if (compound.contains("FluidHolder")) {
            compound.put("fluid", compound.get("FluidHolder"));
            compound.remove("FluidHolder");
        }
        if (compound.contains("fluid")) {
            this.setFluid(SoftFluidStack.load(
                    Utils.hackyGetRegistryAccess(),
                    compound.getCompound("fluid")));
        }
    }

    /**
     * call from tile entity. saves to nbt
     *
     * @param compound nbt
     * @return nbt
     */
    public CompoundTag save(CompoundTag compound) {
        this.setFluid(this.fluidStack);
        Tag tag = this.fluidStack.save(Utils.hackyGetRegistryAccess());
        compound.put("fluid", tag);
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
            if (this.fluidStack.getFoodProvider().consume(player, world, fluidStack::copyComponentsTo)) { //crap code right there
                fluidStack.shrink(1);
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

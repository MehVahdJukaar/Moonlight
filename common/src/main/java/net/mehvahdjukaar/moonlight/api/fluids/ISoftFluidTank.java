package net.mehvahdjukaar.moonlight.api.fluids;

import dev.architectury.injectables.annotations.ExpectPlatform;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * instance this fluid tank in your tile entity
 */
@SuppressWarnings("unused")
public interface ISoftFluidTank {

    String POTION_TYPE_KEY = "Bottle";
    int BOTTLE_COUNT = 1;
    int BOWL_COUNT = 2;
    int BUCKET_COUNT = 4;

    @ExpectPlatform
    static ISoftFluidTank create(int capacity) {
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
    boolean interactWithPlayer(Player player, InteractionHand hand, @Nullable Level world, @Nullable BlockPos pos);

    /**
     * makes current item interact with fluid tank. returns empty stack if
     *
     * @param stack ItemStack to be interacted with
     * @param world world. null if no sound is to be played
     * @param pos   position. null if no sound is to be played
     * @return resulting ItemStack: empty for empty hand return, null if it failed
     */
    @Nullable
    ItemStack interactWithItem(ItemStack stack, @Nullable Level world, @Nullable BlockPos pos, boolean simulate);

    /**
     * tries pouring the content of provided item in the tank
     * also plays sound
     *
     * @return empty container item, null if it failed
     */
    @Nullable
    ItemStack tryDrainItem(ItemStack filledContainerStack, @Nullable Level world, @Nullable BlockPos pos, boolean simulate);

    /**
     * tries removing said amount of fluid and returns filled item
     * also plays sound
     *
     * @return filled bottle item. null if it failed or if simulated is true and failed
     */
    @Nullable
    ItemStack tryFillingItem(Item emptyContainer, @Nullable Level world, @Nullable BlockPos pos, boolean simulate);

    /**
     * tries removing bottle amount and returns filled bottle
     *
     * @return filled bottle item. null if it failed
     */
    @Nullable
    default ItemStack tryFillingBottle(Level world, BlockPos pos) {
        return tryFillingItem(Items.GLASS_BOTTLE, world, pos, false);
    }

    /**
     * tries removing bucket amount and returns filled bucket
     *
     * @return filled bucket item. null if it failed
     */
    @Nullable
    default ItemStack tryFillingBucket(Level world, BlockPos pos) {
        return tryFillingItem(Items.BUCKET, world, pos, false);
    }

    /**
     * tries removing bowl amount and returns filled bowl
     *
     * @return filled bowl item. null if it failed
     */
    @Nullable
    default ItemStack tryFillingBowl(Level world, BlockPos pos) {
        return tryFillingItem(Items.BOWL, world, pos, false);
    }

    /**
     * checks if current tank holds equivalent fluid as provided soft fluid
     *
     * @param other soft fluid
     * @return is same
     */
    default boolean isSameFluidAs(SoftFluid other) {
        return isSameFluidAs(other, null);
    }

    /**
     * checks if current tank holds equivalent fluid as provided soft fluid
     *
     * @param other soft fluid
     * @param com   fluid nbt
     * @return is same
     */
    default boolean isSameFluidAs(SoftFluid other, @Nullable CompoundTag com) {
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
    boolean tryAddingFluid(SoftFluid s, int count, @Nullable CompoundTag com);

    /**
     * try adding provided soft fluid to the tank
     *
     * @param s     soft fluid to add
     * @param count count to add
     * @return success
     */
    default boolean tryAddingFluid(SoftFluid s, int count) {
        return tryAddingFluid(s, count, null);
    }

    /**
     * try adding 1 bottle of provided soft fluid to the tank
     *
     * @param s soft fluid to add
     * @return success
     */
    default boolean tryAddingFluid(SoftFluid s) {
        return this.tryAddingFluid(s, 1);
    }

    /**
     * Transfers between 2 soft fluid tanks
     */
    default boolean tryTransferFluid(ISoftFluidTank destination) {
        return this.tryTransferFluid(destination, BOTTLE_COUNT);
    }

    //transfers between two fluid holders
    boolean tryTransferFluid(ISoftFluidTank destination, int amount);

    /**
     * can I remove n bottles of fluid
     *
     * @param n bottles amount
     * @return can remove
     */
    boolean canRemove(int n);

    /**
     * can I add n bottles of fluid
     *
     * @param n bottles amount
     * @return can add
     */
    boolean canAdd(int n);

    /**
     * can provide soft fluid be added to tank
     *
     * @param s     soft fluid to add
     * @param count bottles amount
     * @return can add
     */
    default boolean canAddSoftFluid(SoftFluid s, int count) {
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
    default boolean canAddSoftFluid(SoftFluid s, int count, @Nullable CompoundTag nbt) {
        return this.canAdd(count) && this.isSameFluidAs(s, nbt);
    }

    boolean isFull();

    boolean isEmpty();

    /**
     * grows contained fluid by at most inc bottles. doesn't need checking
     *
     * @param inc maximum increment
     */
    void lossyAdd(int inc);

    /**
     * unchecked sets the tank fluid count
     *
     * @param count bottles count
     */
    void setCount(int count);

    /**
     * fills out tank to the maximum capacity
     */
    void fillCount();

    /**
     * unchecked grows the tank by inc bottles. Check with canAdd()
     *
     * @param inc bottles increment
     */
    void grow(int inc);

    /**
     * unchecked shrinks the tank by inc bottles
     *
     * @param inc bottles increment
     */
    void shrink(int inc);

    /**
     * gets liquid height for renderer
     *
     * @param maxHeight maximum height in blocks
     * @return fluid height
     */
    float getHeight(float maxHeight);

    /**
     * @return comparator block redstone power
     */
    int getComparatorOutput();

    int getCount();

    @Nonnull
    SoftFluid getFluid();

    @Nullable
    CompoundTag getNbt();

    void setNbt(@Nullable CompoundTag nbt);

    /**
     * resets & clears the tank
     */
    void clear();

    /**
     * copies the content of a fluid tank into this
     *
     * @param other other tank
     */
    void copy(ISoftFluidTank other);


    /**
     * fills to max capacity with provided soft fluid
     *
     * @param fluid forge fluid
     */
    default void fill(SoftFluid fluid) {
        this.fill(fluid, null);
    }

    /**
     * fills to max capacity with provided soft fluid
     *
     * @param fluid soft fluid
     * @param nbt   soft fluid nbt
     */
    default void fill(SoftFluid fluid, @Nullable CompoundTag nbt) {
        this.setFluid(fluid, nbt);
        this.fillCount();
    }

    /**
     * sets current fluid to provided soft fluid equivalent
     *
     * @param fluid soft fluid
     */
    default void setFluid(@NotNull SoftFluid fluid) {
        this.setFluid(fluid, null);
    }

    //called when it goes from empty to full
    void setFluid(@NotNull SoftFluid fluid, @Nullable CompoundTag nbt);

    /**
     * @return tint color to be applied on the fluid texture
     */
    int getTintColor(@Nullable LevelReader world, @Nullable BlockPos pos);

    /**
     * @return tint color to be applied on the fluid texture
     */
    int getFlowingTint(@Nullable LevelReader world, @Nullable BlockPos pos);

    /**
     * @return tint color to be used on particle. Differs from getTintColor since it returns an mixWith color extrapolated from their fluid textures
     */
    int getParticleColor(@Nullable LevelReader world, @Nullable BlockPos pos);

    /**
     * @return true if contained fluid has associated food
     */
    default boolean containsFood() {
        return this.getFluid().isFood();
    }

    /**
     * call from tile entity. loads tank from nbt
     *
     * @param compound nbt
     */
    void load(CompoundTag compound);

    /**
     * call from tile entity. saves to nbt
     *
     * @param compound nbt
     * @return nbt
     */
    CompoundTag save(CompoundTag compound);

    /**
     * makes player drink 1 bottle and removes it from the tank
     *
     * @param player player
     * @param world  world
     * @return success
     */
    boolean tryDrinkUpFluid(Player player, Level world);


    static boolean areNbtEquals(CompoundTag nbt, CompoundTag nbt1) {
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

}

package net.mehvahdjukaar.moonlight.api.fluids;

import dev.architectury.injectables.annotations.ExpectPlatform;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
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

    @ExpectPlatform
    static ISoftFluidTank create(int capacity){
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
    public ItemStack interactWithItem(ItemStack stack, @Nullable Level world, @Nullable BlockPos pos, boolean simulate);

    /**
     * tries pouring the content of provided item in the tank
     * also plays sound
     *
     * @return empty container item, null if it failed
     */
    @Nullable
    public ItemStack tryDrainItem(ItemStack filledContainerStack, @Nullable Level world, @Nullable BlockPos pos, boolean simulate);

    /**
     * tries removing said amount of fluid and returns filled item
     * also plays sound
     *
     * @return filled bottle item. null if it failed or if simulated is true and failed
     */
    @Nullable
    public ItemStack tryFillingItem(Item emptyContainer, @Nullable Level world, @Nullable BlockPos pos, boolean simulate);
    /**
     * tries removing bottle amount and returns filled bottle
     *
     * @return filled bottle item. null if it failed
     */
    @Nullable
    public ItemStack tryFillingBottle(Level world, BlockPos pos);

    /**
     * tries removing bucket amount and returns filled bucket
     *
     * @return filled bucket item. null if it failed
     */
    @Nullable
    public ItemStack tryFillingBucket(Level world, BlockPos pos);

    /**
     * tries removing bowl amount and returns filled bowl
     *
     * @return filled bowl item. null if it failed
     */
    @Nullable
    public ItemStack tryFillingBowl(Level world, BlockPos pos);

    /**
     * checks if current tank holds equivalent fluid as provided soft fluid
     *
     * @param other soft fluid
     * @return is same
     */
    public boolean isSameFluidAs(SoftFluid other);

    /**
     * checks if current tank holds equivalent fluid as provided soft fluid
     *
     * @param other soft fluid
     * @param com   fluid nbt
     * @return is same
     */
    public boolean isSameFluidAs(SoftFluid other, @Nullable CompoundTag com);

    /**
     * try adding provided soft fluid to the tank
     *
     * @param s     soft fluid to add
     * @param count count to add
     * @param com   fluid nbt
     * @return success
     */
    public boolean tryAddingFluid(SoftFluid s, int count, @Nullable CompoundTag com);

    /**
     * try adding provided soft fluid to the tank
     *
     * @param s     soft fluid to add
     * @param count count to add
     * @return success
     */
    public boolean tryAddingFluid(SoftFluid s, int count);

    /**
     * try adding 1 bottle of provided soft fluid to the tank
     *
     * @param s soft fluid to add
     * @return success
     */
    public boolean tryAddingFluid(SoftFluid s);


    /**
     * can I remove n bottles of fluid
     *
     * @param n bottles amount
     * @return can remove
     */
    public boolean canRemove(int n);

    /**
     * can I add n bottles of fluid
     *
     * @param n bottles amount
     * @return can add
     */
    public boolean canAdd(int n);

    /**
     * can provide soft fluid be added to tank
     *
     * @param s     soft fluid to add
     * @param count bottles amount
     * @return can add
     */
    public boolean canAddSoftFluid(SoftFluid s, int count);

    /**
     * can provide soft fluid be added to tank
     *
     * @param s     soft fluid to add
     * @param count bottles amount
     * @param nbt   soft fluid nbt
     * @return can add
     */
    public boolean canAddSoftFluid(SoftFluid s, int count, @Nullable CompoundTag nbt);

    public boolean isFull();

    public boolean isEmpty() ;

    /**
     * grows contained fluid by at most inc bottles. doesn't need checking
     *
     * @param inc maximum increment
     */
    public void lossyAdd(int inc);

    /**
     * unchecked sets the tank fluid count
     *
     * @param count bottles count
     */
    public void setCount(int count);

    /**
     * fills out tank to the maximum capacity
     */
    public void fillCount();

    /**
     * unchecked grows the tank by inc bottles. Check with canAdd()
     *
     * @param inc bottles increment
     */
    public void grow(int inc);

    /**
     * unchecked shrinks the tank by inc bottles
     *
     * @param inc bottles increment
     */
    public void shrink(int inc);

    /**
     * gets liquid height for renderer
     *
     * @param maxHeight maximum height in blocks
     * @return fluid height
     */
    public float getHeight(float maxHeight);

    /**
     * @return comparator block redstone power
     */
    public int getComparatorOutput();

    public int getCount();

    @Nonnull
    public SoftFluid getFluid();

    @Nullable
    public CompoundTag getNbt();

    public void setNbt(@Nullable CompoundTag nbt);

    /**
     * resets & clears the tank
     */
    public void clear() ;

    /**
     * copies the content of a fluid tank into this
     *
     * @param other other tank
     */
    public void copy(ISoftFluidTank other);


    /**
     * fills to max capacity with provided soft fluid
     *
     * @param fluid forge fluid
     */
    public void fill(SoftFluid fluid);

    /**
     * fills to max capacity with provided soft fluid
     *
     * @param fluid soft fluid
     * @param nbt   soft fluid nbt
     */
    public void fill(SoftFluid fluid, @Nullable CompoundTag nbt);


    /**
     * sets current fluid to provided soft fluid equivalent
     *
     * @param fluid soft fluid
     */
    public void setFluid(@NotNull SoftFluid fluid) ;

    //called when it goes from empty to full
    public void setFluid(@NotNull SoftFluid fluid, @Nullable CompoundTag nbt);

    /**
     * @return tint color to be applied on the fluid texture
     */
    public int getTintColor(@Nullable LevelReader world, @Nullable BlockPos pos);

    /**
     * @return tint color to be applied on the fluid texture
     */
    public int getFlowingTint(@Nullable LevelReader world, @Nullable BlockPos pos) ;

    /**
     * @return tint color to be used on particle. Differs from getTintColor since it returns an mixWith color extrapolated from their fluid textures
     */
    public int getParticleColor(@Nullable LevelReader world, @Nullable BlockPos pos);


    /**
     * @return true if contained fluid has associated food
     */
    public boolean containsFood();

    /**
     * call from tile entity. loads tank from nbt
     *
     * @param compound nbt
     */
    public void load(CompoundTag compound);

    /**
     * call from tile entity. saves to nbt
     *
     * @param compound nbt
     * @return nbt
     */
    public CompoundTag save(CompoundTag compound);

    /**
     * makes player drink 1 bottle and removes it from the tank
     *
     * @param player player
     * @param world  world
     * @return success
     */
    public boolean tryDrinkUpFluid(Player player, Level world);


}

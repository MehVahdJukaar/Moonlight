package net.mehvahdjukaar.moonlight.api.block;

import net.minecraft.advancements.CriteriaTriggers;
import net.mehvahdjukaar.moonlight.api.platform.ForgeHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;
import java.util.stream.IntStream;

public abstract class ItemDisplayTile extends RandomizableContainerBlockEntity implements WorldlyContainer, IOwnerProtected {
    @Nullable
    private UUID owner = null;
    private NonNullList<ItemStack> stacks;

    protected ItemDisplayTile(BlockEntityType type, BlockPos pos, BlockState state) {
        this(type, pos, state, 1);
    }

    protected ItemDisplayTile(BlockEntityType type, BlockPos pos, BlockState state, int slots) {
        super(type, pos, state);
        this.stacks = NonNullList.withSize(slots, ItemStack.EMPTY);
    }

    @Override
    public void setOwner(@Nullable UUID owner) {
        this.owner = owner;
    }

    @Override
    @Nullable
    public UUID getOwner() {
        return owner;
    }

    //should only be server side. called when inventory has changed
    @Override
    public void setChanged() {
        if (this.level == null || level.isClientSide) return;
        this.updateTileOnInventoryChanged();
        if (this.needsToUpdateClientWhenChanged()) {
            //this saves and sends a packet to update the client tile
            this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), Block.UPDATE_CLIENTS);
        }
        super.setChanged();
    }

    /**
     * called every time the tile is marked dirty or loaded. Server side method.
     * Put here common logic for things that needs to react to inventory changes like updating blockState or logic
     */
    public void updateTileOnInventoryChanged() {
    }

    /**
     * @return true if the tile needs to react one inventory changes on client.
     * Set to true if you are using updateClientVisualsOnLoad()
     * usually not needed for tiles that do not visually display their content
     */
    public boolean needsToUpdateClientWhenChanged() {
        return true;
    }

    /**
     * Called after the tile is loaded from packet. Client side.
     * Put here client only visual logic that needs to react to inventory changes
     */
    public void updateClientVisualsOnLoad() {
    }

    public ItemStack getDisplayedItem() {
        return this.getItem(0);
    }

    public void setDisplayedItem(ItemStack stack) {
        this.setItem(0, stack);
    }

    public InteractionResult interact(Player player, InteractionHand handIn) {
        return this.interact(player, handIn, 0);
    }

    public InteractionResult interact(Player player, InteractionHand handIn, int slot) {
        if (!this.isAccessibleBy(player)) {
            player.displayClientMessage(Component.translatable("container.isLocked", ""), true);
        } else if (handIn == InteractionHand.MAIN_HAND) {
            ItemStack handItem = player.getItemInHand(handIn);
            //remove
            if (handItem.isEmpty()) {
                ItemStack it = this.removeItemNoUpdate(slot);
                if (!it.isEmpty()) {
                    onItemRemoved(player, it, slot);
                    if (!this.level.isClientSide()) {
                        player.setItemInHand(handIn, it);
                        this.setChanged();
                    } else {
                        //also update visuals on client. will get overwritten by packet tho
                        this.updateClientVisualsOnLoad();
                    }
                    return InteractionResult.sidedSuccess(this.level.isClientSide);
                }
            }
            //place
            else if (!handItem.isEmpty() && this.canPlaceItem(slot, handItem)) {
                ItemStack it = handItem.copy();
                it.setCount(1);
                this.setItem(slot, it);

                if (!player.isCreative()) {
                    handItem.shrink(1);
                }
                onItemAdded(player, it, slot);
                if (!this.level.isClientSide()) {
                    this.level.playSound(null, this.worldPosition, this.getAddItemSound(), SoundSource.BLOCKS, 1.0F, this.level.random.nextFloat() * 0.10F + 0.95F);
                    //this.setChanged();
                } else {
                    //also update visuals on client. will get overwritten by packet tho
                    this.updateClientVisualsOnLoad();
                }
                return InteractionResult.sidedSuccess(this.level.isClientSide);
            }
        }
        return InteractionResult.PASS;
    }

    public void onItemRemoved(Player player, ItemStack stack, int slot) {
        level.gameEvent(GameEvent.BLOCK_CHANGE, worldPosition, GameEvent.Context.of(player, getBlockState()));
    }

    public void onItemAdded(Player player, ItemStack stack, int slot) {
        level.gameEvent(GameEvent.BLOCK_CHANGE, worldPosition, GameEvent.Context.of(player, getBlockState()));

        //server
        if (player instanceof ServerPlayer serverPlayer) {
            CriteriaTriggers.ITEM_USED_ON_BLOCK.trigger(serverPlayer, worldPosition, stack);
            player.awardStat(Stats.ITEM_USED.get(stack.getItem()));
        }
    }

    public SoundEvent getAddItemSound() {
        return SoundEvents.ITEM_FRAME_ADD_ITEM;
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (!this.tryLoadLootTable(tag)) {
            this.stacks = NonNullList.withSize(this.getContainerSize(), ItemStack.EMPTY);
        }
        ContainerHelper.loadAllItems(tag, this.stacks, registries);
        if (this.level != null) {
            if (this.level.isClientSide) this.updateClientVisualsOnLoad();
                //this doesn't work on first load cause world is null on server. You need to save stuff on nbt
            else this.updateTileOnInventoryChanged();
        }
        this.loadOwner(tag);
    }

    @Override
    public void saveAdditional(CompoundTag compound, HolderLookup.Provider registries) {
        super.saveAdditional(compound, registries);
        if (!this.trySaveLootTable(compound)) {
            ContainerHelper.saveAllItems(compound, this.stacks, registries);
        }
        this.saveOwner(compound);
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return this.saveWithoutMetadata(registries);
    }

    @Override
    public int getContainerSize() {
        return stacks.size();
    }

    @Override
    public int getMaxStackSize() {
        return 1;
    }

    //use the one below
    @Deprecated(forRemoval = true)
    @ApiStatus.Internal
    @Override
    public AbstractContainerMenu createMenu(int id, Inventory player) {
        return ChestMenu.threeRows(id, player, this);
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int i, Inventory inventory, Player player) {
        return null;
    }


    @Override
    protected NonNullList<ItemStack> getItems() {
        return this.stacks;
    }

    @Override
    public void setItems(NonNullList<ItemStack> stacks) {
        this.stacks = stacks;
    }

    @Override
    public boolean canPlaceItem(int index, ItemStack stack) {
        return this.isEmpty();
    }

    @Override
    public boolean canPlaceItemThroughFace(int index, ItemStack stack, @Nullable Direction direction) {
        return false;
    }

    @Override
    public boolean canTakeItemThroughFace(int index, ItemStack stack, Direction direction) {
        return false;
    }

    @Override
    public int[] getSlotsForFace(Direction side) {
        return IntStream.range(0, this.getContainerSize()).toArray();
    }


}
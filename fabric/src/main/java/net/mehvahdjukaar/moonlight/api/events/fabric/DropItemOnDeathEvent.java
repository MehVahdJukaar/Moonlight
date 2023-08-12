package net.mehvahdjukaar.moonlight.api.events.fabric;

import net.mehvahdjukaar.moonlight.api.events.IDropItemOnDeathEvent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;


public class DropItemOnDeathEvent implements IDropItemOnDeathEvent  {
    private final ItemStack itemStack;
    private final Player player;
    private boolean canceled = false;
    private ItemStack returnStack;
    private final boolean isBeforeDrop;

    public DropItemOnDeathEvent(ItemStack itemStack, Player player, boolean beforeDrop) {
        this.itemStack = itemStack;
        this.player = player;
        this.returnStack = itemStack;
        this.isBeforeDrop = beforeDrop;
    }


    @Override
    public boolean isBeforeDrop() {
        return this.isBeforeDrop;
    }

    @Override
    public Player getPlayer() {
        return this.player;
    }

    @Override
    public ItemStack getItemStack() {
        return this.itemStack;
    }

    @Override
    public void setCanceled(boolean cancelled) {
        this.canceled = cancelled;
    }

    @Override
    public boolean isCanceled() {
        return canceled;
    }

    @Override
    public void setReturnItemStack(ItemStack stack) {
        this.returnStack = stack;
    }

    @Override
    public ItemStack getReturnItemStack() {
        return returnStack;
    }
}
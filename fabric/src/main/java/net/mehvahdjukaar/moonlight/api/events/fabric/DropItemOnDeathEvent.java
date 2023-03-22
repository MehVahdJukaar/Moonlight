package net.mehvahdjukaar.moonlight.api.events.fabric;

import net.mehvahdjukaar.moonlight.api.events.IDropItemOnDeathEvent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class DropItemOnDeathEvent implements IDropItemOnDeathEvent {
    private final ItemStack itemStack;
    private final Player player;
    private boolean canceled;

    public DropItemOnDeathEvent(ItemStack itemStack, Player player) {
        this.itemStack = itemStack;
        this.player = player;
    }

    public static IDropItemOnDeathEvent create(ItemStack itemStack, Player player) {
        return new DropItemOnDeathEvent(itemStack, player);
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
    public boolean isCanceled() {
        return canceled;
    }

    @Override
    public void setCanceled(boolean canceled) {
        this.canceled = canceled;
    }
}
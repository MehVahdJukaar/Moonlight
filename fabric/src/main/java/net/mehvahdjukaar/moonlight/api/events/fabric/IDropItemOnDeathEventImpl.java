package net.mehvahdjukaar.moonlight.api.events.fabric;

import net.mehvahdjukaar.moonlight.api.events.IDropItemOnDeathEvent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;


public class IDropItemOnDeathEventImpl implements IDropItemOnDeathEvent {
    private final ItemStack itemStack;
    private final Player player;
    private boolean canceled = false;

    public IDropItemOnDeathEventImpl(ItemStack itemStack, Player player) {
        this.itemStack = itemStack;
        this.player = player;
    }

    public static IDropItemOnDeathEvent create(ItemStack itemStack, Player player) {
        return new IDropItemOnDeathEventImpl(itemStack, player);
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

    public boolean isCanceled() {
        return this.canceled;
    }

}

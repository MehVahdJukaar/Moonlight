package net.mehvahdjukaar.moonlight.api.events.forge;

import net.mehvahdjukaar.moonlight.api.events.IDropItemOnDeathEvent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.Event;

public class DropItemOnDeathEvent extends Event implements IDropItemOnDeathEvent {
    private final ItemStack itemStack;
    private final Player player;

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

}

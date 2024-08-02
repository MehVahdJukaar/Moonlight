package net.mehvahdjukaar.moonlight.api.events.neoforge;

import net.mehvahdjukaar.moonlight.api.events.IDropItemOnDeathEvent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class IDropItemOnDeathEventImpl {

    public static IDropItemOnDeathEvent create(ItemStack itemStack, Player player, boolean beforeDrop) {
        return new DropItemOnDeathEvent(itemStack, player, beforeDrop);
    }
}

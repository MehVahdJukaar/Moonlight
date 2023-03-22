package net.mehvahdjukaar.moonlight.api.events.forge;

import net.mehvahdjukaar.moonlight.api.events.IDropItemOnDeathEvent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class IDropItemOnDeathEventImpl {
    public static IDropItemOnDeathEvent create(ItemStack itemStack, Player player) {
        return new DropItemOnDeathEvent(itemStack,player);
    }
}

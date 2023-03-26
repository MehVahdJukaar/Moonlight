package net.mehvahdjukaar.moonlight.api.events;

import dev.architectury.injectables.annotations.ExpectPlatform;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public interface IDropItemOnDeathEvent extends SimpleEvent {

    @ExpectPlatform
    static IDropItemOnDeathEvent create(ItemStack itemStack, Player player, boolean beforeDrop) {
        throw new AssertionError();
    }

    /**
     * If true this is fired before the item is dropped. If false its after the item is cloned
     * Cancel the event on both if you want it to both not drop and persist
     * If you want to modify the returned stack only do it once in one of those
     */
    boolean isBeforeDrop();

    Player getPlayer();

    ItemStack getItemStack();

    /**
     * Prevents item from dropping if before drop is true. From being copied to the new player if after
     */
    void setCanceled(boolean cancelled);

    boolean isCanceled();

    void setReturnItemStack(ItemStack stack);

    ItemStack getReturnItemStack();
}
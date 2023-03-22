package net.mehvahdjukaar.moonlight.api.events;

import dev.architectury.injectables.annotations.ExpectPlatform;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public interface IDropItemOnDeathEvent extends SimpleEvent {

    @ExpectPlatform
    static IDropItemOnDeathEvent create(ItemStack itemStack, Player player) {
        throw new AssertionError();
    }

    Player getPlayer();

    ItemStack getItemStack();

    /**
     * Prevents item from dropping
     */
    void setCanceled(boolean cancelled);

    boolean isCanceled();


}
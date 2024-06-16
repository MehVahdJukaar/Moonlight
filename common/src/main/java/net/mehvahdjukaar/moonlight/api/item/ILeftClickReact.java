package net.mehvahdjukaar.moonlight.api.item;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

// Implement in your items. Adds on left click callback
public interface ILeftClickReact {

    /**
     * Called as soon as the player left clicks with the item, before attacking logic is processed
     * @return true if attack should be cancelled
     */
   boolean onLeftClick(ItemStack stack, Player player, InteractionHand hand);

}

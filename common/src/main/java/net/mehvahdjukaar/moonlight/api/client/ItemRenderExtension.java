package net.mehvahdjukaar.moonlight.api.client;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public interface ItemRenderExtension {

    @Nullable
    default ItemStackRenderer getItemRenderer() {
        return null;
    }

    default void renderHelmetOverlay(ItemStack stack, Player player, GuiGraphics guiGraphics, DeltaTracker deltaTracker) {
    }

}

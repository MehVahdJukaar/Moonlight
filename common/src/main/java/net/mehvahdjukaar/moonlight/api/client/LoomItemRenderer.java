package net.mehvahdjukaar.moonlight.api.client;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BannerPatternLayers;
import org.jetbrains.annotations.Nullable;

@FunctionalInterface
public interface LoomItemRenderer {

    boolean render(GuiGraphics graphics, ItemStack bannerSlotStack, ItemStack result,
                   @Nullable BannerPatternLayers patterns, int leftPos, int topPos, float partialTicks);
}

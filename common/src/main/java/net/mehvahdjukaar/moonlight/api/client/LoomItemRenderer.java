package net.mehvahdjukaar.moonlight.api.client;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.core.Holder;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BannerPattern;
import net.minecraft.world.level.block.entity.BannerPatternLayers;
import org.jetbrains.annotations.Nullable;

@FunctionalInterface
public interface LoomItemRenderer {

    boolean render(GuiGraphicsExtractor graphics, ItemStack bannerSlotStack, ItemStack result,
                   @Nullable BannerPatternLayers patterns, int leftPos, int topPos, float partialTicks);

    /**
     * One of the little buttons in the pattern grid. Vanilla fits its flag in a 5 by 10 box at x + 4, y + 2,
     * pattern in white over a gray base. Return true to skip it and keep your own outline
     */
    default boolean renderPatternIcon(GuiGraphicsExtractor graphics, ItemStack bannerSlotStack,
                                      Holder<BannerPattern> pattern, int x, int y) {
        return false;
    }
}

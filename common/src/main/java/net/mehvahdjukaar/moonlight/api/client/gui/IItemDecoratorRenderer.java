package net.mehvahdjukaar.moonlight.api.client.gui;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.world.item.ItemStack;

@FunctionalInterface
public interface IItemDecoratorRenderer {
    boolean render(GuiGraphicsExtractor graphics, Font font, ItemStack stack, int x, int y);

}

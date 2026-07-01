package net.mehvahdjukaar.moonlight.api.client.gui;

import net.minecraft.Util;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

public final class GuiHelper {

    /**
     * Left aligned text that scrolls back and forth when it doesn't fit its box, mirroring vanilla's
     * {@code AbstractWidget.renderScrollingString} (which isn't publicly accessible). Handy for any label/value
     * box whose text can overflow (option rows, combo box values, ...).
     */
    public static void renderScrollingText(GuiGraphics graphics, Font font, Component text, int minX, int maxX, int rowTop, int rowHeight, int color) {
        int textWidth = font.width(text);
        int available = maxX - minX;
        int textY = rowTop + (rowHeight - font.lineHeight) / 2 + 1;
        if (textWidth > available) {
            int overflow = textWidth - available;
            double seconds = (double) Util.getMillis() / 1000.0;
            double period = Math.max(overflow * 0.5, 3.0);
            double phase = Math.sin((Math.PI / 2) * Math.cos((Math.PI * 2) * seconds / period)) / 2.0 + 0.5;
            double offset = Mth.lerp(phase, 0.0, overflow);
            graphics.enableScissor(minX, rowTop, maxX, rowTop + rowHeight);
            graphics.drawString(font, text, minX - (int) offset, textY, color);
            graphics.disableScissor();
        } else {
            graphics.drawString(font, text, minX, textY, color);
        }
    }
}

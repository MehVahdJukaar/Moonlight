package net.mehvahdjukaar.moonlight.core.client.config;

import net.mehvahdjukaar.moonlight.api.client.gui.misc.ConfigGuiColors;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import org.jetbrains.annotations.Nullable;

import java.util.List;

class DescriptionRow extends ConfigListRow {

    private final Font font;
    private final List<FormattedCharSequence> lines;

    DescriptionRow(Font font, List<FormattedCharSequence> lines) {
        this.font = font;
        this.lines = List.copyOf(lines);
    }

    @Override
    public void render(GuiGraphics graphics, int index, int top, int left, int width, int height,
                       int mouseX, int mouseY, boolean hovering, float partialTick) {
        int y = top + 1;
        for (FormattedCharSequence line : lines) {
            graphics.drawString(font, line, left + ConfigScreenLayout.ARROW_WIDTH + 2, y, ConfigGuiColors.DESCRIPTION);
            y += font.lineHeight;
        }
    }

    @Override
    public List<? extends GuiEventListener> children() {
        return List.of();
    }

    @Override
    public List<? extends NarratableEntry> narratables() {
        return List.of();
    }

    @Nullable
    @Override
    public Component getTooltip(int mouseX, int mouseY) {
        return null;
    }
}

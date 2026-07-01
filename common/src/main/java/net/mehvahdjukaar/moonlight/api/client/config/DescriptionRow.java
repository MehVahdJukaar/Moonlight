package net.mehvahdjukaar.moonlight.api.client.config;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static net.mehvahdjukaar.moonlight.api.client.config.ConfigScreenLayout.ARROW_WIDTH;
import static net.mehvahdjukaar.moonlight.api.client.config.ConfigScreenLayout.DESCRIPTION_COLOR;

/**
 * A read-only row holding a slice of an expanded value's wrapped description text.
 */
class DescriptionRow extends ConfigRow {

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
            graphics.drawString(font, line, left + ARROW_WIDTH + 2, y, DESCRIPTION_COLOR);
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
    Component getTooltip(int mouseX, int mouseY) {
        return null;
    }
}

package net.mehvahdjukaar.moonlight.core.client.config;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static net.mehvahdjukaar.moonlight.core.client.config.ConfigScreenLayout.ARROW_WIDTH;
import static net.mehvahdjukaar.moonlight.api.client.gui.misc.ConfigGuiColors.DESCRIPTION;

class DescriptionRow extends ConfigListRow {

    private final Font font;
    private final List<FormattedCharSequence> lines;

    DescriptionRow(Font font, List<FormattedCharSequence> lines) {
        this.font = font;
        this.lines = List.copyOf(lines);
    }

    @Override
    public void extractContent(GuiGraphicsExtractor graphics, int mouseX, int mouseY, boolean hovering, float partialTick) {
        int top = this.getContentY(), left = this.getX(), width = this.getWidth(), height = this.getContentHeight();
        int y = top + 1;
        for (FormattedCharSequence line : lines) {
            graphics.text(font, line, left + ARROW_WIDTH + 2, y, DESCRIPTION);
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

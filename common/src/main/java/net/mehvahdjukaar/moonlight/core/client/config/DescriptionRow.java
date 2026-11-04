package net.mehvahdjukaar.moonlight.core.client.config;

import net.mehvahdjukaar.moonlight.api.client.gui.GuiHelper;
import net.mehvahdjukaar.moonlight.api.client.gui.Icon;
import net.mehvahdjukaar.moonlight.api.client.gui.misc.ConfigGuiColors;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static net.mehvahdjukaar.moonlight.core.client.config.ConfigScreenLayout.*;

public class DescriptionRow extends ConfigRow {

    private final Font font;
    @Nullable
    private final Icon image;
    private final List<FormattedCharSequence> lines;
    private final int contentHeight;

    public DescriptionRow(Font font, @Nullable Icon image, @Nullable Component description, int rowWidth) {
        this.font = font;
        this.image = image;
        int textWidth = rowWidth - ARROW_WIDTH - GAP - (image == null ? 0 : DESC_IMAGE_SIZE + GAP);
        this.lines = description == null ? List.of() : font.split(description, textWidth);
        this.contentHeight = Math.max(image == null ? 0 : DESC_IMAGE_SIZE, this.lines.size() * font.lineHeight);
    }

    @Override
    public int height(int itemHeight) {
        return contentHeight + 4;
    }

    @Override
    public void extractContent(GuiGraphicsExtractor graphics, int mouseX, int mouseY, boolean hovering, float partialTick) {
        int top = this.getContentY();
        int x = this.getX() + ARROW_WIDTH + 2;
        if (image != null) {
            GuiHelper.renderCroppedIcon(graphics, image, x, top, DESC_IMAGE_SIZE);
            x += DESC_IMAGE_SIZE + GAP;
        }

        int y = top;
        for (FormattedCharSequence line : lines) {
            graphics.text(font, line, x, y, ConfigGuiColors.DESCRIPTION);
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

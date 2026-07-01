package net.mehvahdjukaar.moonlight.api.client.config;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static net.mehvahdjukaar.moonlight.api.client.config.ConfigScreenLayout.*;

/**
 * One full width button on the config select screen: opens a single registered config of a mod. Styled like a
 * {@link CategoryRow} (accent label + chevron) so both read as "navigate in".
 */
class ConfigHolderRow extends ConfigRow {

    private final Button button;
    private final List<AbstractWidget> children;
    @Nullable
    private final Component tooltip;

    ConfigHolderRow(Component label, @Nullable Component tooltip, Runnable onClick) {
        this.tooltip = tooltip;
        Component styled = Component.empty()
                .append(label.copy().withStyle(Style.EMPTY.withColor(TextColor.fromRgb(CATEGORY_COLOR))))
                .append(Component.literal("  ›").withStyle(Style.EMPTY.withColor(TextColor.fromRgb(CRUMB_SEPARATOR_COLOR))));
        this.button = Button.builder(styled, b -> onClick.run())
                .bounds(0, 0, ROW_WIDTH, CONTROL_HEIGHT).build();
        this.children = List.of(button);
    }

    @Override
    public void render(GuiGraphics graphics, int index, int top, int left, int width, int height,
                       int mouseX, int mouseY, boolean hovering, float partialTick) {
        button.setX(left);
        button.setWidth(width);
        button.setY(top + (height - CONTROL_HEIGHT) / 2);
        button.render(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    public List<? extends GuiEventListener> children() {
        return children;
    }

    @Override
    public List<? extends NarratableEntry> narratables() {
        return children;
    }

    @Nullable
    @Override
    Component getTooltip(int mouseX, int mouseY) {
        return tooltip;
    }
}

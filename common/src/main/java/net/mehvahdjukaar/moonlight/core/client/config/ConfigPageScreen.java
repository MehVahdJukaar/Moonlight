package net.mehvahdjukaar.moonlight.core.client.config;

import net.mehvahdjukaar.moonlight.api.client.gui.OverlayLayer;
import net.mehvahdjukaar.moonlight.api.client.gui.PopupHost;
import net.mehvahdjukaar.moonlight.api.platform.configs.options.ConfigCategory;
import net.mehvahdjukaar.moonlight.api.platform.configs.options.ConfigOption;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static net.mehvahdjukaar.moonlight.core.client.config.ConfigScreenLayout.*;

abstract class ConfigPageScreen extends Screen implements ConfigScreenAccess, PopupHost {

    protected final OverlayLayer overlay = new OverlayLayer();
    protected ConfigRowList list;

    protected ConfigPageScreen(Component title) {
        super(title);
    }

    @Override
    public Font font() {
        return this.font;
    }

    @Override
    public OverlayLayer getOverlayLayer() {
        return this.overlay;
    }

    @Override
    public void toggleExpanded(ConfigOption<?> value) {
        session().toggleExpanded(value);
        populate();
    }

    @Override
    public boolean isCategoryEnabled(ConfigCategory category) {
        ConfigOption.BooleanValue gate = category.gate();
        boolean own = gate == null || Boolean.TRUE.equals(session().current(gate));
        ConfigCategory parent = category.parent();
        return own && (parent == null || isCategoryEnabled(parent));
    }

    protected abstract void populate();

    protected void addDescriptionRows(List<ConfigListRow> rows, ConfigOption<?> option) {
        if (option.description() == null || !session().isExpanded(option)) return;
        List<FormattedCharSequence> lines = this.font.split(option.description(), ROW_WIDTH - ARROW_WIDTH - GAP);
        for (int i = 0; i < lines.size(); i += DESC_LINES_PER_ROW) {
            rows.add(new DescriptionRow(this.font, lines.subList(i, Math.min(i + DESC_LINES_PER_ROW, lines.size()))));
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        return overlay.mouseClicked(mouseX, mouseY, button) || super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        return overlay.mouseScrolled(mouseX, mouseY, scrollY) || super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    @Override
    public boolean keyPressed(int key, int scanCode, int modifiers) {
        return overlay.keyPressed(key, scanCode, modifiers) || super.keyPressed(key, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char c, int modifiers) {
        return overlay.charTyped(c, modifiers) || super.charTyped(c, modifiers);
    }

    protected boolean renderOverlayOrTooltip(GuiGraphics graphics, int mouseX, int mouseY) {
        if (overlay.isOpen()) {
            overlay.render(graphics, mouseX, mouseY);
            return true;
        }
        Component tooltip = tooltipAt(mouseX, mouseY);
        if (tooltip != null) {
            graphics.renderTooltip(this.font, this.font.split(tooltip, 220), mouseX, mouseY);
        }
        return false;
    }

    @Nullable
    private Component tooltipAt(int mouseX, int mouseY) {
        ConfigListRow hovered = this.list.getHovered(mouseX, mouseY);
        if (hovered != null) {
            Component tooltip = hovered.getTooltip(mouseX, mouseY);
            if (tooltip != null) return tooltip;
        }
        for (ConfigListRow row : this.list.children()) {
            Component tooltip = row.getGutterTooltip(mouseX, mouseY);
            if (tooltip != null) return tooltip;
        }
        return null;
    }
}

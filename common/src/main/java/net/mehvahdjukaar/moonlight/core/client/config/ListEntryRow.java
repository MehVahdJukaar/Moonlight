package net.mehvahdjukaar.moonlight.core.client.config;

import net.mehvahdjukaar.moonlight.api.client.gui.widget.IconButton;
import net.mehvahdjukaar.moonlight.api.client.gui.MoonlightIcons;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.network.chat.Component;
import net.minecraft.client.input.MouseButtonEvent;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

import static net.mehvahdjukaar.moonlight.core.client.config.ConfigScreenLayout.*;

class ListEntryRow extends ConfigListRow {

    private final ConfigListRow inner;
    private final IconButton remove;
    private final List<GuiEventListener> children;
    private final List<NarratableEntry> narratables;

    ListEntryRow(ConfigListRow inner, boolean canRemove, Runnable onRemove) {
        this.inner = inner;
        this.remove = new IconButton(0, 0, RESET_WIDTH, CONTROL_HEIGHT, Component.empty(), MoonlightIcons.DELETE, b -> onRemove.run());
        this.remove.active = canRemove;
        this.remove.setTooltip(Tooltip.create(Component.translatable("gui.moonlight.config.list_remove")));

        List<GuiEventListener> all = new ArrayList<>(inner.children());
        all.add(remove);
        this.children = List.copyOf(all);

        List<NarratableEntry> narrated = new ArrayList<>(inner.narratables());
        narrated.add(remove);
        this.narratables = List.copyOf(narrated);
    }

    @Override
    public void extractContent(GuiGraphicsExtractor graphics, int mouseX, int mouseY, boolean hovering, float partialTick) {
        inner.setX(this.getX());
        inner.setY(this.getY());
        inner.setWidth(this.getWidth() - RESET_WIDTH - GAP);
        inner.setHeight(this.getHeight());
        inner.extractContent(graphics, mouseX, mouseY, hovering, partialTick);
        remove.setX(this.getX() + this.getWidth() - RESET_WIDTH);
        remove.setY(this.getContentY() + (this.getContentHeight() - CONTROL_HEIGHT) / 2);
        remove.extractRenderState(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        return inner.mouseClicked(event, doubleClick) || super.mouseClicked(event, doubleClick);
    }

    @Override
    public List<? extends GuiEventListener> children() {
        return children;
    }

    @Override
    public List<? extends NarratableEntry> narratables() {
        return narratables;
    }

    @Nullable
    @Override
    Component getTooltip(int mouseX, int mouseY) {
        return inner.getTooltip(mouseX, mouseY);
    }

    @Nullable
    @Override
    Component getGutterTooltip(int mouseX, int mouseY) {
        return inner.getGutterTooltip(mouseX, mouseY);
    }
}

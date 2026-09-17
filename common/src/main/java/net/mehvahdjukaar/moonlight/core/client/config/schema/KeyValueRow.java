package net.mehvahdjukaar.moonlight.core.client.config.schema;

import net.mehvahdjukaar.moonlight.api.client.gui.ConfigControl;
import net.mehvahdjukaar.moonlight.api.client.gui.MoonlightIcons;
import net.mehvahdjukaar.moonlight.api.platform.configs.options.ConfigOption;
import net.mehvahdjukaar.moonlight.core.client.config.ConfigControllers;
import net.mehvahdjukaar.moonlight.core.client.config.ConfigListRow;
import net.mehvahdjukaar.moonlight.core.client.config.ConfigScreenAccess;
import net.mehvahdjukaar.moonlight.core.client.config.GutterHints;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BooleanSupplier;

import static net.mehvahdjukaar.moonlight.core.client.config.ConfigScreenLayout.*;

class KeyValueRow extends ConfigListRow {

    private static final int VALUE_WIDTH = CONTROL_WIDTH + RESET_WIDTH + 3 * GAP;

    private final ConfigControl<?> key;
    private final ConfigListRow valueRow;
    private final BooleanSupplier isDuplicateKey;
    private final GutterHints gutter = new GutterHints();
    private final List<GuiEventListener> children = new ArrayList<>();
    private final List<NarratableEntry> narratableEntries = new ArrayList<>();

    KeyValueRow(ConfigScreenAccess view, ConfigOption<?> keyOption, ConfigListRow valueRow, BooleanSupplier isDuplicateKey) {
        this.key = ConfigControllers.create(keyOption, view.session(), view::onValueEdited);
        this.valueRow = valueRow;
        this.isDuplicateKey = isDuplicateKey;
        this.children.add(key.widget());
        this.children.addAll(valueRow.children());
        this.narratableEntries.add(key.widget());
        this.narratableEntries.addAll(valueRow.narratables());
    }

    @Override
    public void render(GuiGraphics graphics, int index, int top, int left, int width, int height,
                       int mouseX, int mouseY, boolean hovering, float partialTick) {
        gutter.begin(top, height);
        if (isDuplicateKey.getAsBoolean()) {
            gutter.add(graphics, left, MoonlightIcons.WARNING, Component.translatable("gui.moonlight.config.map_duplicate_key"));
        }

        int valueX = left + width - VALUE_WIDTH;

        AbstractWidget keyWidget = key.widget();
        keyWidget.setX(left);
        keyWidget.setY(top + (height - keyWidget.getHeight()) / 2);
        keyWidget.setWidth(valueX - GAP - left);
        keyWidget.render(graphics, mouseX, mouseY, partialTick);

        valueRow.render(graphics, index, top, valueX, VALUE_WIDTH, height, mouseX, mouseY, hovering, partialTick);
    }

    @Override
    public List<? extends GuiEventListener> children() {
        return children;
    }

    @Override
    public List<? extends NarratableEntry> narratables() {
        return narratableEntries;
    }

    @Nullable
    @Override
    public Component getTooltip(int mouseX, int mouseY) {
        return valueRow.getTooltip(mouseX, mouseY);
    }

    @Nullable
    @Override
    public Component getGutterTooltip(int mouseX, int mouseY) {
        Component own = gutter.tooltipAt(mouseX, mouseY);
        return own != null ? own : valueRow.getGutterTooltip(mouseX, mouseY);
    }
}

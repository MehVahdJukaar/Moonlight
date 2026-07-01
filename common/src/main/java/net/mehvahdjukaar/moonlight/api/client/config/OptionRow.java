package net.mehvahdjukaar.moonlight.api.client.config;

import net.mehvahdjukaar.moonlight.api.client.gui.IconButton;
import net.mehvahdjukaar.moonlight.api.platform.configs.options.ConfigCategory;
import net.mehvahdjukaar.moonlight.api.platform.configs.options.ConfigOption;
import net.mehvahdjukaar.moonlight.api.platform.configs.options.ConfigReloadType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static net.mehvahdjukaar.moonlight.api.client.config.ConfigScreenLayout.*;

/**
 * A single editable config value: a (scrolling) label, an editing control from {@link ConfigControls}, a
 * rollback button enabled when the value differs from its default, and an optional left arrow that toggles the
 * inline description.
 */
class OptionRow extends ConfigRow {

    private final ConfigScreenView view;
    private final ConfigEditSession session;
    private final ConfigOption<?> value;
    @Nullable
    private final ConfigCategory owner; // the category this value lives under, for feature-gating greyout
    private final Component title;
    @Nullable
    private final Component description;
    private final Control control;
    private final Button resetButton;
    @Nullable
    private final Button expandButton;
    private final boolean editable;
    private final List<AbstractWidget> children;

    // hover region of the rendered label text, refreshed each frame (used for the precise hover tooltip)
    private int labelX0, labelX1, labelY0, labelY1;
    // hover region of the reload/restart hint icon, if any
    private int reloadIconX0 = -1, reloadIconX1 = -1;

    OptionRow(ConfigScreenView view, ConfigOption<?> value) {
        this.view = view;
        this.session = view.session();
        this.value = value;
        this.owner = value.parent();
        this.title = value.title();
        this.description = value.description();
        this.editable = !(value instanceof ConfigOption.UnsupportedValue);
        this.control = ConfigControls.create(value, session, this::onEdited);

        this.resetButton = new IconButton(0, 0, RESET_WIDTH, CONTROL_HEIGHT, Component.empty(),
                RESET_ICON, 12, 12, b -> rollback());
        this.resetButton.setTooltip(Tooltip.create(Component.translatable("gui.moonlight.config.reset")));

        List<AbstractWidget> kids = new ArrayList<>();
        if (description != null) {
            this.expandButton = Button.builder(Component.literal(arrowGlyph()), b -> view.toggleExpanded(value))
                    .bounds(0, 0, ARROW_WIDTH, CONTROL_HEIGHT).build();
            kids.add(expandButton);
        } else {
            this.expandButton = null;
        }
        kids.add(control.widget());
        kids.add(resetButton);
        this.children = List.copyOf(kids);
        refreshReset();
    }

    private String arrowGlyph() {
        return session.isExpanded(value) ? "▼" : "▶";
    }

    private void onEdited() {
        refreshReset();
        view.onValueEdited();
    }

    private void refreshReset() {
        this.resetButton.active = editable && !Objects.equals(session.currentRaw(value), value.defaultValue());
    }

    private void rollback() {
        if (!editable) return;
        Object def = value.defaultValue();
        session.put(value, def);
        control.setDisplayed().accept(def);
        onEdited();
    }

    @Override
    public void render(GuiGraphics graphics, int index, int top, int left, int width, int height,
                       int mouseX, int mouseY, boolean hovering, float partialTick) {
        Font font = view.font();
        int cy = top + (height - CONTROL_HEIGHT) / 2;
        // greyed while the owning category's feature toggle (or an ancestor's) is off
        boolean contextEnabled = owner == null || view.isCategoryEnabled(owner);

        int resetX = left + width - resetButton.getWidth();
        resetButton.setX(resetX);
        resetButton.setY(cy);
        resetButton.active = editable && contextEnabled && !Objects.equals(session.currentRaw(value), value.defaultValue());
        resetButton.render(graphics, mouseX, mouseY, partialTick);

        AbstractWidget w = control.widget();
        w.active = editable && contextEnabled;
        int controlX = resetX - GAP - w.getWidth();
        w.setX(controlX);
        w.setY(top + (height - w.getHeight()) / 2);
        w.render(graphics, mouseX, mouseY, partialTick);

        int labelLeft = left;
        if (expandButton != null) {
            expandButton.setX(left);
            expandButton.setY(cy);
            expandButton.render(graphics, mouseX, mouseY, partialTick);
            labelLeft = left + ARROW_WIDTH + 2;
        }
        int labelRight = controlX - GAP;
        // reload/restart hint icon: pure decoration, appended in the left gutter so it never shifts the row content
        this.reloadIconX0 = this.reloadIconX1 = -1;
        ResourceLocation reloadIcon = ConfigScreenLayout.reloadIcon(value.reloadType());
        if (reloadIcon != null) {
            int iconSize = 9;
            int iconX = left - iconSize - 3;
            graphics.blitSprite(reloadIcon, iconX, top + (height - iconSize) / 2, iconSize, iconSize);
            this.reloadIconX0 = iconX;
            this.reloadIconX1 = iconX + iconSize;
        }
        // amber label while this value has an unsaved edit; dimmed when its category is switched off
        boolean modified = !java.util.Objects.equals(session.currentRaw(value), value.get());
        int labelColor = !contextEnabled ? DESCRIPTION_COLOR : modified ? MODIFIED_COLOR : LABEL_COLOR;
        renderScrollingText(graphics, font, title, labelLeft, labelRight, top, height, labelColor);

        this.labelX0 = labelLeft;
        this.labelX1 = Math.min(labelLeft + font.width(title), labelRight);
        this.labelY0 = top;
        this.labelY1 = top + height;
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
        // only when collapsed and only over the label text itself (once expanded the inline description is the surface)
        if (description == null || session.isExpanded(value)) return null;
        boolean overLabel = mouseX >= labelX0 && mouseX <= labelX1 && mouseY >= labelY0 && mouseY <= labelY1;
        return overLabel ? description : null;
    }

    @Nullable
    @Override
    Component getGutterTooltip(int mouseX, int mouseY) {
        // the reload/restart hint lives in the left gutter, outside the row's normal hover band
        if (reloadIconX0 >= 0 && mouseX >= reloadIconX0 && mouseX <= reloadIconX1 && mouseY >= labelY0 && mouseY <= labelY1) {
            return reloadTooltip(value.reloadType());
        }
        return null;
    }

    private static Component reloadTooltip(ConfigReloadType type) {
        return Component.translatable(type == ConfigReloadType.GAME_RESTART
                ? "gui.moonlight.config.reload.game" : "gui.moonlight.config.reload.world");
    }
}

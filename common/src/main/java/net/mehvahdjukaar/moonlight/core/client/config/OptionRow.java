package net.mehvahdjukaar.moonlight.core.client.config;

import net.mehvahdjukaar.moonlight.api.client.gui.ConfigEditSession;
import net.mehvahdjukaar.moonlight.api.client.gui.GuiHelper;
import net.mehvahdjukaar.moonlight.api.client.gui.ConfigVisuals;
import net.mehvahdjukaar.moonlight.api.client.gui.widget.IconButton;
import net.mehvahdjukaar.moonlight.api.platform.configs.options.ConfigCategory;
import net.mehvahdjukaar.moonlight.api.platform.configs.options.ConfigOption;
import net.mehvahdjukaar.moonlight.api.platform.configs.options.ConfigReloadType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;

import static net.mehvahdjukaar.moonlight.core.client.config.ConfigScreenLayout.*;
import static net.mehvahdjukaar.moonlight.api.client.gui.misc.ConfigGuiColors.*;

/**
 * A single editable config value on the main config screen: a single-line name with the editing control from
 * {@link ConfigControls} pinned to the right and an icon reset button. Values that have a description show a
 * disclosure triangle in the left gutter; clicking the label area toggles it, dropping the wrapped description
 * beneath the row as read-only {@link DescriptionRow}s. A reload/restart hint sprite sits in the far-left gutter.
 */
class OptionRow extends ConfigListRow {

    private final ConfigScreenAccess view;
    private final ConfigEditSession session;
    private final ConfigOption<?> value;
    @Nullable
    private final ConfigCategory owner; // the category this value lives under, for feature-gating greyout
    private final boolean isGate; // this value IS its category's feature() toggle (the "enabled" switch)
    private final boolean asToggle; // drawn as the ✓/✗ feature toggle (a category gate, or a named feature leaf)
    private final Component title;
    @Nullable
    private final Component description;
    private final ConfigVisuals<?> control;
    private final IconButton resetButton;
    private final boolean editable;
    private final List<AbstractWidget> children;
    private final ConfigScreenIcons.Anim iconAnim = new ConfigScreenIcons.Anim();

    // click region of the label/arrow column that toggles the description, refreshed each frame
    private int toggleX0, toggleX1, rowY0, rowY1;
    // hover region of the reload/restart hint icon, if any
    private int reloadIconX0 = -1, reloadIconX1 = -1;

    OptionRow(ConfigScreenAccess view, ConfigOption<?> value) {
        this.view = view;
        this.session = view.session();
        this.value = value;
        this.owner = value.parent();
        this.isGate = owner != null && owner.gate() == value;
        this.title = value.title();
        this.description = value.description();
        this.editable = !(value instanceof ConfigOption.UnsupportedValue);
        // both the category "enabled" gate and a named feature leaf render as the ✓/✗ toggle (icon beside the symbol)
        this.asToggle = isGate || (value instanceof ConfigOption.BooleanValue bv && bv.isFeature());
        this.control = asToggle
                ? ConfigControls.featureToggle((ConfigOption.BooleanValue) value, session, this::onEdited)
                : ConfigControls.create(value, session, this::onEdited);

        this.resetButton = new IconButton(0, 0, RESET_WIDTH, CONTROL_HEIGHT, Component.empty(),
                RESET_ICON, 12, 12, b -> rollback());
        this.resetButton.setTooltip(Tooltip.create(Component.translatable("gui.moonlight.config.reset")));

        this.children = List.of(control.widget(), resetButton);
        refreshReset();
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
        control.set(def);
        onEdited();
    }

    private boolean hasDescription() {
        return description != null;
    }

    @Override
    public void render(GuiGraphics graphics, int index, int top, int left, int width, int height,
                       int mouseX, int mouseY, boolean hovering, float partialTick) {
        Font font = view.font();
        int cy = top + (height - CONTROL_HEIGHT) / 2;
        // greyed while the owning category's feature toggle (or an ancestor's) is off; the gate row itself only
        // dims when an ANCESTOR is off, so turning it off doesn't grey out (and lock away) its own switch
        boolean contextEnabled = owner == null || (isGate ? view.areAncestorsEnabled(owner) : view.isCategoryEnabled(owner));

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

        int textLeft = left;
        if (hasDescription()) {
            boolean expanded = session.isExpanded(value);
            ResourceLocation arrow = expanded ? SECTION_EXPANDED_ICON : SECTION_COLLAPSED_ICON;
            int arrowSize = 7;
            if (!contextEnabled) graphics.setColor(0.5f, 0.5f, 0.5f, 1f);
            graphics.blitSprite(arrow, left + 2, top + (height - arrowSize) / 2, arrowSize, arrowSize);
            if (!contextEnabled) graphics.setColor(1f, 1f, 1f, 1f);
            textLeft = left + ARROW_WIDTH;
        }
        // decorative, hover-animated item/block icon just before the label, if this value declares one. Feature
        // toggles draw their icon inside the control (next to the ✓/✗ symbol) instead, so skip it here for them.
        if (!asToggle && ConfigScreenIcons.has(value.icon())) {
            iconAnim.update(hovering);
            ConfigScreenIcons.renderAnimated(graphics, value.icon(), textLeft, top + (height - ROW_ICON) / 2,
                    iconAnim.phase(), contextEnabled);
            textLeft += ROW_ICON + GAP;
        }
        int textRight = controlX - GAP;

        // reload/restart hint icon: pure decoration in the far-left gutter, never shifts the row content
        this.reloadIconX0 = this.reloadIconX1 = -1;
        ResourceLocation reloadIcon = ConfigScreenLayout.reloadIcon(value.reloadType());
        if (reloadIcon != null) {
            int iconSize = 8; // native size of the world_reload / game_restart sprites (pixel-perfect)
            int iconX = left - iconSize - 3;
            graphics.blitSprite(reloadIcon, iconX, top + (height - iconSize) / 2, iconSize, iconSize);
            this.reloadIconX0 = iconX;
            this.reloadIconX1 = iconX + iconSize;
        }

        boolean modified = !Objects.equals(session.currentRaw(value), value.get());
        int titleColor = !contextEnabled ? DESCRIPTION : modified ? MODIFIED : LABEL;
        GuiHelper.renderScrollingText(graphics, font, title, textLeft, textRight, top, height, titleColor);

        this.toggleX0 = left;
        this.toggleX1 = textRight;
        this.rowY0 = top;
        this.rowY1 = top + height;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (hasDescription() && button == 0
                && mouseX >= toggleX0 && mouseX < toggleX1 && mouseY >= rowY0 && mouseY < rowY1) {
            view.toggleExpanded(value);
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
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
        // description is revealed by expanding the row, not as a tooltip
        return null;
    }

    @Nullable
    @Override
    Component getGutterTooltip(int mouseX, int mouseY) {
        // the reload/restart hint lives in the far-left gutter, outside the row's normal hover band
        if (reloadIconX0 >= 0 && mouseX >= reloadIconX0 && mouseX <= reloadIconX1 && mouseY >= rowY0 && mouseY <= rowY1) {
            return reloadTooltip(value.reloadType());
        }
        return null;
    }

    private static Component reloadTooltip(ConfigReloadType type) {
        return Component.translatable(type == ConfigReloadType.GAME_RESTART
                ? "gui.moonlight.config.reload.game" : "gui.moonlight.config.reload.world");
    }
}

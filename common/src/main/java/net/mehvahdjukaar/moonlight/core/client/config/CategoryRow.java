package net.mehvahdjukaar.moonlight.core.client.config;

import net.mehvahdjukaar.moonlight.api.client.gui.GuiHelper;
import net.mehvahdjukaar.moonlight.api.client.gui.widget.BooleanToggleWidget;
import net.mehvahdjukaar.moonlight.api.platform.configs.options.ConfigCategory;
import net.mehvahdjukaar.moonlight.api.platform.configs.options.ConfigOption;
import net.mehvahdjukaar.moonlight.api.client.gui.MoonlightIcons;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static net.mehvahdjukaar.moonlight.core.client.config.ConfigScreenLayout.*;
import static net.mehvahdjukaar.moonlight.api.client.gui.misc.ConfigGuiColors.*;

class CategoryRow extends ConfigListRow {

    private final ConfigScreenAccess view;
    private final ConfigCategory category;
    private final Button button;
    @Nullable
    private final ConfigOption.BooleanValue gate;
    @Nullable
    private final BooleanToggleWidget toggle;
    private final List<AbstractWidget> children;
    @Nullable
    private final Component tooltip;
    private final ConfigScreenIcons.Anim iconAnim = new ConfigScreenIcons.Anim();

    CategoryRow(ConfigScreenAccess view, ConfigCategory category) {
        this.view = view;
        this.category = category;
        this.tooltip = category.description();
        this.gate = category.gate();
        this.button = Button.builder(Component.empty(), b -> view.openCategory(category))
                .bounds(0, 0, ROW_WIDTH, ITEM_HEIGHT).build();
        if (gate != null) {
            this.toggle = new BooleanToggleWidget(CONTROL_HEIGHT, CONTROL_HEIGHT, MoonlightIcons.YES, MoonlightIcons.NO,
                    Boolean.TRUE.equals(view.session().current(gate)), val -> {
                view.session().put(gate, val);
                view.onValueEdited();
            });
            this.children = List.of(button, toggle);
        } else {
            this.toggle = null;
            this.children = List.of(button);
        }
    }

    @Override
    public void render(GuiGraphics graphics, int index, int top, int left, int width, int height,
                       int mouseX, int mouseY, boolean hovering, float partialTick) {
        Font font = view.font();
        int cy = top + (height - CONTROL_HEIGHT) / 2;
        boolean enabled = view.isCategoryEnabled(category);

        int buttonWidth = toggle != null ? width - CONTROL_HEIGHT - GAP : width;
        button.setMessage(Component.empty()); // we draw our own icon + label over the (empty) button background
        button.setX(left);
        button.setWidth(buttonWidth);
        button.setY(top);
        button.setHeight(height);
        button.render(graphics, mouseX, mouseY, partialTick);

        int iconX = left + 6;
        int textLeft = iconX + ROW_ICON + 6;
        int textRight = left + buttonWidth - GAP;
        int titleColor = enabled ? LABEL : DESCRIPTION; // white (bold), greyed when the feature is off

        int iconY = top + (height - ROW_ICON) / 2;
        // an animated item/block icon if the category declares one, otherwise the default folder sprite
        if (ConfigScreenIcons.has(category.icon())) {
            iconAnim.update(hovering);
            ConfigScreenIcons.renderAnimated(graphics, category.icon(), iconX, iconY, iconAnim.phase(), enabled);
        } else {
            graphics.blitSprite(MoonlightIcons.FOLDER, iconX, iconY, ROW_ICON, ROW_ICON);
        }
        Component title = category.title().copy().withStyle(ChatFormatting.BOLD);
        GuiHelper.renderScrollingText(graphics, font, title, textLeft, textRight, top, height, titleColor);

        if (toggle != null && gate != null) {
            toggle.set(Boolean.TRUE.equals(view.session().current(gate)));
            toggle.active = view.areAncestorsEnabled(category); // can't enable a sub-feature of a disabled feature
            toggle.setX(left + width - CONTROL_HEIGHT);
            toggle.setY(cy);
            toggle.render(graphics, mouseX, mouseY, partialTick);
        }
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

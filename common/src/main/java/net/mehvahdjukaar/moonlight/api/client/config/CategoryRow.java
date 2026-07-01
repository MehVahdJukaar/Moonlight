package net.mehvahdjukaar.moonlight.api.client.config;

import net.mehvahdjukaar.moonlight.api.platform.configs.options.ConfigCategory;
import net.mehvahdjukaar.moonlight.api.platform.configs.options.ConfigOption;
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
 * One full width button that opens a sub category. If the category declares a {@code feature()} toggle, that
 * boolean is edited inline here (right-aligned) instead of as a row inside the category; the button label dims when
 * the category is effectively off (its own toggle or an ancestor's), and the toggle itself is disabled when an
 * ancestor is off (you can't enable a sub-feature of a disabled feature).
 */
class CategoryRow extends ConfigRow {

    private final ConfigScreenView view;
    private final ConfigCategory category;
    private final Button button;
    @Nullable
    private final ConfigOption.BooleanValue gate;
    @Nullable
    private final BooleanToggleWidget toggle;
    private final List<AbstractWidget> children;
    @Nullable
    private final Component tooltip;

    CategoryRow(ConfigScreenView view, ConfigCategory category) {
        this.view = view;
        this.category = category;
        this.tooltip = category.description();
        this.gate = category.gate();
        this.button = Button.builder(label(true), b -> view.openCategory(category))
                .bounds(0, 0, ROW_WIDTH, CONTROL_HEIGHT).build();
        if (gate != null) {
            this.toggle = new BooleanToggleWidget(CONTROL_HEIGHT, CONTROL_HEIGHT,
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

    /** Accent-colored label + a trailing chevron; dimmed when the category is switched off. */
    private Component label(boolean enabled) {
        int labelColor = enabled ? CATEGORY_COLOR : DESCRIPTION_COLOR;
        return Component.empty()
                .append(category.title().copy().withStyle(Style.EMPTY.withColor(TextColor.fromRgb(labelColor))))
                .append(Component.literal("  ›").withStyle(Style.EMPTY.withColor(TextColor.fromRgb(CRUMB_SEPARATOR_COLOR))));
    }

    @Override
    public void render(GuiGraphics graphics, int index, int top, int left, int width, int height,
                       int mouseX, int mouseY, boolean hovering, float partialTick) {
        int cy = top + (height - CONTROL_HEIGHT) / 2;
        boolean enabled = view.isCategoryEnabled(category);

        int buttonWidth = toggle != null ? width - CONTROL_HEIGHT - GAP : width;
        button.setMessage(label(enabled));
        button.setX(left);
        button.setWidth(buttonWidth);
        button.setY(cy);
        button.render(graphics, mouseX, mouseY, partialTick);

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

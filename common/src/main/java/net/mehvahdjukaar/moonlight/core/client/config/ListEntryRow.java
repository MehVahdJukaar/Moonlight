package net.mehvahdjukaar.moonlight.core.client.config;

import net.mehvahdjukaar.moonlight.api.client.gui.widget.IconButton;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

import static net.mehvahdjukaar.moonlight.core.client.config.ConfigScreenLayout.*;

/**
 * One entry of a {@link SchemaForm.ListCategory} page: the row the entry would normally get ({@link CategoryRow} for a
 * record element, {@link OptionRow} for a scalar one) rendered slightly narrower, with a delete button in the freed
 * space. Purely a decorator - it owns no editing state of its own, so every control keeps behaving exactly as it does
 * on a normal page.
 */
class ListEntryRow extends ConfigListRow {

    private final ConfigListRow inner;
    private final IconButton remove;
    private final List<GuiEventListener> children;
    private final List<NarratableEntry> narratables;

    ListEntryRow(ConfigListRow inner, boolean canRemove, Runnable onRemove) {
        this.inner = inner;
        this.remove = new IconButton(0, 0, RESET_WIDTH, CONTROL_HEIGHT, Component.empty(),
                DELETE_ICON, 12, 12, b -> onRemove.run());
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
    public void render(GuiGraphics graphics, int index, int top, int left, int width, int height,
                       int mouseX, int mouseY, boolean hovering, float partialTick) {
        inner.render(graphics, index, top, left, width - RESET_WIDTH - GAP, height,
                mouseX, mouseY, hovering, partialTick);
        remove.setX(left + width - RESET_WIDTH);
        remove.setY(top + (height - CONTROL_HEIGHT) / 2);
        remove.render(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        // give the wrapped row its own handling first (OptionRow toggles its description on a label click); only if it
        // passes does the default child dispatch run, so nothing is handled twice
        return inner.mouseClicked(mouseX, mouseY, button) || super.mouseClicked(mouseX, mouseY, button);
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

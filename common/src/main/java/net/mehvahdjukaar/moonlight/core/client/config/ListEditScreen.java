package net.mehvahdjukaar.moonlight.core.client.config;

import net.mehvahdjukaar.moonlight.api.client.gui.GuiHelper;
import net.mehvahdjukaar.moonlight.api.client.gui.screen.ColorPickerScreen;
import net.mehvahdjukaar.moonlight.api.client.gui.misc.ConfigGuiColors;
import net.mehvahdjukaar.moonlight.api.client.gui.widget.DropdownWidget;
import net.mehvahdjukaar.moonlight.api.client.gui.widget.IconButton;
import net.mehvahdjukaar.moonlight.api.client.gui.OverlayLayer;
import net.mehvahdjukaar.moonlight.api.client.gui.PopupHost;
import net.mehvahdjukaar.moonlight.api.platform.configs.options.ConfigOption;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static net.mehvahdjukaar.moonlight.core.client.config.ConfigScreenLayout.*;

class ListEditScreen extends Screen implements PopupHost {

    private final Screen parent;
    private final Consumer<List<String>> onApply;
    private final ConfigOption.ListValue option;
    private final List<String> working;
    @Nullable
    private final List<String> options; // non-null -> entries are picked with a dropdown
    private final OverlayLayer overlay = new OverlayLayer();

    private ConfigRowList list;

    ListEditScreen(ConfigOption.ListValue option, List<String> initial, Screen parent, Consumer<List<String>> onApply) {
        super(option.title());
        this.option = option;
        this.working = new ArrayList<>(initial);
        this.parent = parent;
        this.onApply = onApply;
        this.options = option.options == null ? null : option.options.get();
    }

    @Override
    public OverlayLayer getOverlayLayer() {
        return this.overlay;
    }

    @Override
    protected void init() {
        this.overlay.clear();
        this.list = new ConfigRowList(this.minecraft, this.width, this.height - HEADER - 58, HEADER, 24);
        rebuildRows();
        this.addRenderableWidget(this.list);

        int cx = this.width / 2;
        Component addLabel = Component.literal("+ ").withStyle(ChatFormatting.AQUA)
                .append(Component.translatable("gui.moonlight.config.list_add").withStyle(ChatFormatting.RESET));
        this.addRenderableWidget(Button.builder(addLabel, b -> {
            working.add(options != null && !options.isEmpty() ? options.getFirst() : "");
            rebuildRows();
            this.list.setScrollAmount(this.list.getMaxScroll());
        }).bounds(cx - 100, this.height - 52, 200, 20).build());

        this.addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, b -> {
            onApply.accept(new ArrayList<>(working));
            onClose();
        }).bounds(cx - 100, this.height - 28, 96, 20).build());
        this.addRenderableWidget(Button.builder(CommonComponents.GUI_CANCEL, b -> onClose())
                .bounds(cx + 4, this.height - 28, 96, 20).build());
    }

    private void rebuildRows() {
        this.overlay.clear(); // rows (and their dropdowns) are recreated here
        List<ConfigListRow> rows = new ArrayList<>();
        for (int i = 0; i < working.size(); i++) {
            rows.add(new EntryRow(i));
        }
        this.list.setRows(rows);
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(parent);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (overlay.mouseClicked(mouseX, mouseY, button)) return true;
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (overlay.mouseScrolled(mouseX, mouseY, scrollY)) return true;
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    @Override
    public boolean keyPressed(int key, int scanCode, int modifiers) {
        if (overlay.keyPressed(key, scanCode, modifiers)) return true;
        return super.keyPressed(key, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char c, int modifiers) {
        if (overlay.charTyped(c, modifiers)) return true;
        return super.charTyped(c, modifiers);
    }

    @Override
    public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.renderBackground(graphics, mouseX, mouseY, partialTick);
        // header chrome in the background layer, behind the widgets (the list draws only its footer separator)
        GuiHelper.renderHeaderBar(graphics, this.font, this.title, this.width, HEADER);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        this.overlay.render(graphics, mouseX, mouseY); // open dropdown popup floats on top
    }

    private class EntryRow extends ConfigListRow {
        private final AbstractWidget editor;
        @Nullable
        private final EditBox box; // set only for free-text entries, for validity coloring
        private final Button remove;
        private final List<AbstractWidget> children;
        private final int index;

        EntryRow(int index) {
            this.index = index;
            int editorWidth = ROW_WIDTH - RESET_WIDTH - GAP;
            if (options != null) {
                this.box = null;
                this.editor = new DropdownWidget(editorWidth, CONTROL_HEIGHT, options, option.icon,
                        working.get(index), v -> working.set(index, v));
            } else {
                EditBox b = new EditBox(ListEditScreen.this.font, 0, 0, editorWidth, CONTROL_HEIGHT, Component.empty());
                b.setMaxLength(Short.MAX_VALUE);
                b.setValue(working.get(index));
                b.setResponder(s -> {
                    working.set(index, s);
                    b.setTextColor(option.isValidEntry(b.getValue()) ? ConfigGuiColors.TEXT : ConfigGuiColors.ERROR);
                });
                b.setTextColor(option.isValidEntry(b.getValue()) ? ConfigGuiColors.TEXT : ConfigGuiColors.ERROR);
                this.box = b;
                this.editor = b;
            }
            this.remove = new IconButton(0, 0, RESET_WIDTH, CONTROL_HEIGHT, Component.empty(),
                    DELETE_ICON, 12, 12, btn -> {
                working.remove(index);
                rebuildRows();
            });
            this.children = List.of(editor, remove);
        }

        @Override
        public void render(GuiGraphics graphics, int i, int top, int left, int width, int height,
                           int mouseX, int mouseY, boolean hovering, float partialTick) {
            int cy = top + (height - CONTROL_HEIGHT) / 2;
            editor.setX(left);
            editor.setY(cy);
            editor.render(graphics, mouseX, mouseY, partialTick);
            remove.setX(left + width - RESET_WIDTH);
            remove.setY(cy);
            remove.render(graphics, mouseX, mouseY, partialTick);
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
            return null;
        }
    }
}

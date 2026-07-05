package net.mehvahdjukaar.moonlight.core.client.config;

import com.mojang.blaze3d.systems.RenderSystem;
import net.mehvahdjukaar.moonlight.api.client.gui.screen.ColorPickerScreen;
import net.mehvahdjukaar.moonlight.api.client.gui.misc.ConfigGuiColors;
import net.mehvahdjukaar.moonlight.api.client.gui.widget.DropdownWidget;
import net.mehvahdjukaar.moonlight.api.client.gui.widget.IconButton;
import net.mehvahdjukaar.moonlight.api.client.gui.OverlayLayer;
import net.mehvahdjukaar.moonlight.api.client.gui.PopupHost;
import net.mehvahdjukaar.moonlight.api.platform.configs.options.ConfigOption;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static net.mehvahdjukaar.moonlight.core.client.config.ConfigScreenLayout.*;

/**
 * A dedicated page for editing a string list: one editor per entry (a free text field, or a dropdown picker when
 * the list is option-backed) with a remove button, plus an add button. Follows the same "edit on a sub page, hand
 * the result back on Done" shape as {@link ColorPickerScreen}. Hosts dropdown popups the same way the main config
 * screen does (see {@link OverlayLayer}).
 */
class ListEditScreen extends Screen implements PopupHost {

    private final Screen parent;
    private final Consumer<List<String>> onApply;
    private final ConfigOption.ListValue option;
    private final List<String> working;
    @Nullable
    private final List<String> options; // non-null -> entries are picked with a dropdown
    private final OverlayLayer overlay = new OverlayLayer();

    private EntryList list;

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
        this.list = new EntryList(this.minecraft, this.width, this.height - HEADER - 58, HEADER, 24);
        rebuildRows();
        this.addRenderableWidget(this.list);

        int cx = this.width / 2;
        this.addRenderableWidget(Button.builder(Component.translatable("gui.moonlight.config.list_add"), b -> {
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
        List<EntryRow> rows = new ArrayList<>();
        for (int i = 0; i < working.size(); i++) {
            rows.add(new EntryRow(i));
        }
        this.list.replaceEntries(rows);
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
        graphics.fill(0, 0, this.width, HEADER, ConfigGuiColors.HEADER_BG);
        graphics.fill(0, HEADER - 1, this.width, HEADER, ConfigGuiColors.HEADER_SEPARATOR);
        graphics.drawCenteredString(this.font, this.title, this.width / 2, (HEADER - this.font.lineHeight) / 2, ConfigGuiColors.TITLE);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        this.overlay.render(graphics, mouseX, mouseY); // open dropdown popup floats on top
    }

    private class EntryList extends ContainerObjectSelectionList<EntryRow> {
        EntryList(Minecraft minecraft, int width, int height, int y, int itemHeight) {
            super(minecraft, width, height, y, itemHeight);
        }

        void replaceEntries(List<EntryRow> rows) {
            this.clearEntries();
            rows.forEach(this::addEntry);
            this.clampScrollAmount();
        }

        @Override
        public int getRowWidth() {
            return ROW_WIDTH;
        }

        @Override
        protected int getScrollbarPosition() {
            return this.width / 2 + ROW_WIDTH / 2 + 6;
        }

        @Override
        protected void renderListSeparators(GuiGraphics graphics) {
            // the top separator is owned by the screen's header bar (drawn in renderBackground); only draw the footer
            ResourceLocation footer = this.minecraft.level == null ? Screen.FOOTER_SEPARATOR : Screen.INWORLD_FOOTER_SEPARATOR;
            RenderSystem.enableBlend();
            graphics.blit(footer, this.getX(), this.getBottom(), 0f, 0f, this.getWidth(), 2, 32, 2);
            RenderSystem.disableBlend();
        }
    }

    private class EntryRow extends ContainerObjectSelectionList.Entry<EntryRow> {
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
    }
}

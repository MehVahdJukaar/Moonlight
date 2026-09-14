package net.mehvahdjukaar.moonlight.api.client.gui.widget;

import com.mojang.blaze3d.platform.InputConstants;
import net.mehvahdjukaar.moonlight.api.client.gui.AnchoredPopup;
import net.mehvahdjukaar.moonlight.api.client.gui.GuiHelper;
import net.mehvahdjukaar.moonlight.api.client.gui.misc.ConfigGuiColors;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.util.CommonColors;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

public class DropdownWidget extends AbstractWidget {

    private static final int MAX_VISIBLE = 8;

    private final List<String> options;
    @Nullable
    private final Function<String, ItemStack> iconGetter;
    private final Map<String, ItemStack> iconCache = new HashMap<>();
    private final int itemHeight;
    private String value;
    private final Consumer<String> onChange;

    private final EditBox searchBox;
    private final ListPopup popup;

    public DropdownWidget(int width, int height, List<String> options, @Nullable Function<String, ItemStack> icon,
                          String value, Consumer<String> onChange) {
        super(0, 0, width, height, Component.literal(value));
        this.options = options;
        this.iconGetter = icon;
        this.value = value;
        this.onChange = onChange;
        this.itemHeight = icon != null ? 18 : 14;
        this.popup = new ListPopup();

        this.searchBox = new EditBox(font(), 0, 0, width, height, Component.empty());
        this.searchBox.setBordered(false);
        this.searchBox.setTextColor(ConfigGuiColors.TEXT);
        this.searchBox.setResponder(popup::filter);
    }

    public void setValue(String v) {
        this.value = v;
    }

    public String getValue() {
        return value;
    }

    private static Font font() {
        return Minecraft.getInstance().font;
    }

    private ItemStack iconFor(String id) {
        return iconCache.computeIfAbsent(id, iconGetter);
    }

    private int valueAreaWidth() {
        return getWidth() - getHeight(); // the right square is the arrow box
    }

    @Override
    protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        int x = getX(), y = getY(), w = getWidth(), h = getHeight();
        boolean open = popup.isOpen();
        int border = (open || isFocused()) ? CommonColors.WHITE : CommonColors.LIGHT_GRAY;
        Font font = font();

        graphics.fill(x, y, x + w, y + h, CommonColors.BLACK);

        int arrowBox = h;
        int sepX = x + w - arrowBox;

        if (open) {
            this.searchBox.setPosition(x + 4, y + (h - font.lineHeight) / 2 + 1);
            this.searchBox.setWidth(sepX - 2 - (x + 4));
            this.searchBox.setHeight(font.lineHeight);
            this.searchBox.render(graphics, mouseX, mouseY, partialTick);
        } else {
            int textX = x + 4;
            if (iconGetter != null) {
                graphics.renderFakeItem(iconFor(value), x + 2, y + (h - 16) / 2);
                textX = x + 2 + 18;
            }
            // scroll the value like an option-row label when it's wider than the box
            GuiHelper.renderScrollingText(graphics, font, Component.literal(value), textX, sepX - 2, y, h, ConfigGuiColors.TEXT);
        }

        graphics.fill(sepX, y, sepX + 1, y + h, border); // separator, same color as the outline
        graphics.drawCenteredString(font, open ? "▲" : "▼", sepX + arrowBox / 2, y + (h - font.lineHeight) / 2 + 1, ConfigGuiColors.TEXT);
        graphics.renderOutline(x, y, w, h, border);
    }

    @Override
    public void onClick(double mouseX, double mouseY) {
        if (popup.isOpen()) popup.close();
        else popup.open();
    }

    private void select(String v) {
        GuiHelper.playClickSound();
        // the popup is drawn by the overlay layer, so it gets no widget click sound
        this.value = v;
        onChange.accept(v);
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
    }

    private class ListPopup extends AnchoredPopup {

        private List<String> filtered = options;
        private int scrollOffset;

        ListPopup() {
            super(DropdownWidget.this, 0, 0);
        }

        @Override
        protected void onOpened() {
            this.filtered = options;
            searchBox.setValue("");
            searchBox.setHint(Component.literal(value));
            searchBox.setFocused(true);
            layout();
            int selected = filtered.indexOf(value);
            this.scrollOffset = selected < 0 ? 0 : Mth.clamp(selected - visibleCount() + 1, 0, maxScroll());
        }

        @Override
        public void onPopupClosed() {
            super.onPopupClosed();
            searchBox.setFocused(false);
        }

        private void filter(String query) {
            String q = query.trim().toLowerCase(Locale.ROOT);
            this.filtered = q.isEmpty() ? options
                    : options.stream().filter(o -> o.toLowerCase(Locale.ROOT).contains(q)).toList();
            this.scrollOffset = 0;
            layout();
        }

        // as tall as the rows it has, capped to the side of the anchor with more room when neither side holds them all
        private void layout() {
            this.width = getWidth();
            int desired = Mth.clamp(filtered.size(), 1, MAX_VISIBLE);
            int screenH = Minecraft.getInstance().getWindow().getGuiScaledHeight();
            int fitBelow = Math.max(1, (screenH - MARGIN - getY() - getHeight()) / itemHeight);
            int fitAbove = Math.max(1, (getY() - MARGIN) / itemHeight);
            int visible = desired <= fitBelow || desired <= fitAbove ? desired : Math.max(fitBelow, fitAbove);
            this.height = visible * itemHeight;
        }

        private int visibleCount() {
            return height / itemHeight;
        }

        private int maxScroll() {
            return Math.max(0, filtered.size() - visibleCount());
        }

        @Override
        protected void renderContent(GuiGraphics graphics, int x, int y, int mouseX, int mouseY) {
            Font font = font();
            if (filtered.isEmpty()) {
                graphics.drawString(font, Component.translatable("gui.moonlight.config.no_matches"),
                        x + 4, y + (itemHeight - font.lineHeight) / 2 + 1, ConfigGuiColors.DESCRIPTION);
            }

            int visible = visibleCount();
            boolean hasScrollbar = filtered.size() > visible;
            int textRight = x + width - (hasScrollbar ? 6 : 4);
            for (int i = 0; i < visible; i++) {
                int idx = scrollOffset + i;
                if (idx >= filtered.size()) break;
                String opt = filtered.get(idx);
                int iy = y + i * itemHeight;
                boolean hover = GuiHelper.isMouseOver(mouseX, mouseY, x, iy, width, itemHeight);
                if (hover) graphics.fill(x + 1, iy, x + width - 1, iy + itemHeight, ConfigGuiColors.HOVER_HIGHLIGHT);
                int textX = x + 4;
                if (iconGetter != null) {
                    graphics.renderFakeItem(iconFor(opt), x + 2, iy + (itemHeight - 16) / 2);
                    textX = x + 2 + 18;
                }
                int color = opt.equals(value) ? ConfigGuiColors.SELECTED : ConfigGuiColors.TEXT;
                if (hover) {
                    GuiHelper.renderScrollingText(graphics, font, Component.literal(opt), textX, textRight, iy, itemHeight, color);
                } else {
                    graphics.enableScissor(textX, iy, textRight, iy + itemHeight);
                    graphics.drawString(font, opt, textX, iy + (itemHeight - font.lineHeight) / 2 + 1, color);
                    graphics.disableScissor();
                }
            }

            if (hasScrollbar) {
                int trackX = x + width - 4;
                int thumbH = Math.max(8, height * visible / filtered.size());
                int thumbY = y + (height - thumbH) * scrollOffset / maxScroll();
                graphics.fill(trackX, y, trackX + 2, y + height, CommonColors.BLACK);
                graphics.fill(trackX, thumbY, trackX + 2, thumbY + thumbH, ConfigGuiColors.SCROLLBAR_THUMB);
            }
        }

        @Override
        protected void clickContent(double mouseX, double mouseY, int x, int y) {
            int idx = scrollOffset + (int) ((mouseY - y) / itemHeight);
            if (idx >= 0 && idx < filtered.size()) select(filtered.get(idx));
            close();
        }

        // clicking in the search area just moves the caret, so keep the popup open
        @Override
        protected void clickOutside(double mouseX, double mouseY, int button) {
            if (GuiHelper.isMouseOver(mouseX, mouseY, getX(), getY(), valueAreaWidth(), getHeight())) {
                searchBox.mouseClicked(mouseX, mouseY, button);
            } else {
                close();
            }
        }

        @Override
        public boolean popupMouseScrolled(double mouseX, double mouseY, double delta) {
            if (filtered.size() <= visibleCount()) return false;
            if (!GuiHelper.isMouseOver(mouseX, mouseY, x(), y(), width, height)) return false;
            this.scrollOffset = Mth.clamp(scrollOffset - (int) Math.signum(delta), 0, maxScroll());
            return true;
        }

        @Override
        public boolean popupKeyPressed(int key, int scanCode, int modifiers) {
            if (super.popupKeyPressed(key, scanCode, modifiers)) return true;
            if (key == InputConstants.KEY_RETURN || key == InputConstants.KEY_NUMPADENTER) {
                if (!filtered.isEmpty()) select(filtered.contains(value) ? value : filtered.getFirst());
                close();
                return true;
            }
            return searchBox.keyPressed(key, scanCode, modifiers);
        }

        @Override
        public boolean popupCharTyped(char c, int modifiers) {
            return searchBox.charTyped(c, modifiers);
        }
    }
}

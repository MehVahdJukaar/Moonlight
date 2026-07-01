package net.mehvahdjukaar.moonlight.api.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * A combo box / picker styled like a text field: a box holding the current value, with the right edge split off by
 * a vertical line into a small box holding a downward arrow. Clicking it expands a scrollable list of the options
 * below (or above, if there's no room), and the value area turns into a search box that filters the list. An
 * optional {@code icon} draws an item beside each option (item/block pickers).
 * <p>
 * Because a widget can live inside a scissored list (which clips by rectangle, not depth, so a Z offset alone can't
 * escape it), the expanded popup is drawn and click/scroll/key tested by the {@link Host} screen (which delegates
 * to a {@link DropdownPopup}) so it can float above everything. Any screen that hosts a dropdown must implement
 * {@link Host}, keep a {@link DropdownPopup}, forward its render/mouse/key events to it, and reset it in {@code init}.
 */
public class DropdownWidget extends AbstractWidget {

    private static final int TEXT_COLOR = 0xE0E0E0;
    private static final int DESCRIPTION_COLOR = 0xA0A0A0;
    private static final int SELECTED_COLOR = 0x9AD8FF;

    /** A screen that can float an open dropdown's popup above its content (see {@link DropdownPopup}). */
    public interface Host {
        void setOpenDropdown(@Nullable DropdownWidget dropdown);
    }

    private static final int MAX_VISIBLE = 8;

    private final List<String> options;
    @Nullable
    private final Function<String, ItemStack> icon;
    private final Map<String, ItemStack> iconCache = new HashMap<>();
    private final int itemHeight;
    private String value;
    private final Consumer<String> onChange;

    private boolean open;
    private int scrollOffset;
    private List<String> filtered;
    @Nullable
    private Host host;
    private final EditBox searchBox;

    public DropdownWidget(int width, int height, List<String> options, @Nullable Function<String, ItemStack> icon,
                          String value, Consumer<String> onChange) {
        super(0, 0, width, height, Component.literal(value));
        this.options = options;
        this.icon = icon;
        this.value = value;
        this.onChange = onChange;
        this.filtered = options;
        this.itemHeight = icon != null ? 18 : 14;

        this.searchBox = new EditBox(font(), 0, 0, width, height, Component.empty());
        this.searchBox.setBordered(false);
        this.searchBox.setTextColor(TEXT_COLOR);
        this.searchBox.setResponder(query -> {
            String q = query.trim().toLowerCase(Locale.ROOT);
            this.filtered = q.isEmpty() ? options
                    : options.stream().filter(o -> o.toLowerCase(Locale.ROOT).contains(q)).toList();
            this.scrollOffset = 0;
        });
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
        return iconCache.computeIfAbsent(id, icon);
    }

    private int valueAreaWidth() {
        return getWidth() - getHeight(); // the right square is the arrow box
    }

    // ===== closed box (rendered in the row) =====

    @Override
    protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        int x = getX(), y = getY(), w = getWidth(), h = getHeight();
        int border = (open || isFocused()) ? 0xFFFFFFFF : 0xFFA0A0A0;
        Font font = font();

        graphics.fill(x, y, x + w, y + h, 0xFF000000);

        int arrowBox = h;
        int sepX = x + w - arrowBox;

        if (open) {
            this.searchBox.setPosition(x + 4, y + (h - font.lineHeight) / 2 + 1);
            this.searchBox.setWidth(sepX - 2 - (x + 4));
            this.searchBox.setHeight(font.lineHeight);
            this.searchBox.render(graphics, mouseX, mouseY, partialTick);
        } else {
            int textX = x + 4;
            if (icon != null) {
                graphics.renderFakeItem(iconFor(value), x + 2, y + (h - 16) / 2);
                textX = x + 2 + 18;
            }
            // scroll the value like an option-row label when it's wider than the box
            GuiHelper.renderScrollingText(graphics, font, Component.literal(value), textX, sepX - 2, y, h, TEXT_COLOR);
        }

        graphics.fill(sepX, y, sepX + 1, y + h, border); // separator, same color as the outline
        graphics.drawCenteredString(font, open ? "▲" : "▼", sepX + arrowBox / 2, y + (h - font.lineHeight) / 2 + 1, TEXT_COLOR);
        graphics.renderOutline(x, y, w, h, border);
    }

    @Override
    public void onClick(double mouseX, double mouseY) {
        if (open) {
            close();
        } else if (Minecraft.getInstance().screen instanceof Host h) {
            this.host = h;
            this.open = true;
            this.filtered = options;
            this.searchBox.setValue("");
            this.searchBox.setHint(Component.literal(value));
            this.searchBox.setFocused(true);
            h.setOpenDropdown(this);
            int selected = filtered.indexOf(value);
            scrollOffset = selected < 0 ? 0 : Mth.clamp(selected - visibleCount() + 1, 0, maxScroll());
        }
    }

    public void close() {
        this.open = false;
        this.searchBox.setFocused(false);
        if (host != null) {
            host.setOpenDropdown(null);
            host = null;
        }
    }

    private int visibleCount() {
        return popupRect()[4];
    }

    private int maxScroll() {
        return Math.max(0, filtered.size() - visibleCount());
    }

    /** {x, y, w, h, visible} of the open popup. Opens downward when it fits (the natural direction), else toward the
     * side with more room; and caps the visible-row count to whatever the chosen side can hold so a dropdown opened
     * mid-screen shrinks instead of running off the top or bottom. */
    private int[] popupRect() {
        int w = getWidth();
        int x = getX();
        int desired = Mth.clamp(filtered.size(), 1, MAX_VISIBLE);
        int below = getY() + getHeight();
        int screenH = Minecraft.getInstance().getWindow().getGuiScaledHeight();
        int margin = 2;
        int fitBelow = Math.max(1, (screenH - margin - below) / itemHeight);
        int fitAbove = Math.max(1, (getY() - margin) / itemHeight);
        boolean down;
        if (desired <= fitBelow) down = true;
        else if (desired <= fitAbove) down = false;
        else down = fitBelow >= fitAbove;
        int visible = Math.min(desired, down ? fitBelow : fitAbove);
        int h = visible * itemHeight;
        int y = down ? below : getY() - h;
        return new int[]{x, y, w, h, visible};
    }

    // ===== popup + input (driven by the host so it floats above the list) =====

    void renderPopup(GuiGraphics graphics, int mouseX, int mouseY) {
        if (!open) return;
        int[] r = popupRect();
        int x = r[0], y = r[1], w = r[2], h = r[3];
        Font font = font();

        // float the whole popup above the list rows and their item icons (which render at z 150)
        graphics.pose().pushPose();
        graphics.pose().translate(0, 0, 200);

        graphics.fill(x, y, x + w, y + h, 0xFF101010); // fully opaque so text behind never bleeds through

        if (filtered.isEmpty()) {
            graphics.drawString(font, Component.translatable("gui.moonlight.config.no_matches"),
                    x + 4, y + (itemHeight - font.lineHeight) / 2 + 1, DESCRIPTION_COLOR);
        }

        int visible = r[4];
        boolean hasScrollbar = filtered.size() > visible;
        int textRight = x + w - (hasScrollbar ? 6 : 4);
        for (int i = 0; i < visible; i++) {
            int idx = scrollOffset + i;
            if (idx >= filtered.size()) break;
            String opt = filtered.get(idx);
            int iy = y + i * itemHeight;
            boolean hover = mouseX >= x && mouseX < x + w && mouseY >= iy && mouseY < iy + itemHeight;
            if (hover) graphics.fill(x + 1, iy, x + w - 1, iy + itemHeight, 0x40FFFFFF);
            int textX = x + 4;
            if (icon != null) {
                graphics.renderFakeItem(iconFor(opt), x + 2, iy + (itemHeight - 16) / 2);
                textX = x + 2 + 18;
            }
            int color = opt.equals(value) ? SELECTED_COLOR : TEXT_COLOR;
            // scroll the hovered entry when it overflows; clip the rest so nothing bleeds under the scrollbar
            if (hover) {
                GuiHelper.renderScrollingText(graphics, font, Component.literal(opt), textX, textRight, iy, itemHeight, color);
            } else {
                graphics.enableScissor(textX, iy, textRight, iy + itemHeight);
                graphics.drawString(font, opt, textX, iy + (itemHeight - font.lineHeight) / 2 + 1, color);
                graphics.disableScissor();
            }
        }

        if (hasScrollbar) {
            int trackX = x + w - 4;
            int thumbH = Math.max(8, h * visible / filtered.size());
            int thumbY = y + (h - thumbH) * scrollOffset / maxScroll();
            graphics.fill(trackX, y, trackX + 2, y + h, 0xFF000000);
            graphics.fill(trackX, thumbY, trackX + 2, thumbY + thumbH, 0xFFB0B0B0);
        }

        graphics.renderOutline(x, y, w, h, 0xFFFFFFFF); // drawn last so the border is unbroken
        graphics.pose().popPose();
    }

    /** Called by the host on any click while open. Returns true (always consumes). */
    boolean popupMouseClicked(double mouseX, double mouseY, int button) {
        if (!open) return false;
        // clicking in the value/search area just moves the caret, keep the popup open
        if (mouseX >= getX() && mouseX < getX() + valueAreaWidth() && mouseY >= getY() && mouseY < getY() + getHeight()) {
            this.searchBox.mouseClicked(mouseX, mouseY, button);
            return true;
        }
        int[] r = popupRect();
        int x = r[0], y = r[1], w = r[2], h = r[3];
        if (mouseX >= x && mouseX < x + w && mouseY >= y && mouseY < y + h) {
            int idx = scrollOffset + (int) ((mouseY - y) / itemHeight);
            if (idx >= 0 && idx < filtered.size()) select(filtered.get(idx));
        }
        close();
        return true;
    }

    boolean popupMouseScrolled(double mouseX, double mouseY, double delta) {
        if (!open || filtered.size() <= visibleCount()) return false;
        int[] r = popupRect();
        if (mouseX < r[0] || mouseX >= r[0] + r[2] || mouseY < r[1] || mouseY >= r[1] + r[3]) return false;
        this.scrollOffset = Mth.clamp(scrollOffset - (int) Math.signum(delta), 0, maxScroll());
        return true;
    }

    boolean popupKeyPressed(int key, int scan, int mods) {
        if (!open) return false;
        if (key == 256) { // escape
            close();
            return true;
        }
        if (key == 257 || key == 335) { // enter
            if (!filtered.isEmpty()) select(filtered.contains(value) ? value : filtered.get(0));
            close();
            return true;
        }
        return this.searchBox.keyPressed(key, scan, mods);
    }

    boolean popupCharTyped(char c, int mods) {
        return open && this.searchBox.charTyped(c, mods);
    }

    private void select(String v) {
        this.value = v;
        onChange.accept(v);
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
    }
}

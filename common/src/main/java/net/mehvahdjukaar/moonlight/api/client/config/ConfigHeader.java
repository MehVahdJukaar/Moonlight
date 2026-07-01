package net.mehvahdjukaar.moonlight.api.client.config;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Consumer;

import static net.mehvahdjukaar.moonlight.api.client.config.ConfigScreenLayout.*;

/**
 * The top bar of the config screen: the config title, a clickable breadcrumb navigation trail, and a search
 * box. Purely presentational plus breadcrumb hit-testing; it owns the search {@link EditBox} but the screen is
 * responsible for registering it for input and reading its value.
 */
class ConfigHeader {

    /**
     * One breadcrumb segment. {@code target} is the screen to jump to when clicked; {@code current} marks the
     * segment for the page we're already on (not clickable).
     */
    record Crumb(Component label, Screen target, boolean current) {
    }

    private static final int SEARCH_WIDTH = 110;
    private static final int SEARCH_HEIGHT = 14;
    private static final int SIDE_MARGIN = 14;
    private static final int CRUMB_Y = 25;

    private final Component title;
    private final List<Crumb> crumbs;
    private final EditBox search;

    // per-crumb text bounds, recomputed each render for hit-testing
    private final int[] crumbX0;
    private final int[] crumbX1;

    ConfigHeader(Font font, int width, Component title, List<Crumb> crumbs, String initialQuery, Consumer<String> onSearch) {
        this.title = title;
        this.crumbs = crumbs;
        this.crumbX0 = new int[crumbs.size()];
        this.crumbX1 = new int[crumbs.size()];

        this.search = new EditBox(font, width - SIDE_MARGIN - SEARCH_WIDTH, CRUMB_Y - 3, SEARCH_WIDTH, SEARCH_HEIGHT,
                Component.translatable("gui.moonlight.config.search"));
        this.search.setHint(Component.translatable("gui.moonlight.config.search"));
        this.search.setValue(initialQuery);
        this.search.setResponder(onSearch);
    }

    EditBox searchBox() {
        return search;
    }

    void render(GuiGraphics graphics, int width, int mouseX, int mouseY, Font font) {
        graphics.fill(0, 0, width, HEADER, HEADER_BG);
        graphics.fill(0, HEADER - 1, width, HEADER, HEADER_SEPARATOR);

        graphics.drawCenteredString(font, title, width / 2, 7, TITLE_COLOR);

        // breadcrumb trail
        int x = SIDE_MARGIN;
        for (int i = 0; i < crumbs.size(); i++) {
            Crumb c = crumbs.get(i);
            if (i > 0) {
                String sep = " › "; // ›
                graphics.drawString(font, sep, x, CRUMB_Y, CRUMB_SEPARATOR_COLOR);
                x += font.width(sep);
            }
            int w = font.width(c.label());
            crumbX0[i] = x;
            crumbX1[i] = x + w;
            boolean hover = !c.current() && inside(mouseX, mouseY, x, w);
            int color = c.current() ? CRUMB_CURRENT_COLOR : (hover ? CRUMB_HOVER_COLOR : CRUMB_COLOR);
            graphics.drawString(font, c.label(), x, CRUMB_Y, color);
            x += w;
        }

        // search: magnifier glyph + box
        graphics.drawString(font, "⌕", search.getX() - 10, CRUMB_Y, SEARCH_ICON_COLOR); // ⌕
        search.render(graphics, mouseX, mouseY, 0);
    }

    private boolean inside(int mouseX, int mouseY, int x, int w) {
        return mouseX >= x && mouseX <= x + w && mouseY >= CRUMB_Y - 2 && mouseY <= CRUMB_Y + 9;
    }

    /**
     * If a clickable breadcrumb segment is under the cursor, returns its target screen; otherwise null.
     */
    @Nullable
    Screen breadcrumbTarget(double mouseX, double mouseY) {
        for (int i = 0; i < crumbs.size(); i++) {
            Crumb c = crumbs.get(i);
            if (c.current()) continue;
            if (mouseX >= crumbX0[i] && mouseX <= crumbX1[i] && mouseY >= CRUMB_Y - 2 && mouseY <= CRUMB_Y + 9) {
                return c.target();
            }
        }
        return null;
    }
}

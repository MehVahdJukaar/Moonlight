package net.mehvahdjukaar.moonlight.api.client.config;

import net.mehvahdjukaar.moonlight.api.client.gui.ConfigGuiColors;
import net.mehvahdjukaar.moonlight.api.client.gui.GuiHelper;
import net.mehvahdjukaar.moonlight.core.Moonlight;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

/**
 * Shared sizing/color constants for the config screen widgets. Kept in one place so the screen, list, rows and
 * control providers all agree on the grid.
 */
final class ConfigScreenLayout {

    private ConfigScreenLayout() {
    }

    /**
     * Left aligned text that scrolls back and forth when it doesn't fit its box, mirroring vanilla's
     * {@code AbstractWidget.renderScrollingString} (which isn't accessible from here). Shared by option-row
     * labels and the dropdown's current-value box so overflowing text reads the same everywhere.
     */
    static void renderScrollingText(GuiGraphics graphics, Font font, Component text, int minX, int maxX, int rowTop, int rowHeight, int color) {
        GuiHelper.renderScrollingText(graphics, font, text, minX, maxX, rowTop, rowHeight, color);
    }

    /** Left-aligned single-line text hard-clipped (scissored) to {@code [minX, maxX]} — used for row subtitles. */
    static void drawClipped(GuiGraphics graphics, Font font, Component text, int minX, int y, int maxX, int color) {
        graphics.enableScissor(minX, y - 1, maxX, y + font.lineHeight + 1);
        graphics.drawString(font, text, minX, y, color);
        graphics.disableScissor();
    }

    /** First line only of a (possibly multi-line) component, as plain text — for a one-line subtitle. */
    static Component firstLine(Component text) {
        String s = text.getString();
        int nl = s.indexOf('\n');
        return Component.literal(nl >= 0 ? s.substring(0, nl) : s);
    }

    // gui sprites (assets/moonlight/textures/gui/sprites/{yes,no,save}.png)
    static final ResourceLocation ON_ICON = Moonlight.res("yes");
    static final ResourceLocation OFF_ICON = Moonlight.res("no");
    static final ResourceLocation SAVE_ICON = Moonlight.res("save");
    static final ResourceLocation CONFIG_ICON = Moonlight.res("config");
    static final ResourceLocation FOLDER_ICON = Moonlight.res("folder");
    static final ResourceLocation SEARCH_ICON = Moonlight.res("search");
    static final ResourceLocation RESET_ICON = Moonlight.res("reset");
    static final ResourceLocation DELETE_ICON = Moonlight.res("delete");
    static final ResourceLocation EDIT_ICON = Moonlight.res("edit");
    static final ResourceLocation WORLD_RELOAD_ICON = Moonlight.res("world_reload");
    static final ResourceLocation GAME_RESTART_ICON = Moonlight.res("game_restart");

    /** The reload/restart hint sprite for a value, or null when it applies immediately. */
    @org.jetbrains.annotations.Nullable
    static ResourceLocation reloadIcon(net.mehvahdjukaar.moonlight.api.platform.configs.options.ConfigReloadType type) {
        return switch (type) {
            case WORLD_RELOAD -> WORLD_RELOAD_ICON;
            case GAME_RESTART -> GAME_RESTART_ICON;
            case NONE -> null;
        };
    }

    static final int HEADER = 44;
    static final int FOOTER = 36;
    static final int ITEM_HEIGHT = 24; // compact single-line rows on the main config screen (button ~ on/off height)
    static final int SELECT_ITEM_HEIGHT = 30; // taller two-line rows (title + subtitle) on the config-list screen
    static final int ROW_WIDTH = 280;
    static final int ROW_ICON = 16; // leading category/config icon

    static final int CONTROL_WIDTH = 88; // ~80% of the former 110px, giving labels more room
    static final int CONTROL_HEIGHT = 20;
    static final int ARROW_WIDTH = 12;
    static final int RESET_WIDTH = CONTROL_HEIGHT;
    static final int GAP = 4;

    static final int DESC_LINES_PER_ROW = 2;

    // ── palette: all colors live in one place, {@link GuiColors}; these are just the names the config code uses ──
    static final int LABEL_COLOR = ConfigGuiColors.LABEL;
    static final int TEXT_COLOR = ConfigGuiColors.TEXT;
    static final int ERROR_COLOR = ConfigGuiColors.ERROR;
    static final int DESCRIPTION_COLOR = ConfigGuiColors.DESCRIPTION;

    static final int HEADER_BG = ConfigGuiColors.HEADER_BG;
    static final int HEADER_SEPARATOR = ConfigGuiColors.HEADER_SEPARATOR;
    static final int TITLE_COLOR = ConfigGuiColors.TITLE;
    static final int CRUMB_COLOR = ConfigGuiColors.CRUMB;
    static final int CRUMB_HOVER_COLOR = ConfigGuiColors.CRUMB_HOVER;
    static final int CRUMB_CURRENT_COLOR = ConfigGuiColors.CRUMB_CURRENT;
    static final int CRUMB_SEPARATOR_COLOR = ConfigGuiColors.CRUMB_SEPARATOR;

    static final int CATEGORY_COLOR = ConfigGuiColors.CATEGORY;
    static final int MODIFIED_COLOR = ConfigGuiColors.MODIFIED;
}

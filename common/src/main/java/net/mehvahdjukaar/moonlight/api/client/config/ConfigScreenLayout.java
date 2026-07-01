package net.mehvahdjukaar.moonlight.api.client.config;

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

    // gui sprites (assets/moonlight/textures/gui/sprites/{yes,no,save}.png)
    static final ResourceLocation ON_ICON = Moonlight.res("yes");
    static final ResourceLocation OFF_ICON = Moonlight.res("no");
    static final ResourceLocation SAVE_ICON = Moonlight.res("save");
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
    static final int ITEM_HEIGHT = 22;
    static final int ROW_WIDTH = 280;

    static final int CONTROL_WIDTH = 110;
    static final int CONTROL_HEIGHT = 20;
    static final int ARROW_WIDTH = 12;
    static final int RESET_WIDTH = CONTROL_HEIGHT;
    static final int GAP = 4;

    static final int DESC_LINES_PER_ROW = 2;

    static final int LABEL_COLOR = 0xFFFFFF;
    static final int TEXT_COLOR = 0xE0E0E0;
    static final int ERROR_COLOR = 0xFF5555;
    static final int DESCRIPTION_COLOR = 0xA0A0A0;

    // header / navigation
    static final int HEADER_BG = 0x90000000;
    static final int HEADER_SEPARATOR = 0xFF101012;
    static final int TITLE_COLOR = 0xFFE0A0;          // soft gold
    static final int CRUMB_COLOR = 0x9A9A9A;          // ancestor crumb
    static final int CRUMB_HOVER_COLOR = 0xFFFFFF;
    static final int CRUMB_CURRENT_COLOR = 0xFFE0A0;
    static final int CRUMB_SEPARATOR_COLOR = 0x6A6A6A;
    static final int SEARCH_ICON_COLOR = 0x9A9A9A;

    // value/row accents
    static final int CATEGORY_COLOR = 0x9AD8FF;       // light blue category labels
    static final int MODIFIED_COLOR = 0xFFD96B;       // amber: unsaved edit
}

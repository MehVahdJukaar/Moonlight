package net.mehvahdjukaar.moonlight.api.client.config;

import net.mehvahdjukaar.moonlight.api.client.gui.ConfigGuiColors;
import net.mehvahdjukaar.moonlight.api.client.gui.GuiHelper;
import net.mehvahdjukaar.moonlight.api.platform.configs.ConfigType;
import net.mehvahdjukaar.moonlight.api.platform.configs.options.ConfigReloadType;
import net.mehvahdjukaar.moonlight.core.Moonlight;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

/**
 * Shared sizing/color constants for the config screen widgets. Kept in one place so the screen, list, rows and
 * control providers all agree on the grid.
 */
final class ConfigScreenLayout {

    /** Left-aligned single-line text hard-clipped (scissored) to {@code [minX, maxX]} — used for row subtitles. */
    static void drawClipped(GuiGraphics graphics, Font font, Component text, int minX, int y, int maxX, int color) {
        graphics.enableScissor(minX, y - 1, maxX, y + font.lineHeight + 1);
        graphics.drawString(font, text, minX, y, color);
        graphics.disableScissor();
    }

    // gui sprites (assets/moonlight/textures/gui/sprites/{yes,no,save}.png)
    static final ResourceLocation ON_ICON = Moonlight.res("yes");
    static final ResourceLocation OFF_ICON = Moonlight.res("no");
    static final ResourceLocation SAVE_ICON = Moonlight.res("save");
    static final ResourceLocation CONFIG_ICON = Moonlight.res("config");
    static final ResourceLocation CLIENT_CONFIG_ICON = Moonlight.res("config_client");
    static final ResourceLocation SERVER_CONFIG_ICON = Moonlight.res("config_server");
    static final ResourceLocation COMMON_CONFIG_ICON = Moonlight.res("config_common");
    static final ResourceLocation FOLDER_ICON = Moonlight.res("folder");
    static final ResourceLocation SEARCH_ICON = Moonlight.res("search");
    static final ResourceLocation RESET_ICON = Moonlight.res("reset");
    static final ResourceLocation DELETE_ICON = Moonlight.res("delete");
    static final ResourceLocation EDIT_ICON = Moonlight.res("edit");
    static final ResourceLocation WORLD_RELOAD_ICON = Moonlight.res("world_reload");
    static final ResourceLocation GAME_RESTART_ICON = Moonlight.res("game_restart");

    /** The reload/restart hint sprite for a value, or null when it applies immediately. */
    @Nullable
    static ResourceLocation reloadIcon(ConfigReloadType type) {
        return switch (type) {
            case WORLD_RELOAD -> WORLD_RELOAD_ICON;
            case GAME_RESTART -> GAME_RESTART_ICON;
            case NONE -> null;
        };
    }

    /** The paper "config file" sprite for a config's type, distinguishing client / server-synced / common. */
    static ResourceLocation configFileIcon(ConfigType type) {
        return switch (type) {
            case CLIENT -> CLIENT_CONFIG_ICON;
            case COMMON_SYNCED -> SERVER_CONFIG_ICON;
            case COMMON -> COMMON_CONFIG_ICON;
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

    // structural header chrome — a neutral translucent-black bar, not part of the themeable palette
    static final int HEADER_BG = 0x90000000;
    static final int HEADER_SEPARATOR = 0xFF101012;
    static final int TITLE_COLOR = ConfigGuiColors.TITLE;
    static final int CRUMB_COLOR = ConfigGuiColors.CRUMB;
    static final int CRUMB_HOVER_COLOR = ConfigGuiColors.CRUMB_HOVER;
    static final int CRUMB_CURRENT_COLOR = ConfigGuiColors.CRUMB_CURRENT;
    static final int CRUMB_SEPARATOR_COLOR = ConfigGuiColors.CRUMB_SEPARATOR;

    static final int CATEGORY_COLOR = ConfigGuiColors.CATEGORY;
    static final int MODIFIED_COLOR = ConfigGuiColors.MODIFIED;
}

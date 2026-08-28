package net.mehvahdjukaar.moonlight.core.client.config;

import net.mehvahdjukaar.moonlight.api.client.gui.MoonlightIcons;
import net.mehvahdjukaar.moonlight.api.platform.configs.ConfigType;
import net.mehvahdjukaar.moonlight.api.platform.configs.options.ConfigReloadType;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.Nullable;

// Shared sizing constants and drawing helpers for the config screen widgets, in one place so the screen,
// list, rows and control providers all agree on the grid.
final class ConfigScreenLayout {

    static final int HEADER = 44;
    static final int FOOTER = 36;
    static final int ITEM_HEIGHT = 24; // compact single-line rows on the main config screen (button ~ on/off height)
    static final int SELECT_ITEM_HEIGHT = 30; // taller two-line rows (title + subtitle) on the config-list screen
    static final int ROW_WIDTH = 280;
    static final int ROW_ICON = 16; // leading category/config icon

    static final int CONTROL_WIDTH = 96; // kept narrow so row labels get more room
    static final int CONTROL_HEIGHT = 20;
    static final int ARROW_WIDTH = 12;
    static final int RESET_WIDTH = CONTROL_HEIGHT;
    static final int GAP = 4;

    static final int DESC_LINES_PER_ROW = 2;

    // the reload/restart hint sprite for a value, or null when it applies immediately
    @Nullable
    static Identifier reloadIcon(ConfigReloadType type) {
        return switch (type) {
            case WORLD_RELOAD -> MoonlightIcons.WORLD_RELOAD;
            case GAME_RESTART -> MoonlightIcons.GAME_RESTART;
            case NONE -> null;
        };
    }

    // the paper "config file" sprite for a config's type: client, server-synced or common
    static Identifier configFileIcon(ConfigType type) {
        return switch (type) {
            case CLIENT -> MoonlightIcons.CONFIG_CLIENT;
            case COMMON_SYNCED -> MoonlightIcons.CONFIG_SERVER;
            case COMMON -> MoonlightIcons.CONFIG_COMMON;
        };
    }

    // left-aligned single-line text hard-clipped to [minX, maxX], for row subtitles
    static void drawClipped(GuiGraphicsExtractor graphics, Font font, Component text, int minX, int y, int maxX, int color) {
        graphics.enableScissor(minX, y - 1, maxX, y + font.lineHeight + 1);
        graphics.text(font, text, minX, y, color);
        graphics.disableScissor();
    }
}

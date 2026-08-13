package net.mehvahdjukaar.moonlight.core.client.config;

import net.mehvahdjukaar.moonlight.api.platform.configs.ConfigType;
import net.mehvahdjukaar.moonlight.api.platform.configs.options.ConfigReloadType;
import net.mehvahdjukaar.moonlight.core.Moonlight;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

// Shared sizing constants and drawing helpers for the config screen widgets, in one place so the screen, list, rows
// and control providers all agree on the grid.
final class ConfigScreenLayout {

    // left-aligned single-line text hard-clipped to [minX, maxX], for row subtitles
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
    // 7x7 disclosure triangles for expandable option descriptions
    static final ResourceLocation SECTION_COLLAPSED_ICON = Moonlight.res("widget/section_collapsed");
    static final ResourceLocation SECTION_EXPANDED_ICON = Moonlight.res("widget/section_expanded");

    // the reload/restart hint sprite for a value, or null when it applies immediately
    @Nullable
    static ResourceLocation reloadIcon(ConfigReloadType type) {
        return switch (type) {
            case WORLD_RELOAD -> WORLD_RELOAD_ICON;
            case GAME_RESTART -> GAME_RESTART_ICON;
            case NONE -> null;
        };
    }

    // the paper "config file" sprite for a config's type: client, server-synced or common
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

    static final int CONTROL_WIDTH = 96; // kept narrow so row labels get more room
    static final int CONTROL_HEIGHT = 20;
    static final int ARROW_WIDTH = 12;
    static final int RESET_WIDTH = CONTROL_HEIGHT;
    static final int GAP = 4;

    static final int DESC_LINES_PER_ROW = 2;
}

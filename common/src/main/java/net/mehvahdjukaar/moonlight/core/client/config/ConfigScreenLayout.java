package net.mehvahdjukaar.moonlight.core.client.config;

import net.mehvahdjukaar.moonlight.api.client.gui.MoonlightIcons;
import net.mehvahdjukaar.moonlight.api.platform.configs.ConfigType;
import net.mehvahdjukaar.moonlight.api.platform.configs.options.ConfigReloadType;
import net.minecraft.client.gui.components.Button;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.Nullable;

//Shared sizing constants
public final class ConfigScreenLayout {

    public static final int HEADER = 44;
    public static final int FOOTER = 36;
    public static final int GAP = 4;
    public static final int LINE_HEIGHT = 9; // Font.lineHeight, for layout math that has no font at hand
    public static final int CONTROL_HEIGHT = Button.DEFAULT_HEIGHT;
    public static final int CONTROL_WIDTH = 96; // kept narrow so row labels get more room
    public static final int ARROW_WIDTH = 12;
    public static final int RESET_WIDTH = CONTROL_HEIGHT;

    public static final int ITEM_HEIGHT = 24; // compact single-line rows on the main config screen (button ~ on/off height)
    public static final int SELECT_ITEM_HEIGHT = 30; // taller two-line rows (title + subtitle) on the config-list screen
    public static final int ROW_WIDTH = 280;
    public static final int ROW_ICON = 16; // leading category/config icon
    public static final int DESC_LINES_PER_ROW = 2;

    // the mod tile grids (mods hub, discover mods)
    public static final int GRID_SIDE_MARGIN = 24;
    public static final int GRID_PAD = 8;
    public static final int MOD_ICON_SIZE = 32; // icon slot height; square icons render at this, wider ones expand

    @Nullable
    static Identifier reloadIcon(ConfigReloadType type) {
        return switch (type) {
            case WORLD_RELOAD -> MoonlightIcons.WORLD_RELOAD;
            case GAME_RESTART -> MoonlightIcons.GAME_RESTART;
            case NONE -> null;
        };
    }

    static Identifier configFileIcon(ConfigType type) {
        return switch (type) {
            case CLIENT -> MoonlightIcons.CONFIG_CLIENT;
            case COMMON_SYNCED -> MoonlightIcons.CONFIG_SERVER;
            case COMMON -> MoonlightIcons.CONFIG_COMMON;
        };
    }
}

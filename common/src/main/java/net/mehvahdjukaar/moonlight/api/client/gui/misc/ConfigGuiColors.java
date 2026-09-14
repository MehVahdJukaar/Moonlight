package net.mehvahdjukaar.moonlight.api.client.gui.misc;

import net.minecraft.ChatFormatting;
import net.minecraft.util.CommonColors;

import java.util.Map;
import java.util.Objects;

public final class ConfigGuiColors {

    public static int chat(ChatFormatting color) {
        return 0xFF000000 | Objects.requireNonNull(color.getColor());
    }

    // structural chrome, deliberately not themeable
    public static final int HEADER_SEPARATOR = 0xFF101012; // our own inner bands; header/footer use the vanilla sprites
    public static final int HOVER_HIGHLIGHT = 0x40FFFFFF; // translucent wash over a hovered popup row/cell
    public static final int POPUP_BG = 0xFF101010; // opaque so the rows behind never bleed through
    public static final int SCROLLBAR_THUMB = 0xFFB0B0B0;
    public static final int CHECKER_LIGHT = 0xFFBBBBBB; // behind translucent colors so alpha reads
    public static final int CHECKER_DARK = 0xFF6E6E6E;

    // core text
    public static final int LABEL = chat(ChatFormatting.WHITE);
    public static final int TEXT = chat(ChatFormatting.WHITE);
    public static final int TEXT_SECONDARY = chat(ChatFormatting.GRAY);
    public static final int DESCRIPTION = chat(ChatFormatting.GRAY);
    public static final int DISABLED = chat(ChatFormatting.DARK_GRAY);
    public static final int ERROR = chat(ChatFormatting.RED);
    public static final int TITLE = chat(ChatFormatting.GOLD);
    public static final int MODIFIED = chat(ChatFormatting.YELLOW);
    public static final int CATEGORY = chat(ChatFormatting.GREEN);
    public static final int SELECTED = chat(ChatFormatting.LIGHT_PURPLE);

    // cards of the mod grids (mods hub, discover mods)
    public static final int TILE_BG = 0xFF1B1B20;
    public static final int TILE_BG_HOVER = 0xFF2C2C34;
    public static final int TILE_OUTLINE = CommonColors.BLACK;
    public static final int TILE_OUTLINE_HOVER = CATEGORY;
    // mods that aren't ours: their config is either the loader's own screen or one we converted on the fly
    public static final int TILE_OUTLINE_HOVER_FOREIGN = chat(ChatFormatting.AQUA);
    public static final int TILE_ICON_BG = 0xFF303038; // backdrop of the letter tile standing in for a missing icon

    // the bright chat colors, picked from the mod id so a mod without a logo always gets the same one
    private static final ChatFormatting[] INITIAL_COLORS = {
            ChatFormatting.RED, ChatFormatting.GOLD, ChatFormatting.YELLOW, ChatFormatting.GREEN,
            ChatFormatting.AQUA, ChatFormatting.BLUE, ChatFormatting.LIGHT_PURPLE, ChatFormatting.WHITE
    };

    // mods with no icon but a color everyone knows them by. hacky
    private static final Map<String, Integer> BRAND_COLORS = Map.of(
            "quark", 0xFF48DDBC,
            "zeta", 0xFF48DDBC);

    public static int initialLetter(String modId) {
        Integer brand = BRAND_COLORS.get(modId);
        if (brand != null) return brand;
        return chat(INITIAL_COLORS[Math.floorMod(modId.hashCode(), INITIAL_COLORS.length)]);
    }

    // breadcrumb
    public static final int CRUMB = chat(ChatFormatting.GRAY);
    public static final int CRUMB_HOVER = chat(ChatFormatting.WHITE);
    public static final int CRUMB_CURRENT = chat(ChatFormatting.YELLOW);
    public static final int CRUMB_SEPARATOR = chat(ChatFormatting.DARK_GRAY);

    // syntax highlighting (JSON / SNBT editors)
    public static final int SYNTAX_DEFAULT = chat(ChatFormatting.WHITE);
    public static final int SYNTAX_KEY = chat(ChatFormatting.AQUA);
    public static final int SYNTAX_STRING = chat(ChatFormatting.GREEN);
    public static final int SYNTAX_NUMBER = chat(ChatFormatting.GOLD);
    public static final int SYNTAX_KEYWORD = chat(ChatFormatting.LIGHT_PURPLE);
    public static final int SYNTAX_TYPE = chat(ChatFormatting.YELLOW);
    public static final int SYNTAX_PUNCTUATION = chat(ChatFormatting.GRAY);

    // regex-specific token roles
    public static final int SYNTAX_ESCAPE = chat(ChatFormatting.GOLD);
    public static final int SYNTAX_CHAR_CLASS = chat(ChatFormatting.GREEN);
    public static final int SYNTAX_GROUP = chat(ChatFormatting.AQUA);
    public static final int SYNTAX_QUANTIFIER = chat(ChatFormatting.LIGHT_PURPLE);
    public static final int SYNTAX_ANCHOR = chat(ChatFormatting.YELLOW);
}

package net.mehvahdjukaar.moonlight.api.client.gui.misc;

import net.minecraft.ChatFormatting;

import java.util.Objects;

public final class ConfigGuiColors {

    public static int chat(ChatFormatting color) {
        return Objects.requireNonNull(color.getColor());
    }

    // ── structural chrome (the fixed dark UI frame — deliberately NOT themeable, kept here only so it isn't duplicated) ──
    public static final int HEADER_SEPARATOR = 0xFF101012; // 1px divider for our own inner bands (header/footer use the vanilla separator sprites)

    // ── core text ──
    public static final int LABEL = chat(ChatFormatting.WHITE);        // primary label — MC white
    public static final int TEXT = chat(ChatFormatting.WHITE);         // editable field text
    public static final int TEXT_SECONDARY = chat(ChatFormatting.GRAY);         // editable field text
    public static final int DESCRIPTION = 0xA8B0C0;                    // subtitles / secondary text — custom muted blue-gray (softer than MC gray)
    public static final int ERROR = chat(ChatFormatting.RED);          // invalid value — MC red
    public static final int TITLE = chat(ChatFormatting.GOLD);         // screen title — MC gold
    public static final int MODIFIED = chat(ChatFormatting.YELLOW);    // unsaved edit — MC yellow, pops against the gold title
    public static final int CATEGORY = chat(ChatFormatting.GREEN);      // category labels / accents — MC aqua
    public static final int SELECTED = chat(ChatFormatting.LIGHT_PURPLE); // highlighted / selected entry — MC light purple, a distinct accent hue

    // ── cards / rows of the mod grids (mods hub, discover mods) ──
    public static final int TILE_BG = 0xFF1B1B20;
    public static final int TILE_BG_HOVER = 0xFF2C2C34;
    public static final int TILE_OUTLINE = 0xFF000000;
    public static final int TILE_OUTLINE_HOVER = 0xFF000000 | CATEGORY; // aqua accent (opaque)
    public static final int TILE_ICON_BG = 0xFF303038;                  // backdrop of the letter tile standing in for a missing icon

    // ── breadcrumb / navigation ──
    public static final int CRUMB = 0x8B93A5;                          // custom muted blue-gray
    public static final int CRUMB_HOVER = chat(ChatFormatting.WHITE);
    public static final int CRUMB_CURRENT = chat(ChatFormatting.YELLOW); // MC yellow — the page you're on
    public static final int CRUMB_SEPARATOR = 0x565F70;                // custom faint separator

    // ── syntax highlighting (JSON / SNBT editors) ──
    public static final int SYNTAX_DEFAULT = chat(ChatFormatting.WHITE);
    public static final int SYNTAX_KEY = chat(ChatFormatting.AQUA);         // aqua
    public static final int SYNTAX_STRING = chat(ChatFormatting.GREEN);     // green
    public static final int SYNTAX_NUMBER = chat(ChatFormatting.GOLD);      // gold
    public static final int SYNTAX_KEYWORD = chat(ChatFormatting.LIGHT_PURPLE); // light purple
    public static final int SYNTAX_TYPE = chat(ChatFormatting.YELLOW);      // yellow
    public static final int SYNTAX_PUNCTUATION = chat(ChatFormatting.GRAY); // gray

    // ── regex-specific token roles ──
    public static final int SYNTAX_ESCAPE = chat(ChatFormatting.GOLD);          // gold
    public static final int SYNTAX_CHAR_CLASS = chat(ChatFormatting.GREEN);     // green
    public static final int SYNTAX_GROUP = chat(ChatFormatting.AQUA);           // aqua
    public static final int SYNTAX_QUANTIFIER = chat(ChatFormatting.LIGHT_PURPLE); // light purple
    public static final int SYNTAX_ANCHOR = chat(ChatFormatting.YELLOW);        // yellow
}

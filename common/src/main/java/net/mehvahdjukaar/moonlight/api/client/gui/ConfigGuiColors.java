package net.mehvahdjukaar.moonlight.api.client.gui;

import net.minecraft.ChatFormatting;

import java.util.Objects;

/**
 * The single source of truth for every text/accent color used by Moonlight's native config UI and its shared GUI
 * widgets (config screen, dropdowns, color picker, JSON/SNBT/regex editors, mods hub). Values are pulled from
 * Minecraft's 16 built-in chat colors ({@link ChatFormatting}); change them here to retheme the whole UI in one
 * place. Structural panel fills (dark translucent backgrounds) live here too, but aren't chat colors.
 */
public final class ConfigGuiColors {

    /** RGB of one of Minecraft's 16 built-in chat colors. */
    public static int chat(ChatFormatting color) {
        return Objects.requireNonNull(color.getColor());
    }

    // ── core text ──
    public static final int LABEL = chat(ChatFormatting.WHITE);        // primary label
    public static final int TEXT = chat(ChatFormatting.WHITE);         // editable field text
    public static final int DESCRIPTION = chat(ChatFormatting.GRAY);   // subtitles / secondary text
    public static final int ERROR = chat(ChatFormatting.RED);          // invalid value
    public static final int TITLE = chat(ChatFormatting.GOLD);         // screen title
    public static final int MODIFIED = chat(ChatFormatting.YELLOW);    // unsaved edit
    public static final int CATEGORY = chat(ChatFormatting.AQUA);      // category labels / accents
    public static final int SELECTED = chat(ChatFormatting.AQUA);      // highlighted / selected entry

    // ── breadcrumb / navigation ──
    public static final int CRUMB = chat(ChatFormatting.GRAY);
    public static final int CRUMB_HOVER = chat(ChatFormatting.WHITE);
    public static final int CRUMB_CURRENT = chat(ChatFormatting.GOLD);
    public static final int CRUMB_SEPARATOR = chat(ChatFormatting.DARK_GRAY);

    // ── structural panel fills (not chat colors) ──
    public static final int HEADER_BG = 0x90000000;
    public static final int HEADER_SEPARATOR = 0xFF101012;
    public static final int SELECTION_BG = 0xFF000000 | chat(ChatFormatting.DARK_BLUE); // text-selection highlight

    // ── syntax highlighting (JSON / SNBT editors) ──
    public static final int SYNTAX_DEFAULT = chat(ChatFormatting.WHITE);
    public static final int SYNTAX_KEY = chat(ChatFormatting.AQUA);
    public static final int SYNTAX_STRING = chat(ChatFormatting.GREEN);
    public static final int SYNTAX_NUMBER = chat(ChatFormatting.GOLD);
    public static final int SYNTAX_KEYWORD = chat(ChatFormatting.LIGHT_PURPLE);
    public static final int SYNTAX_TYPE = chat(ChatFormatting.RED);
    public static final int SYNTAX_PUNCTUATION = chat(ChatFormatting.GRAY);

    // ── regex-specific token roles ──
    public static final int SYNTAX_ESCAPE = chat(ChatFormatting.GOLD);
    public static final int SYNTAX_CHAR_CLASS = chat(ChatFormatting.GREEN);
    public static final int SYNTAX_GROUP = chat(ChatFormatting.AQUA);
    public static final int SYNTAX_QUANTIFIER = chat(ChatFormatting.LIGHT_PURPLE);
    public static final int SYNTAX_ANCHOR = chat(ChatFormatting.YELLOW);
}

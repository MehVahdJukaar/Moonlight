package net.mehvahdjukaar.moonlight.api.client.gui;

import net.minecraft.ChatFormatting;

import java.util.Objects;

/**
 * The single source of truth for every text/accent color used by Moonlight's native config UI and its shared GUI
 * widgets (config screen, dropdowns, color picker, JSON/SNBT/regex editors, mods hub). Change them here to retheme
 * the whole UI in one place.
 * <p>
 * The palette leans on Minecraft's own vibrant chat colors (via the {@link #chat} helper) so the UI feels native and
 * lively, with a handful of hand-tuned customs where the vanilla grays would look flat — muted secondary text,
 * breadcrumbs and separators. Each accent role gets its own distinct hue so they read as one coherent family. The
 * bulk of these are the text / accent / syntax theme; the handful of structural-chrome constants (header fills,
 * separators) are fixed and not meant to be re-themed, but they live here too so they aren't copy-pasted across screens.
 */
public final class ConfigGuiColors {

    /** RGB of one of Minecraft's 16 built-in chat colors. */
    public static int chat(ChatFormatting color) {
        return Objects.requireNonNull(color.getColor());
    }

    // ── structural chrome (the fixed dark UI frame — deliberately NOT themeable, kept here only so it isn't duplicated) ──
    public static final int HEADER_BG = 0x90000000;       // translucent black top/bottom bars
    public static final int HEADER_SEPARATOR = 0xFF101012; // the 1px line under the header / above the footer

    // ── core text ──
    public static final int LABEL = chat(ChatFormatting.WHITE);        // primary label — MC white
    public static final int TEXT = chat(ChatFormatting.WHITE);         // editable field text
    public static final int DESCRIPTION = 0xA8B0C0;                    // subtitles / secondary text — custom muted blue-gray (softer than MC gray)
    public static final int ERROR = chat(ChatFormatting.RED);          // invalid value — MC red
    public static final int TITLE = chat(ChatFormatting.GOLD);         // screen title — MC gold
    public static final int MODIFIED = chat(ChatFormatting.YELLOW);    // unsaved edit — MC yellow, pops against the gold title
    public static final int CATEGORY = chat(ChatFormatting.AQUA);      // category labels / accents — MC aqua
    public static final int SELECTED = chat(ChatFormatting.LIGHT_PURPLE); // highlighted / selected entry — MC light purple, a distinct accent hue

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

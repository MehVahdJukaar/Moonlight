package net.mehvahdjukaar.moonlight.api.client.gui;

import net.minecraft.ChatFormatting;

import java.util.Objects;

/**
 * The single source of truth for every text/accent color used by Moonlight's native config UI and its shared GUI
 * widgets (config screen, dropdowns, color picker, JSON/SNBT/regex editors, mods hub). Change them here to retheme
 * the whole UI in one place.
 * <p>
 * Rather than Minecraft's 16 harsh, fully-saturated chat colors, these are a hand-tuned, vivid palette (inspired by
 * modern dark editor themes) so text is easier on the eyes and accents read as one coherent family. The
 * {@link #chat} helper is still available if you want to fall back to a vanilla color. The bulk of these are the
 * text / accent / syntax theme; the handful of structural-chrome constants (header fills, separators) are fixed and
 * not meant to be re-themed, but they live here too so they aren't copy-pasted across screens.
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
    public static final int LABEL = 0xF8F8F2;        // primary label — bright off-white
    public static final int TEXT = 0xF8F8F2;         // editable field text
    public static final int DESCRIPTION = 0x9AA4C0;  // subtitles / secondary text — cool muted blue-gray
    public static final int ERROR = 0xFF5555;        // invalid value — vivid red
    public static final int TITLE = 0xFFC24B;        // screen title — rich gold
    public static final int MODIFIED = 0xFFB86C;     // unsaved edit — warm orange, pops against the gold title
    public static final int CATEGORY = 0x8BE9FD;     // category labels / accents — vivid cyan
    public static final int SELECTED = 0xBD93F9;     // highlighted / selected entry — purple, a distinct accent hue

    // ── breadcrumb / navigation ──
    public static final int CRUMB = 0x8B93A5;
    public static final int CRUMB_HOVER = 0xF8F8F2;
    public static final int CRUMB_CURRENT = 0xF1FA8C;      // pale yellow — the page you're on
    public static final int CRUMB_SEPARATOR = 0x565F70;

    // ── syntax highlighting (JSON / SNBT editors) ──
    public static final int SYNTAX_DEFAULT = 0xF8F8F2;
    public static final int SYNTAX_KEY = 0x8BE9FD;         // cyan
    public static final int SYNTAX_STRING = 0x50FA7B;      // green
    public static final int SYNTAX_NUMBER = 0xBD93F9;      // purple
    public static final int SYNTAX_KEYWORD = 0xFF79C6;     // pink
    public static final int SYNTAX_TYPE = 0xFFB86C;        // orange
    public static final int SYNTAX_PUNCTUATION = 0xABB2BF; // soft gray

    // ── regex-specific token roles ──
    public static final int SYNTAX_ESCAPE = 0xFFB86C;      // orange
    public static final int SYNTAX_CHAR_CLASS = 0x50FA7B;  // green
    public static final int SYNTAX_GROUP = 0x8BE9FD;       // cyan
    public static final int SYNTAX_QUANTIFIER = 0xFF79C6;  // pink
    public static final int SYNTAX_ANCHOR = 0xF1FA8C;      // yellow
}

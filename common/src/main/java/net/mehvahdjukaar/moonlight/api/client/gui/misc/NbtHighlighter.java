package net.mehvahdjukaar.moonlight.api.client.gui.misc;

import net.mehvahdjukaar.moonlight.api.client.gui.widget.SyntaxEditBox;

import java.util.Arrays;

/**
 * Tiny SNBT (stringified NBT) colorer, one line at a time, same idea as JsonHighlighter. SNBT looks like JSON but
 * with unquoted keys, single or double quoted strings, number suffixes (10b, 1.5f, 20L) and array markers
 * ([B; [I; [L;). Reading each line on its own is enough, this only colors text, it doesn't parse it.
 */
public final class NbtHighlighter implements SyntaxHighlighter {

    public static final NbtHighlighter INSTANCE = new NbtHighlighter();

    private static final int KEY = ConfigGuiColors.SYNTAX_KEY;          // tag name before a ':'
    private static final int STRING = ConfigGuiColors.SYNTAX_STRING;    // "quoted" / 'quoted' / unquoted string values
    private static final int NUMBER = ConfigGuiColors.SYNTAX_NUMBER;    // numeric part of a value
    private static final int TYPE = ConfigGuiColors.SYNTAX_TYPE;        // type suffix (b s l f d) and array markers (B; I; L;)
    private static final int KEYWORD = ConfigGuiColors.SYNTAX_KEYWORD;  // true / false
    private static final int PUNCTUATION = ConfigGuiColors.SYNTAX_PUNCTUATION; // { } [ ] : , ;
    private static final int DEFAULT = ConfigGuiColors.SYNTAX_DEFAULT;

    @Override
    public int[] colors(String line) {
        int n = line.length();
        int[] colors = new int[n];
        int i = 0;
        while (i < n) {
            char c = line.charAt(i);
            if (c == '"' || c == '\'') {
                char quote = c;
                int start = i++;
                while (i < n) {
                    char d = line.charAt(i++);
                    if (d == '\\' && i < n) i++; // skip escaped char
                    else if (d == quote) break;
                }
                Arrays.fill(colors, start, i, isKeyAhead(line, i) ? KEY : STRING);
            } else if (c == '-' || c == '+' ? (i + 1 < n && isDigit(line.charAt(i + 1))) : isDigit(c)) {
                int start = i++;
                while (i < n && isNumberChar(line.charAt(i))) i++;
                Arrays.fill(colors, start, i, NUMBER);
                // optional single-letter type suffix: 10b, 20s, 30L, 1.5f, 2.0d
                if (i < n && isTypeSuffix(line.charAt(i))) {
                    colors[i++] = TYPE;
                }
            } else if (Character.isLetter(c) || c == '_') {
                int start = i++;
                while (i < n && isWordChar(line.charAt(i))) i++;
                String word = line.substring(start, i);
                if (word.equals("true") || word.equals("false")) {
                    Arrays.fill(colors, start, i, KEYWORD);
                } else if (word.length() == 1 && charAheadIs(line, i, ';')) {
                    colors[start] = TYPE; // B / I / L array marker (the ';' is punctuation below)
                } else {
                    Arrays.fill(colors, start, i, isKeyAhead(line, i) ? KEY : STRING);
                }
            } else if (c == '{' || c == '}' || c == '[' || c == ']' || c == ':' || c == ',' || c == ';') {
                colors[i++] = PUNCTUATION;
            } else {
                colors[i++] = DEFAULT;
            }
        }
        return colors;
    }

    /** A token is a key when the next non-space character is a ':'. */
    private static boolean isKeyAhead(String line, int from) {
        return charAheadIs(line, from, ':');
    }

    private static boolean charAheadIs(String line, int from, char target) {
        int j = from;
        while (j < line.length() && line.charAt(j) == ' ') j++;
        return j < line.length() && line.charAt(j) == target;
    }

    private static boolean isDigit(char c) {
        return c >= '0' && c <= '9';
    }

    private static boolean isNumberChar(char c) {
        return isDigit(c) || c == '.' || c == 'e' || c == 'E' || c == '+' || c == '-';
    }

    private static boolean isTypeSuffix(char c) {
        return "bslfdBSLFD".indexOf(c) >= 0;
    }

    private static boolean isWordChar(char c) {
        return Character.isLetterOrDigit(c) || c == '_' || c == '.' || c == '+' || c == '-';
    }
}

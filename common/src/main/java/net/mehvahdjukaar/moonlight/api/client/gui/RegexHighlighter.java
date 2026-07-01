package net.mehvahdjukaar.moonlight.api.client.gui;

import net.minecraft.client.gui.components.EditBox;

import java.util.Arrays;
import java.util.regex.Pattern;

/**
 * Lightweight regex syntax highlighter. Classifies each character of the pattern (escapes, character classes,
 * groups, quantifiers, anchors) into a color; a pattern that doesn't compile is drawn entirely in red. This is a
 * coloring scan, not a full parser. Typically bound to a single-line {@link EditBox} via
 * {@code box.setFormatter(RegexHighlighter.INSTANCE.formatter(box))}.
 */
public final class RegexHighlighter implements SyntaxHighlighter {

    public static final RegexHighlighter INSTANCE = new RegexHighlighter();

    private RegexHighlighter() {
    }

    private static final int LITERAL = 0xE0E0E0;      // plain text
    private static final int ESCAPE = 0xFFD966;       // \d \w \. ...
    private static final int CHAR_CLASS = 0x8CD9A0;   // [...]
    private static final int GROUP = 0x9AD8FF;        // ( ) and (?: (?<name>
    private static final int QUANTIFIER = 0xE58CE0;   // * + ? { }
    private static final int ANCHOR = 0xFF9A6B;       // ^ $ | .
    private static final int ERROR = 0xFF5555;        // doesn't compile

    @Override
    public int[] colors(String source) {
        if (!compilesOk(source)) {
            int[] all = new int[source.length()];
            Arrays.fill(all, ERROR);
            return all;
        }
        return classify(source);
    }

    private static boolean compilesOk(String source) {
        try {
            Pattern.compile(source);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private static int[] classify(String s) {
        int[] colors = new int[s.length()];
        boolean inClass = false;
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '\\') {
                colors[i] = ESCAPE;
                if (i + 1 < s.length()) colors[++i] = ESCAPE; // the escaped char too
                continue;
            }
            if (inClass) {
                colors[i] = CHAR_CLASS;
                if (c == ']') inClass = false;
                continue;
            }
            colors[i] = switch (c) {
                case '[' -> {
                    inClass = true;
                    yield CHAR_CLASS;
                }
                case '(', ')' -> GROUP;
                case '*', '+', '?', '{', '}' -> QUANTIFIER;
                case '^', '$', '|', '.' -> ANCHOR;
                default -> LITERAL;
            };
        }
        return colors;
    }
}

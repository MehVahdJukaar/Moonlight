package net.mehvahdjukaar.moonlight.api.client.gui.misc;

import net.minecraft.client.gui.components.EditBox;

import java.util.Arrays;
import java.util.regex.Pattern;

/**
 * Small regex colorer. Picks a color per character of the pattern (escapes, character classes, groups, quantifiers,
 * anchors), and paints the whole thing red when the pattern doesn't compile. It only colors text, it doesn't parse
 * it. Usually hooked to a one line EditBox with box.setFormatter(RegexHighlighter.INSTANCE.formatter(box)).
 */
public final class RegexHighlighter implements SyntaxHighlighter {

    public static final RegexHighlighter INSTANCE = new RegexHighlighter();

    private static final int LITERAL = ConfigGuiColors.SYNTAX_DEFAULT;      // plain text
    private static final int ESCAPE = ConfigGuiColors.SYNTAX_ESCAPE;        // \d \w \. ...
    private static final int CHAR_CLASS = ConfigGuiColors.SYNTAX_CHAR_CLASS; // [...]
    private static final int GROUP = ConfigGuiColors.SYNTAX_GROUP;          // ( ) and (?: (?<name>
    private static final int QUANTIFIER = ConfigGuiColors.SYNTAX_QUANTIFIER; // * + ? { }
    private static final int ANCHOR = ConfigGuiColors.SYNTAX_ANCHOR;        // ^ $ | .
    private static final int ERROR = ConfigGuiColors.ERROR;                 // doesn't compile

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

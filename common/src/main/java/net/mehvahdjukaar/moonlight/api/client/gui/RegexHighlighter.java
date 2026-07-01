package net.mehvahdjukaar.moonlight.api.client.gui;

import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.util.FormattedCharSequence;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.regex.Pattern;

/**
 * Lightweight regex syntax highlighter for an {@link EditBox}. Plug it in via {@link EditBox#setFormatter}: it
 * classifies each character of the pattern (escapes, character classes, groups, quantifiers, anchors) and colors
 * it. A pattern that doesn't compile is drawn entirely in red. This is a coloring scan, not a full parser.
 */
public final class RegexHighlighter {

    private RegexHighlighter() {
    }

    private static final int LITERAL = 0xE0E0E0;      // plain text
    private static final int ESCAPE = 0xFFD966;       // \d \w \. ...
    private static final int CHAR_CLASS = 0x8CD9A0;   // [...]
    private static final int GROUP = 0x9AD8FF;        // ( ) and (?: (?<name>
    private static final int QUANTIFIER = 0xE58CE0;   // * + ? { }
    private static final int ANCHOR = 0xFF9A6B;       // ^ $ | .
    private static final int ERROR = 0xFF5555;        // doesn't compile

    /**
     * Builds a formatter bound to the given box. Caches the color scan per source string so it isn't recomputed
     * for every rendered chunk.
     */
    public static BiFunction<String, Integer, FormattedCharSequence> formatter(EditBox box) {
        return new BiFunction<>() {
            private String cachedSource;
            private int[] cachedColors; // null while the current source doesn't compile

            @Override
            public FormattedCharSequence apply(String chunk, Integer displayPos) {
                String source = box.getValue();
                if (!source.equals(cachedSource)) {
                    cachedSource = source;
                    cachedColors = compilesOk(source) ? classify(source) : null;
                }
                List<FormattedCharSequence> parts = new ArrayList<>(chunk.length());
                for (int i = 0; i < chunk.length(); i++) {
                    int index = displayPos + i;
                    int color = (cachedColors != null && index < cachedColors.length) ? cachedColors[index] : ERROR;
                    parts.add(FormattedCharSequence.forward(String.valueOf(chunk.charAt(i)),
                            Style.EMPTY.withColor(TextColor.fromRgb(color))));
                }
                return FormattedCharSequence.fromList(parts);
            }
        };
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

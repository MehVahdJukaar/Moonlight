package net.mehvahdjukaar.moonlight.api.client.gui.misc;

import net.mehvahdjukaar.moonlight.api.client.gui.widget.SyntaxEditBox;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.util.FormattedCharSequence;

import java.util.ArrayList;
import java.util.List;

/**
 * Colors one line of text at a time. All an implementation does is give a color per character of the line, and both
 * highlightLine (for SyntaxEditBox) and formatter (for EditBox.setFormatter) are built on top of that. The built-in
 * ones are singletons, and since this is a functional interface a plain line -> int[] lambda works too.
 */
@FunctionalInterface
public interface SyntaxHighlighter {

    int FALLBACK_COLOR = ConfigGuiColors.SYNTAX_DEFAULT;

    /** @return one RGB color per character of the line. */
    int[] colors(String line);

    default FormattedCharSequence highlightLine(String line) {
        if (line.isEmpty()) return FormattedCharSequence.EMPTY;
        int[] colors = colors(line);
        List<FormattedCharSequence> parts = new ArrayList<>();
        int runStart = 0;
        for (int i = 1; i <= line.length(); i++) {
            int prev = colorAt(colors, i - 1);
            if (i == line.length() || colorAt(colors, i) != prev) {
                parts.add(FormattedCharSequence.forward(line.substring(runStart, i), Style.EMPTY.withColor(TextColor.fromRgb(prev))));
                runStart = i;
            }
        }
        return FormattedCharSequence.fromList(parts);
    }

    /**
     * A formatter for EditBox.setFormatter. Colors the box's whole value, then hands back the piece it asked for.
     * The scan is cached per source string so it isn't redone for every rendered chunk.
     */
    default EditBox.TextFormatter formatter(EditBox box) {
        return new EditBox.TextFormatter() {
            private String cachedSource;
            private int[] cachedColors;

            @Override
            public FormattedCharSequence format(String chunk, int displayPos) {
                String source = box.getValue();
                if (!source.equals(cachedSource)) {
                    cachedSource = source;
                    cachedColors = colors(source);
                }
                List<FormattedCharSequence> parts = new ArrayList<>(chunk.length());
                for (int i = 0; i < chunk.length(); i++) {
                    int color = colorAt(cachedColors, displayPos + i);
                    parts.add(FormattedCharSequence.forward(String.valueOf(chunk.charAt(i)),
                            Style.EMPTY.withColor(TextColor.fromRgb(color))));
                }
                return FormattedCharSequence.fromList(parts);
            }
        };
    }

    private static int colorAt(int[] colors, int index) {
        return index >= 0 && index < colors.length ? colors[index] : FALLBACK_COLOR;
    }
}

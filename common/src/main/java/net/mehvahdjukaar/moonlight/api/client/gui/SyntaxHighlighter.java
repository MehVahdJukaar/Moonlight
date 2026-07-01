package net.mehvahdjukaar.moonlight.api.client.gui;

import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.util.FormattedCharSequence;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;

/**
 * A per-line syntax colorer. The one thing an implementation must do is classify each character of a line into an
 * RGB color ({@link #colors}); everything else is derived from that:
 * <ul>
 *     <li>{@link #highlightLine} renders a line as a colored {@link FormattedCharSequence} — used by
 *         {@link SyntaxEditBox} (multi-line).</li>
 *     <li>{@link #formatter} adapts it to {@link EditBox#setFormatter} — used to colorize a single-line box.</li>
 * </ul>
 * Built-in implementations are singletons: {@link JsonHighlighter#INSTANCE}, {@link NbtHighlighter#INSTANCE},
 * {@link RegexHighlighter#INSTANCE}. Being a functional interface, an ad-hoc {@code line -> int[]} lambda works too.
 */
@FunctionalInterface
public interface SyntaxHighlighter {

    int FALLBACK_COLOR = 0xE0E0E0;

    /** @return one RGB color per character of {@code line} (array length == {@code line.length()}). */
    int[] colors(String line);

    /** Renders one line as a colored sequence, coalescing runs of equal color. */
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
     * A formatter for {@link EditBox#setFormatter} bound to {@code box}: it colors the box's whole value with this
     * highlighter and hands back the requested chunk. The color scan is cached per source string so it isn't
     * recomputed for every rendered chunk.
     */
    default BiFunction<String, Integer, FormattedCharSequence> formatter(EditBox box) {
        return new BiFunction<>() {
            private String cachedSource;
            private int[] cachedColors;

            @Override
            public FormattedCharSequence apply(String chunk, Integer displayPos) {
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

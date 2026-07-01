package net.mehvahdjukaar.moonlight.api.client.config;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.FormattedCharSequence;

/**
 * Very small, line-by-line JSON syntax colorer used by {@link JsonEditBox}. Pretty-printed JSON keeps each string
 * on its own line, so tokenizing per line (no cross-line state) is enough for a "basic" highlight: strings, numbers,
 * the {@code true/false/null} literals and the structural punctuation each get their own color.
 */
final class JsonHighlighter {

    private JsonHighlighter() {
    }

    private static final int KEY = 0x9CDCFE;         // "key":
    private static final int STRING = 0xCE9178;      // "value"
    private static final int NUMBER = 0xB5CEA8;
    private static final int KEYWORD = 0x569CD6;     // true / false / null
    private static final int PUNCTUATION = 0x808080; // { } [ ] : ,
    private static final int DEFAULT = 0xD4D4D4;

    static FormattedCharSequence highlightLine(String line) {
        MutableComponent out = Component.empty();
        int i = 0;
        int n = line.length();
        while (i < n) {
            char c = line.charAt(i);
            if (c == '"') {
                int start = i++;
                while (i < n) {
                    char d = line.charAt(i++);
                    if (d == '\\' && i < n) i++; // skip escaped char
                    else if (d == '"') break;
                }
                String token = line.substring(start, i);
                // a string immediately followed by a colon (ignoring spaces) is a key
                int j = i;
                while (j < n && line.charAt(j) == ' ') j++;
                boolean isKey = j < n && line.charAt(j) == ':';
                append(out, token, isKey ? KEY : STRING);
            } else if (c == '-' || (c >= '0' && c <= '9')) {
                int start = i++;
                while (i < n && isNumberChar(line.charAt(i))) i++;
                append(out, line.substring(start, i), NUMBER);
            } else if (Character.isLetter(c)) {
                int start = i++;
                while (i < n && Character.isLetter(line.charAt(i))) i++;
                String word = line.substring(start, i);
                boolean keyword = word.equals("true") || word.equals("false") || word.equals("null");
                append(out, word, keyword ? KEYWORD : DEFAULT);
            } else if (c == '{' || c == '}' || c == '[' || c == ']' || c == ':' || c == ',') {
                append(out, String.valueOf(c), PUNCTUATION);
                i++;
            } else {
                append(out, String.valueOf(c), DEFAULT);
                i++;
            }
        }
        return out.getVisualOrderText();
    }

    private static boolean isNumberChar(char c) {
        return (c >= '0' && c <= '9') || c == '.' || c == 'e' || c == 'E' || c == '+' || c == '-';
    }

    private static void append(MutableComponent out, String text, int color) {
        out.append(Component.literal(text).withStyle(s -> s.withColor(color)));
    }
}

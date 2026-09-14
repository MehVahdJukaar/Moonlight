package net.mehvahdjukaar.moonlight.api.util;

import net.minecraft.network.chat.Component;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;

import java.net.URI;
import java.util.Arrays;
import java.util.Locale;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class TextHelper {

    // splits camelCase and PascalCase words. Acronym runs stay together, so FDLogo becomes FD Logo and URLs stays URLs
    private static final Pattern CAMEL_CASE_BOUNDARY =
            Pattern.compile("(?<=[a-z0-9])(?=[A-Z])|(?<=[A-Z])(?=[A-Z][a-z])(?![A-Z]s(?![a-z]))");

    public static String getReadableName(String name) {
        return Arrays.stream((name).replace(":", "_").split("_"))
                .flatMap(word -> Arrays.stream(CAMEL_CASE_BOUNDARY.split(word)))
                .filter(word -> !word.isEmpty())
                .map(StringUtils::capitalize).collect(Collectors.joining(" "));
    }

    public static Component getReadableComponent(String key, String... arguments) {
        Component translated = Component.translatable(key, (Object[]) arguments);
        if (translated.getString().equals(key)) {
            StringBuilder aa = new StringBuilder();
            for (String s : arguments) {
                aa.append("_").append(s);
            }
            return Component.literal(getReadableName(key + aa));
        }
        return translated;
    }

    public static String formatNumber(double v) {
        return v == Math.rint(v) && !Double.isInfinite(v) ? String.valueOf((long) v) : String.valueOf(v);
    }

    @Nullable
    public static String urlHost(String url) {
        try {
            String host = URI.create(url.trim()).getHost();
            return host == null ? null : host.toLowerCase(Locale.ROOT);
        } catch (Exception e) {
            return null;
        }
    }
}

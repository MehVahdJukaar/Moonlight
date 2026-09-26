package net.mehvahdjukaar.moonlight.core.misc;

public class ResourceLocationSanityCheck {

    public static void validateNamespace(String namespace, String path) {
        for (int i = 0; i < namespace.length(); i++) {
            char c = namespace.charAt(i);
            if (!(c >= 'a' && c <= 'z' || c >= '0' && c <= '9' || c == '_' || c == '.' || c == '-')) {
                throw new IllegalStateException("Invalid ResourceLocation namespace: " + namespace + ":" + path +
                        ". Some mod disabled vanilla id validation, check the mixins applied to ResourceLocation. This is VERY BAD!");
            }
        }
    }
}

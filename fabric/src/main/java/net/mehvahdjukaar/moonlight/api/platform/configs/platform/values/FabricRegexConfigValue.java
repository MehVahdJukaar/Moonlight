package net.mehvahdjukaar.moonlight.api.platform.configs.platform.values;

import net.mehvahdjukaar.moonlight.api.platform.configs.ConfigBuilder;
import net.mehvahdjukaar.moonlight.api.platform.configs.ConfigMetadata;

/**
 * A string config value constrained to be a valid regular expression. Stored exactly like a
 * {@link FabricStringConfigValue}; the distinct type just tells the screen to use the regex highlighting field.
 */
public class FabricRegexConfigValue extends FabricStringConfigValue {

    public FabricRegexConfigValue(String name, String defaultValue, ConfigMetadata meta) {
        super(name, defaultValue, ConfigBuilder.REGEX_CHECK, meta);
    }
}

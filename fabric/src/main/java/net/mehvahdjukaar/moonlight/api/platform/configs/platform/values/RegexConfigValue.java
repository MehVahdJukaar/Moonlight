package net.mehvahdjukaar.moonlight.api.platform.configs.platform.values;

import net.mehvahdjukaar.moonlight.api.platform.configs.ConfigBuilder;

/**
 * A string config value constrained to be a valid regular expression. Stored exactly like a
 * {@link StringConfigValue}; the distinct type just tells the screen to use the regex highlighting field.
 */
public class RegexConfigValue extends StringConfigValue {

    public RegexConfigValue(String name, String defaultValue) {
        super(name, defaultValue, ConfigBuilder.REGEX_CHECK);
    }
}

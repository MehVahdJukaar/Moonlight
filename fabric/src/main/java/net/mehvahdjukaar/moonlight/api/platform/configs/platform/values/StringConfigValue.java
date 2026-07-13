package net.mehvahdjukaar.moonlight.api.platform.configs.platform.values;

import net.mehvahdjukaar.moonlight.api.platform.configs.ConfigMetadata;

import com.google.common.base.Preconditions;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

import java.util.function.Predicate;

public class StringConfigValue extends ConfigValue<String> {

    private final Predicate<Object> validator;

    public StringConfigValue(String name, String defaultValue, Predicate<Object> validator, ConfigMetadata meta) {
        super(name, defaultValue, meta);
        this.validator = validator;
        Preconditions.checkState(isValid(defaultValue), "Config defaults are invalid");
    }

    @Override
    public boolean isValid(String value) {
        return validator.test(value);
    }

    @Override
    protected String parseValue(JsonElement element) {
        return element.getAsString();
    }

    @Override
    protected JsonElement encodeValue(String value) {
        return new JsonPrimitive(value);
    }
}

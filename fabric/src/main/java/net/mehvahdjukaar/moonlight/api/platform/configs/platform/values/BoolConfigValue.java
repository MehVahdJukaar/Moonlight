package net.mehvahdjukaar.moonlight.api.platform.configs.platform.values;

import net.mehvahdjukaar.moonlight.api.platform.configs.ConfigMetadata;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

public class BoolConfigValue extends ConfigValue<Boolean> {

    public BoolConfigValue(String name, Boolean defaultValue, ConfigMetadata meta) {
        super(name, defaultValue, meta);
    }

    @Override
    public boolean isValid(Boolean value) {
        return true;
    }

    @Override
    protected Boolean parseValue(JsonElement element) {
        return element.getAsBoolean();
    }

    @Override
    protected JsonElement encodeValue(Boolean value) {
        return new JsonPrimitive(value);
    }
}

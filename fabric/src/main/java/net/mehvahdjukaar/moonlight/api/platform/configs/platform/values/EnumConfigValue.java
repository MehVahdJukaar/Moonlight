package net.mehvahdjukaar.moonlight.api.platform.configs.platform.values;

import net.mehvahdjukaar.moonlight.api.platform.configs.ConfigMetadata;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

public class EnumConfigValue<T extends Enum<T>> extends ConfigValue<T> {

    private final T[] acceptedValues;

    public EnumConfigValue(String name, T defaultValue, ConfigMetadata meta) {
        super(name, defaultValue, meta);
        this.acceptedValues = defaultValue.getDeclaringClass().getEnumConstants();
    }

    @Override
    public boolean isValid(T value) {
        return true;
    }

    public Class<T> getEnumClass() {
        return this.defaultValue.getDeclaringClass();
    }

    @Override
    protected T parseValue(JsonElement element) {
        String s = element.getAsString();
        for (var v : acceptedValues) {
            if (v.name().equals(s)) return v;
        }
        return null; // unknown constant -> falls back to the default
    }

    @Override
    protected JsonElement encodeValue(T value) {
        return new JsonPrimitive(value.name());
    }
}

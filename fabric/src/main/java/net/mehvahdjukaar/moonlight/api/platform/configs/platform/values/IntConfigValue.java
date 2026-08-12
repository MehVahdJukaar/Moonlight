package net.mehvahdjukaar.moonlight.api.platform.configs.platform.values;

import net.mehvahdjukaar.moonlight.api.platform.configs.ConfigMetadata;

import com.google.common.base.Preconditions;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

import java.util.Objects;

public class IntConfigValue extends ConfigValue<Integer> {

    private final Integer min;
    private final Integer max;

    public IntConfigValue(String name, Integer defaultValue, Integer min, Integer max, ConfigMetadata meta) {
        super(name, defaultValue, meta);
        this.min = Objects.requireNonNull(min);
        this.max = Objects.requireNonNull(max);
        Preconditions.checkState(isValid(defaultValue), "Config defaults are invalid");
    }

    @Override
    public boolean isValid(Integer value) {
        return value >= min && value <= max;
    }

    @Override
    protected Integer parseValue(JsonElement element) {
        return element.getAsInt();
    }

    @Override
    protected JsonElement encodeValue(Integer value) {
        return new JsonPrimitive(value);
    }

    public Integer getMax() {
        return max;
    }

    public Integer getMin() {
        return min;
    }
}

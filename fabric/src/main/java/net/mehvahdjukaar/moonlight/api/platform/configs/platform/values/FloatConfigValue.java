package net.mehvahdjukaar.moonlight.api.platform.configs.platform.values;

import net.mehvahdjukaar.moonlight.api.platform.configs.ConfigMeta;

import com.google.common.base.Preconditions;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

import java.util.Objects;

public class FloatConfigValue extends ConfigValue<Float> {

    private final Float min;
    private final Float max;

    public FloatConfigValue(String name, Float defaultValue, Float min, Float max, ConfigMeta meta) {
        super(name, defaultValue, meta);
        this.min = Objects.requireNonNull(min);
        this.max = Objects.requireNonNull(max);
        Preconditions.checkState(isValid(defaultValue), "Config defaults are invalid");
    }

    @Override
    public boolean isValid(Float value) {
        return value >= min && value <= max;
    }

    @Override
    protected Float parseValue(JsonElement element) {
        return element.getAsFloat();
    }

    @Override
    protected JsonElement encodeValue(Float value) {
        return new JsonPrimitive(value);
    }

    public Float getMax() {
        return max;
    }

    public Float getMin() {
        return min;
    }

    @Override
    public String getExtraInfo() {
        return "Accepted range: " + min + " to " + max;
    }
}

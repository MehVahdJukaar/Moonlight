package net.mehvahdjukaar.moonlight.api.platform.configs.platform.values;

import net.mehvahdjukaar.moonlight.api.platform.configs.ConfigMetadata;

import com.google.common.base.Preconditions;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

import java.util.Objects;

public class DoubleConfigValue extends ConfigValue<Double> {

    private final Double min;
    private final Double max;

    public DoubleConfigValue(String name, Double defaultValue, Double min, Double max, ConfigMetadata meta) {
        super(name, defaultValue, meta);
        this.min = Objects.requireNonNull(min);
        this.max = Objects.requireNonNull(max);
        Preconditions.checkState(isValid(defaultValue), "Config defaults are invalid");
    }

    @Override
    public boolean isValid(Double value) {
        return value >= min && value <= max;
    }

    @Override
    protected Double parseValue(JsonElement element) {
        return element.getAsDouble();
    }

    @Override
    protected JsonElement encodeValue(Double value) {
        return new JsonPrimitive(value);
    }

    public Double getMax() {
        return max;
    }

    public Double getMin() {
        return min;
    }

    @Override
    public String getExtraInfo() {
        return "Accepted range: " + min + " to " + max;
    }
}

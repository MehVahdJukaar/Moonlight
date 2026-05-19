package net.mehvahdjukaar.moonlight.api.platform.configs.platform.values;

import com.google.common.base.Preconditions;
import com.google.gson.JsonObject;
import net.mehvahdjukaar.moonlight.core.Moonlight;

import java.util.Objects;

public class IntConfigValue extends ConfigValue<Integer> {

    private final Integer min;
    private final Integer max;

    public IntConfigValue(String name, Integer defaultValue, Integer min, Integer max) {
        super(name, defaultValue);
        this.min = Objects.requireNonNull(min);
        this.max = Objects.requireNonNull(max);
        Preconditions.checkState(isValid(defaultValue), "Config defaults are invalid");
    }

    @Override
    public boolean isValid(Integer value) {
        return value >= min && value <= max;
    }

    @Override
    public boolean loadFromJson(JsonObject element) {
        if (element.has(this.name)) {
            try {
                Integer newValue = element.get(this.name).getAsInt();
                if (!this.isValid(newValue)) {
                    //if not valid it defaults
                    newValue = defaultValue;
                }
                boolean changed = this.setAndTrack(newValue);
                this.markLoaded();
                return this.affectsDynamicPacks() && changed;
            } catch (Exception ignored) {
            }
            Moonlight.LOGGER.warn("Config file had incorrect entry {}, correcting", this.name);
        } else {
            Moonlight.LOGGER.warn("Config file had missing entry {}", this.name);
        }
        this.markLoaded();
        return false;
    }

    @Override
    public void saveToJson(JsonObject object) {
        if (this.value == null) this.value = defaultValue;
        object.addProperty(this.name, this.value);
    }

    public Integer getMax() {
        return max;
    }

    public Integer getMin() {
        return min;
    }

    @Override
    public String getExtraInfo() {
        return "Accepted range: " + min + " to " + max;
    }
}

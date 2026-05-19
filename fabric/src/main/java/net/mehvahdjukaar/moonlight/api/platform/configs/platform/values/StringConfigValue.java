package net.mehvahdjukaar.moonlight.api.platform.configs.platform.values;

import com.google.common.base.Preconditions;
import com.google.gson.JsonObject;
import net.mehvahdjukaar.moonlight.core.Moonlight;

import java.util.function.Predicate;

public class StringConfigValue extends ConfigValue<String> {

    private final Predicate<Object> validator;

    public StringConfigValue(String name, String defaultValue, Predicate<Object> validator) {
        super(name, defaultValue);
        this.validator = validator;
        Preconditions.checkState(isValid(defaultValue), "Config defaults are invalid");
    }

    @Override
    public boolean isValid(String value) {
        return validator.test(value);
    }

    @Override
    public boolean loadFromJson(JsonObject element) {
        if (element.has(this.name)) {
            try {
                String newValue = element.get(this.name).getAsString();
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


}

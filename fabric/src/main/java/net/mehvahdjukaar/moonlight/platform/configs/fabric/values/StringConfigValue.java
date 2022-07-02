package net.mehvahdjukaar.moonlight.platform.configs.fabric.values;

import com.google.gson.JsonObject;
import net.mehvahdjukaar.moonlight.Moonlight;
import net.mehvahdjukaar.moonlight.platform.configs.fabric.values.ConfigValue;

public class StringConfigValue extends ConfigValue<String> {

    public StringConfigValue(String name, String defaultValue) {
        super(name, defaultValue);
    }

    @Override
    public boolean isValid(String value) {
        return true;
    }

    @Override
    public void loadFromJson(JsonObject element) {
        if (element.has(this.name)) {
            try {
                this.value = element.get(this.name).getAsString();
                if (this.isValid(value)) return;
                //if not valid it defaults
                this.value = defaultValue;
            } catch (Exception ignored) {
            }
            Moonlight.LOGGER.warn("Config file had incorrect entry {}, correcting", this.name);
        } else {
            Moonlight.LOGGER.warn("Config file had missing entry {}", this.name);
        }
    }

    @Override
    public void saveToJson(JsonObject object) {
        if (this.value == null) this.value = defaultValue;
        object.addProperty(this.name, this.value);
    }


}

package net.mehvahdjukaar.moonlight.api.platform.configs.fabric.values;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.mehvahdjukaar.moonlight.core.Moonlight;

public class JsonConfigValue extends ConfigValue<JsonElement> {

    public JsonConfigValue(String name, JsonElement defaultValue) {
        super(name, defaultValue);
    }

    @Override
    public boolean isValid(JsonElement value) {
        return value.isJsonObject();
    }

    @Override
    public void loadFromJson(JsonObject element) {
        if (element.has(this.name)) {
            try {
                this.value = element.get(this.name);
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
        object.add(this.name, this.value);
    }
}

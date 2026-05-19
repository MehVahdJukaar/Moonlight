package net.mehvahdjukaar.moonlight.api.platform.configs.platform.values;

import com.google.gson.JsonObject;
import net.mehvahdjukaar.moonlight.core.Moonlight;

public class BoolConfigValue extends ConfigValue<Boolean> {

    public BoolConfigValue(String name, Boolean defaultValue) {
        super(name, defaultValue);
    }

    @Override
    public boolean isValid(Boolean value) {
        return true;
    }

    @Override
    public boolean loadFromJson(JsonObject element) {
        if (element.has(this.name)) {
            try {
                Boolean newValue = element.get(this.name).getAsBoolean();
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

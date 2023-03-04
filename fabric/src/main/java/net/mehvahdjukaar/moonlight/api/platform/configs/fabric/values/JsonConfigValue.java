package net.mehvahdjukaar.moonlight.api.platform.configs.fabric.values;

import com.google.common.base.Suppliers;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.mehvahdjukaar.moonlight.core.Moonlight;

import java.util.function.Supplier;

public class JsonConfigValue extends ConfigValue<JsonElement> {

    private final com.google.common.base.Supplier<JsonElement> defValue;

    public JsonConfigValue(String name, Supplier<JsonElement> defaultSupplier) {
        super(name, null);
        this.defValue = Suppliers.memoize(defaultSupplier::get);
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
                this.value = getDefaultValue();
            } catch (Exception ignored) {
            }
            Moonlight.LOGGER.warn("Config file had incorrect entry {}, correcting", this.name);
        } else {
            Moonlight.LOGGER.warn("Config file had missing entry {}", this.name);
        }
    }

    @Override
    public JsonElement getDefaultValue() {
        return defValue.get();
    }

    @Override
    public void saveToJson(JsonObject object) {
        if (this.value == null) this.value = getDefaultValue();
        object.add(this.name, this.value);
    }
}

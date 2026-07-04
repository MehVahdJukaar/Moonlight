package net.mehvahdjukaar.moonlight.api.platform.configs.platform.values;

import net.mehvahdjukaar.moonlight.api.platform.configs.ConfigMeta;

import com.google.common.base.Suppliers;
import com.google.gson.JsonElement;

import java.util.function.Supplier;

public class JsonConfigValue extends ConfigValue<JsonElement> {

    private final Supplier<JsonElement> defValue;

    public JsonConfigValue(String name, Supplier<JsonElement> defaultSupplier, ConfigMeta meta) {
        super(name, null, meta);
        this.defValue = Suppliers.memoize(defaultSupplier::get);
    }

    @Override
    public boolean isValid(JsonElement value) {
        return value.isJsonObject();
    }

    @Override
    protected JsonElement parseValue(JsonElement element) {
        return element; // validity (must be a json object) is enforced by isValid in the base loader
    }

    @Override
    protected JsonElement encodeValue(JsonElement value) {
        return value;
    }

    @Override
    public JsonElement getDefaultValue() {
        return defValue.get();
    }
}

package net.mehvahdjukaar.moonlight.api.platform.configs.fabric.values;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import net.mehvahdjukaar.moonlight.core.Moonlight;

public class ObjectConfigValue<T> extends ConfigValue<T> {

    private final Codec<T> codec;
    private final Supplier<T> lazyDefault;

    public ObjectConfigValue(String name, Supplier<T> defaultValue, Codec<T> codec) {
        super(name, null);
        this.codec = codec;
        this.lazyDefault = Suppliers.memoize(defaultValue);
    }

    @Override
    public boolean isValid(T value) {
        return true;
    }

    @Override
    public void loadFromJson(JsonObject element) {
        if (element.has(this.name)) {
            try {
                JsonElement j = element.get(this.name);
                var e = codec.decode(JsonOps.INSTANCE, j);
                var json = e.resultOrPartial(s -> {
                    Moonlight.LOGGER.warn("Failed to parse config {}: {}" + name, s);
                });
                if (json.isPresent()) {
                    this.value = json.get().getFirst();
                    return;
                }
                Moonlight.LOGGER.warn("Config file had incorrect entry {}, correcting " + name);
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
    public T getDefaultValue() {
        return lazyDefault.get();
    }

    @Override
    public void saveToJson(JsonObject object) {
        if (this.value == null) this.value = getDefaultValue();
        var e = codec.encodeStart(JsonOps.INSTANCE, value);
        var json = e.resultOrPartial(s -> {
            throw new RuntimeException("Failed to parse config " + name + ": " + s);
        });
        json.ifPresent(jsonElement -> object.add(this.name, jsonElement));
    }
}

package net.mehvahdjukaar.moonlight.api.platform.configs.platform.values;

import net.mehvahdjukaar.moonlight.api.platform.configs.ConfigMetadata;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.google.gson.JsonElement;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import net.mehvahdjukaar.moonlight.core.Moonlight;

public class ObjectConfigValue<T> extends ConfigValue<T> {

    private final Codec<T> codec;
    private final Supplier<T> lazyDefault;

    public ObjectConfigValue(String name, Supplier<T> defaultValue, Codec<T> codec, ConfigMetadata meta) {
        super(name, null, meta);
        this.codec = codec;
        this.lazyDefault = Suppliers.memoize(defaultValue);
    }

    @Override
    public boolean isValid(T value) {
        return true;
    }

    @Override
    protected T parseValue(JsonElement element) {
        var decoded = codec.decode(JsonOps.INSTANCE, element)
                .resultOrPartial(s -> Moonlight.LOGGER.warn("Failed to parse config {}: {}", name, s));
        return decoded.map(com.mojang.datafixers.util.Pair::getFirst).orElse(getDefaultValue());
    }

    @Override
    protected JsonElement encodeValue(T value) {
        return codec.encodeStart(JsonOps.INSTANCE, value)
                .resultOrPartial(s -> {
                    throw new RuntimeException("Failed to parse config " + name + ": " + s);
                })
                .orElse(null);
    }

    @Override
    public T getDefaultValue() {
        return lazyDefault.get();
    }
}

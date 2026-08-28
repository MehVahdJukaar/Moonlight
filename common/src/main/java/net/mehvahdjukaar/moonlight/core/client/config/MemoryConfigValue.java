package net.mehvahdjukaar.moonlight.core.client.config;

import net.mehvahdjukaar.moonlight.api.platform.configs.IConfigValue;
import net.mehvahdjukaar.moonlight.api.platform.configs.options.ConfigReloadType;

import java.util.Objects;

// in-memory value backing a leaf of a schema generated form. Nothing is persisted through it
class MemoryConfigValue<T> implements IConfigValue<T> {

    private T value;

    MemoryConfigValue(T value) {
        this.value = value;
    }

    @Override
    public T get() {
        return value;
    }

    @Override
    public boolean setValue(T value) {
        boolean changed = !Objects.equals(this.value, value);
        this.value = value;
        return changed;
    }

    @Override
    public ConfigReloadType reloadType() {
        return ConfigReloadType.NONE;
    }

    @Override
    public boolean affectsDynamicPacks() {
        return false;
    }
}

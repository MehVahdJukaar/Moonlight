package net.mehvahdjukaar.moonlight.core.client.config.schema;

import net.mehvahdjukaar.moonlight.api.platform.configs.IConfigValue;
import net.mehvahdjukaar.moonlight.api.platform.configs.options.ConfigReloadType;

import java.util.Objects;

class InMemoryConfigValue<T> implements IConfigValue<T> {

    private T value;

    InMemoryConfigValue(T value) {
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

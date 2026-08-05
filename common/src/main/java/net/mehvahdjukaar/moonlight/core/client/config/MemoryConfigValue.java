package net.mehvahdjukaar.moonlight.core.client.config;

import net.mehvahdjukaar.moonlight.api.platform.configs.IConfigValue;
import net.mehvahdjukaar.moonlight.api.platform.configs.options.ConfigReloadType;

import java.util.Objects;

// Transient in-memory value backing each leaf of a schema-generated form, so the reused row/control machinery can
// drive it like a real config value. Nothing is persisted through here: the editor reads working values straight out
// of its ConfigEditSession and re-encodes them through the codec on Done.
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

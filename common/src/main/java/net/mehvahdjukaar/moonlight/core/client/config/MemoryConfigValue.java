package net.mehvahdjukaar.moonlight.core.client.config;

import net.mehvahdjukaar.moonlight.api.platform.configs.WritableConfigValue;
import net.mehvahdjukaar.moonlight.api.platform.configs.options.ConfigReloadType;

import java.util.Objects;

/**
 * A transient, in-memory {@link WritableConfigValue} used only by the schema-driven editor ({@link SchemaEditScreen}):
 * it backs each generated form leaf so the whole reused config-row/control machinery ({@code OptionRow},
 * {@link ConfigControls}) can drive it exactly like a real config value, without touching any on-disk config. Nothing
 * is persisted through here — the editor reads the working values straight out of its {@code ConfigEditSession} and
 * re-encodes them through the codec on Done. Carries no change metadata (it never triggers a reload / pack refresh).
 */
class MemoryConfigValue<T> implements WritableConfigValue<T> {

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

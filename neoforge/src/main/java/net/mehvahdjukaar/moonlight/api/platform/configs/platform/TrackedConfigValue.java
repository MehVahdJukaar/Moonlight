package net.mehvahdjukaar.moonlight.api.platform.configs.platform;

import net.mehvahdjukaar.moonlight.api.platform.configs.ConfigValueHandle;
import net.mehvahdjukaar.moonlight.api.platform.configs.DynamicPackTrigger;

/**
 * NeoForge storage value: a {@link ConfigValueHandle} that also carries the storage-side {@link DynamicPackTrigger}
 * metadata. On top of those it can be asked whether the backing {@code ModConfigSpec} value changed since the last
 * check ({@link #pollChanged()}), which the holder uses together with {@link #affectsDynamicPacks()} to decide
 * dynamic-pack invalidation when Forge fires its single coarse config-reload event.
 */
public interface TrackedConfigValue<T> extends ConfigValueHandle<T>, DynamicPackTrigger {

    /** Refreshes the cached value from the backing spec; returns whether it changed since the previous poll. */
    boolean pollChanged();
}

package net.mehvahdjukaar.moonlight.api.platform.configs.platform;

import net.mehvahdjukaar.moonlight.api.platform.configs.IConfigValue;

/**
 * NeoForge leaf storage value: a {@link IConfigValue} (settable handle + change metadata) that can also be
 * asked whether the backing {@code ModConfigSpec} value changed since the last check ({@link #pollChanged()}), which
 * the holder uses together with {@link #affectsDynamicPacks()} to decide dynamic-pack invalidation when Forge fires
 * its single coarse config-reload event.
 */
public interface TrackedConfigValue<T> extends IConfigValue<T> {

    /** Refreshes the cached value from the backing spec; returns whether it changed since the previous poll. */
    boolean pollChanged();
}

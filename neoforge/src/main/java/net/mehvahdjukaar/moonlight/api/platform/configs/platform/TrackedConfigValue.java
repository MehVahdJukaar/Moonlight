package net.mehvahdjukaar.moonlight.api.platform.configs.platform;

import net.mehvahdjukaar.moonlight.api.platform.configs.ConfigValueHandle;

/**
 * NeoForge {@link ConfigValueHandle}. On top of the common handle it can be asked whether the backing
 * {@code ModConfigSpec} value changed since the last check ({@link #pollChanged()}), which the holder uses to
 * decide dynamic-pack invalidation when Forge fires its single coarse config-reload event.
 */
public interface TrackedConfigValue<T> extends ConfigValueHandle<T> {

    /** Refreshes the cached value from the backing spec; returns whether it changed since the previous poll. */
    boolean pollChanged();
}

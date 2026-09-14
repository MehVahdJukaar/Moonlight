package net.mehvahdjukaar.moonlight.api.platform.configs.platform;

import net.mehvahdjukaar.moonlight.api.platform.configs.IConfigValue;

// A leaf value that can also be asked whether the backing spec value changed since the last check. The holder uses
// that with affectsDynamicPacks() to decide pack invalidation when Forge fires its single coarse reload event.
public interface TrackedConfigValue<T> extends IConfigValue<T> {

    // refreshes the cached value from the backing spec and reports whether it changed since the previous poll
    boolean pollChanged();
}

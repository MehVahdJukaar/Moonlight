package net.mehvahdjukaar.moonlight.api.platform.configs.platform;

import net.mehvahdjukaar.moonlight.api.platform.configs.IConfigValue;

// used with affectsDynamicPacks to decide pack invalidation on the coarse forge reload event
public interface TrackedConfigValue<T> extends IConfigValue<T> {

    boolean pollChanged();
}

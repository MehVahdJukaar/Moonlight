package net.mehvahdjukaar.moonlight.api.platform.configs.platform;

import net.mehvahdjukaar.moonlight.api.platform.configs.IConfigWrapper;

import java.util.function.Supplier;

public interface TrackedConfigValue<T> extends Supplier<T>, IConfigWrapper {

    boolean pollChanged();

    boolean setValue(T value);
}

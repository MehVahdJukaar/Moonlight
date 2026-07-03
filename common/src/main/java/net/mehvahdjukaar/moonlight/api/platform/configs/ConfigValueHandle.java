package net.mehvahdjukaar.moonlight.api.platform.configs;

import java.util.function.Supplier;

/**
 * Loader independent handle to a single config value: the object every {@code define(...)} returns.
 * Reading it ({@link #get()}) gives the live value; {@link #setValue} writes a new one. Both platforms have their
 * own implementation (a {@code ConfigValue} on Fabric, a {@code TrackedConfigValue} on NeoForge) but the screen and
 * the {@link ModConfigHolder} only ever talk to this interface, so nothing above the platform layer needs to know
 * which loader it is running on.
 *
 * @param <T> the value type
 */
public interface ConfigValueHandle<T> extends Supplier<T> {

    /** Writes a new (already validated) value. Returns whether it actually differed from the previous one. */
    boolean setValue(T value);
}

package net.mehvahdjukaar.moonlight.api.platform.configs;

import net.mehvahdjukaar.moonlight.api.platform.configs.options.ConfigReloadType;
import org.jetbrains.annotations.ApiStatus;

import java.util.function.Supplier;

/**
 * The settable leaf behind every {@code define(...)}: a read/write value ({@link #get()} + {@link #setValue}) that also
 * carries its own change metadata ({@link #reloadType()} and {@link #affectsDynamicPacks()}). Mods only ever see the
 * read-only {@link Supplier} view (what {@code define()} returns); the config screen writes through this type, and
 * {@link ModConfigHolder#manuallySetValue} recovers it with a single cast.
 * <p>
 * The metadata methods are value-type independent, so consumers that only need the metadata (the screen aggregating
 * reload effects) use a {@code WritableConfigValue<?>} wildcard and ignore {@code T}. This is a read-only view: the
 * metadata is injected once at build time (see {@link ConfigMeta}) and never mutated through here. Fabric's
 * {@code ConfigValue} implements this; NeoForge's {@code TrackedConfigValue} extends it.
 */
@ApiStatus.Internal
public interface WritableConfigValue<T> extends Supplier<T> {

    boolean setValue(T value);

    ConfigReloadType reloadType();

    boolean affectsDynamicPacks();
}

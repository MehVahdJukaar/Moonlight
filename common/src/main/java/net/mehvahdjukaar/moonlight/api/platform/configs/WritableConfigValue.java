package net.mehvahdjukaar.moonlight.api.platform.configs;

import org.jetbrains.annotations.ApiStatus;

/**
 * A leaf config value the config screen can write to: a {@link ConfigValueHandle} (readable + settable) that also
 * carries its {@link ConfigValueMeta} change metadata. Every {@code define(...)} is backed by one of these, but it is
 * handed to mods only through the read-only {@link java.util.function.Supplier} view. Keeping the write handle and the
 * metadata under one type lets {@link ModConfigHolder#manuallySetValue} act on a value with a single boundary cast
 * instead of an {@code instanceof} chain.
 */
@ApiStatus.Internal
public interface WritableConfigValue<T> extends ConfigValueHandle<T>, ConfigValueMeta {
}

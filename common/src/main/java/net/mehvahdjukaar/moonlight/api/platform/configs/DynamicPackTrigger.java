package net.mehvahdjukaar.moonlight.api.platform.configs;

import org.jetbrains.annotations.ApiStatus;

/**
 * Build-time metadata for a config value whose change should invalidate this config's dynamic resource/data packs.
 * This is a storage-side concern (read by the {@link ModConfigHolder} when a value changes on disk, on sync, or from
 * the screen) and is deliberately kept off {@link ConfigValueHandle} — the value handle a mod receives has no business
 * reading or mutating it. It is the storage-side counterpart of {@link
 * net.mehvahdjukaar.moonlight.api.platform.configs.options.ConfigReloadType} (which lives on the UI-side
 * {@code ConfigOption}): both describe "what happens when this value changes", each attached to the object of the
 * subsystem that actually consumes it. The setter is only ever called by the builder while assembling the config.
 */
@ApiStatus.Internal
public interface DynamicPackTrigger {

    /** Whether changing this value should invalidate dynamic resource/data packs. */
    boolean affectsDynamicPacks();

    /** Build-time only: called by the builder to mark this value as pack-affecting. */
    void setAffectsDynamicPacks(boolean affectsDynamicPacks);
}
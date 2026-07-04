package net.mehvahdjukaar.moonlight.api.platform.configs;

import net.mehvahdjukaar.moonlight.api.platform.configs.options.ConfigReloadType;
import org.jetbrains.annotations.ApiStatus;

/**
 * The build-time change metadata of a single {@link WritableConfigValue} leaf: how a change takes effect
 * ({@link #reloadType}) and whether it should invalidate dynamic resource/data packs ({@link #affectsDynamicPacks}).
 * Bundled into one immutable value so leaves can take it in their constructor instead of exposing setters — the
 * builder reads its pending flags into a {@code ConfigMeta} the moment each leaf is defined.
 */
@ApiStatus.Internal
public record ConfigMeta(ConfigReloadType reloadType, boolean affectsDynamicPacks) {

    public static final ConfigMeta NONE = new ConfigMeta(ConfigReloadType.NONE, false);
}

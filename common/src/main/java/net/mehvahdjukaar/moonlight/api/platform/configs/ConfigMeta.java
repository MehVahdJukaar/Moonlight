package net.mehvahdjukaar.moonlight.api.platform.configs;

import net.mehvahdjukaar.moonlight.api.platform.configs.options.ConfigReloadType;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public record ConfigMeta(ConfigReloadType reloadType, boolean affectsDynamicPacks) {

    public static final ConfigMeta NONE = new ConfigMeta(ConfigReloadType.NONE, false);
}

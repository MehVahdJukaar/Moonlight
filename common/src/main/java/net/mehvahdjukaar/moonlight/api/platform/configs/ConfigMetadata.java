package net.mehvahdjukaar.moonlight.api.platform.configs;

import net.mehvahdjukaar.moonlight.api.platform.configs.options.ConfigReloadType;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public record ConfigMetadata(ConfigReloadType reloadType, boolean affectsDynamicPacks) {

    public static final ConfigMetadata NONE = new ConfigMetadata(ConfigReloadType.NONE, false);
}

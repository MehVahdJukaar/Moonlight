package net.mehvahdjukaar.moonlight.core;

import net.mehvahdjukaar.moonlight.api.platform.PlatformHelper;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public class CompatHandler {

    public static final boolean MAP_ATLASES = PlatformHelper.isModLoaded("map_atlases");
    public static final boolean MODERNFIX = PlatformHelper.isModLoaded("modernfix");
    public static final boolean YACL = PlatformHelper.isModLoaded("yet-another-config-lib");
    public static final boolean CLOTH_CONFIG = PlatformHelper.isModLoaded("cloth-config");

}
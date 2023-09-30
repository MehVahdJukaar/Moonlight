package net.mehvahdjukaar.moonlight.core;

import net.mehvahdjukaar.moonlight.api.platform.PlatHelper;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public class CompatHandler {

    public static final boolean TWILIGHTFOREST = PlatHelper.isModLoaded("twilightforest");
    public static final boolean MAP_ATLASES = PlatHelper.isModLoaded("map_atlases");
    public static final boolean MODERNFIX = PlatHelper.isModLoaded("modernfix");
    public static final boolean YACL = PlatHelper.isModLoaded("yet-another-config-lib");
    public static final boolean CLOTH_CONFIG = PlatHelper.isModLoaded("cloth-config");

}

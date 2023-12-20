package net.mehvahdjukaar.moonlight.core;

import net.mehvahdjukaar.moonlight.api.platform.PlatHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.MapItem;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public class CompatHandler {

    public static final boolean MAP_ATLASES = PlatHelper.isModLoaded("map_atlases");
    public static final boolean MODERNFIX = PlatHelper.isModLoaded("modernfix");
    public static final boolean YACL = PlatHelper.isModLoaded("yet-another-config-lib");
    public static final boolean CLOTH_CONFIG = PlatHelper.isModLoaded("cloth-config");

    public static MapItemSavedData getMapDataFromKnownKeys(ServerLevel level, int mapId) {
        var d = level.getMapData(MapItem.makeKey(mapId));
        if (d == null) {
            d = level.getMapData("magicmap_" + mapId);
            if (d == null) {
                d = level.getMapData("mazemap_" + mapId);
            }
        }
        return d;
    }
}

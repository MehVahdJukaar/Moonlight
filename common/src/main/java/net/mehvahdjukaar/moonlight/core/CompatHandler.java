package net.mehvahdjukaar.moonlight.core;

import net.mehvahdjukaar.moonlight.api.platform.PlatHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.MapItem;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public class CompatHandler {

    public static final boolean MAP_ATLASES = PlatHelper.isModLoaded("map_atlases") &&
            compareVersions(PlatHelper.getModVersion("map_atlases"),"1.20-2.7.0")>=0;
    public static final boolean MODERNFIX = PlatHelper.isModLoaded("modernfix");
    public static final boolean YACL = PlatHelper.isModLoaded("yet-another-config-lib");
    public static final boolean CLOTH_CONFIG = PlatHelper.isModLoaded("cloth-config");

    private static int compareVersions(String version1, String version2) {
        if (version2.contains("-")) {
            version2 = version2.split("-")[1];
        }
        if (version1.contains("-")) {
            version1 = version1.split("-")[1];
        }
        String[] splitVersion1 = version1.split("\\.");
        String[] splitVersion2 = version2.split("\\.");

        int length = Math.max(splitVersion1.length, splitVersion2.length);

        for (int i = 0; i < length; i++) {
            Integer v1 = i < splitVersion1.length ? Integer.parseInt(splitVersion1[i]) : 0;
            Integer v2 = i < splitVersion2.length ? Integer.parseInt(splitVersion2[i]) : 0;

            int comparison = v1.compareTo(v2);
            if (comparison != 0) {
                return comparison;
            }
        }
        return 0;
    }
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

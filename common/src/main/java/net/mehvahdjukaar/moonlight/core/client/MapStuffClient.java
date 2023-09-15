package net.mehvahdjukaar.moonlight.core.client;

import net.mehvahdjukaar.moonlight.api.map.CustomMapData;
import net.mehvahdjukaar.moonlight.api.map.CustomMapDecoration;
import net.mehvahdjukaar.moonlight.api.map.ExpandedMapData;
import net.mehvahdjukaar.moonlight.core.Moonlight;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.MapRenderer;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.MapItem;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.util.Map;

public class MapStuffClient {


    public static void handlePacketExtension(int mapId, byte scale, boolean locked,
                                             int centerX, int centerZ,
                                             @Nullable CustomMapDecoration[] serverDeco,
                                             @Nullable CustomMapData[] serverData) {
        if (serverDeco == null && serverData == null) return;

        MapRenderer mapRenderer = Minecraft.getInstance().gameRenderer.getMapRenderer();

        String string = MapItem.makeKey(mapId);
        ClientLevel level = Minecraft.getInstance().level;
        MapItemSavedData mapData = level.getMapData(string);

        //same behavior as a normal packet
        if (mapData == null) {
            mapData = MapItemSavedData.createForClient(scale, locked, level.dimension());
            level.overrideMapData(string, mapData);
        }

        mapData.centerX = centerX;
        mapData.centerZ = centerZ;

        if (mapData instanceof ExpandedMapData ed) {
            //mapData = MapItemSavedData.createForClient(message.scale, message.locked, Minecraft.getInstance().level.dimension());
            //Minecraft.getInstance().level.setMapData(string, mapData);

            if(serverDeco != null) {
                Map<String, CustomMapDecoration> decorations = ed.getCustomDecorations();
                decorations.clear();
                for (int i = 0; i < serverDeco.length; ++i) {
                    CustomMapDecoration customDecoration = serverDeco[i];
                    if (customDecoration != null) decorations.put("icon-" + i, customDecoration);
                    else {
                        Moonlight.LOGGER.warn("Failed to load custom map decoration, skipping");
                    }
                }
            }
            if(serverData != null) {
                Map<ResourceLocation, CustomMapData> customData = ed.getCustomData();
                customData.clear();
                for (CustomMapData instance : serverData) {
                    if (instance != null) customData.put(instance.getType().id(), instance);
                    else {
                        Moonlight.LOGGER.warn("Failed to load custom map data, skipping");
                    }
                }
            }

            mapRenderer.update(mapId, mapData);
        }
    }
}

package net.mehvahdjukaar.moonlight.api.integration.forge;

import com.mojang.datafixers.util.Pair;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import org.jetbrains.annotations.Nullable;
import twilightforest.TFMazeMapData;
import twilightforest.item.MazeMapItem;

public class TwilightForestCompatImpl {
    public static void syncTfYLevel(MapItemSavedData mapData, Pair<Boolean, Integer> data) {
        if(mapData instanceof TFMazeMapData m && data != null){
            m.yCenter = data.getSecond();
        }
    }

    @Nullable
    public static Pair<Boolean, Integer> getMapData(MapItemSavedData data) {
        if(data instanceof TFMazeMapData m){
            return Pair.of(false, m.yCenter);
        }
        return null;
    }
}

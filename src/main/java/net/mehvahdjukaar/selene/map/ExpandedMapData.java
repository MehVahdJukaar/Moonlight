package net.mehvahdjukaar.selene.map;


import net.mehvahdjukaar.selene.map.markers.MapBlockMarker;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;

import java.util.Map;

public interface ExpandedMapData {
    Map<String, CustomDecoration> getCustomDecorations();
    Map<String, MapBlockMarker<?>> getCustomMarkers();
    Map<String, CustomDataHolder.Instance<?>> getCustomData();

    void toggleCustomDecoration(LevelAccessor world, BlockPos pos);

    void resetCustomDecoration();

    int getVanillaDecorationSize();

    <D extends CustomDecoration> void addCustomDecoration(MapBlockMarker<D> marker);


    MapItemSavedData copy();


}

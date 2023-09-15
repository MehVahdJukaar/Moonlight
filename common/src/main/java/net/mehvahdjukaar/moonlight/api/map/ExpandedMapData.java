package net.mehvahdjukaar.moonlight.api.map;


import net.mehvahdjukaar.moonlight.api.map.markers.MapBlockMarker;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;

import java.util.Map;

public interface ExpandedMapData {
    Map<String, CustomMapDecoration> getCustomDecorations();
    Map<String, MapBlockMarker<?>> getCustomMarkers();
    Map<ResourceLocation, CustomMapData> getCustomData();

    boolean toggleCustomDecoration(LevelAccessor world, BlockPos pos);

    void resetCustomDecoration();

    int getVanillaDecorationSize();

    <D extends CustomMapDecoration> void addCustomDecoration(MapBlockMarker<D> marker);


    MapItemSavedData copy();

    void setCustomDecorationsDirty();
    void setCustomDataDirty();
}

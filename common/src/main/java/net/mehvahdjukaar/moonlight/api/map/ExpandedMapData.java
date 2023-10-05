package net.mehvahdjukaar.moonlight.api.map;


import net.mehvahdjukaar.moonlight.api.map.markers.MapBlockMarker;
import net.mehvahdjukaar.moonlight.api.map.type.MapDecorationType;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import org.jetbrains.annotations.ApiStatus;

import java.util.Map;

public interface ExpandedMapData {

    @ApiStatus.Internal
    Map<String, CustomMapDecoration> getCustomDecorations();

    @ApiStatus.Internal
    Map<String, MapBlockMarker<?>> getCustomMarkers();

    Map<ResourceLocation, CustomMapData> getCustomData();

    boolean toggleCustomDecoration(LevelAccessor world, BlockPos pos);

    void resetCustomDecoration();

    int getVanillaDecorationSize();

    <M extends MapBlockMarker<?>> void addCustomMarker(M fromMarker);

    boolean removeCustomMarker(String id);

    MapItemSavedData copy();

    void setCustomDecorationsDirty();

    void setCustomDataDirty();
}

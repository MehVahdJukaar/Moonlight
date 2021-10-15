package net.mehvahdjukaar.selene.map;


import net.mehvahdjukaar.selene.map.markers.MapWorldMarker;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelAccessor;

import java.util.Map;

public interface CustomDecorationHolder {
    Map<String, CustomDecoration> getCustomDecorations();
    Map<String, MapWorldMarker<?>> getCustomMarkers();

    void toggleCustomDecoration(LevelAccessor world, BlockPos pos);

    void resetCustomDecoration();
}

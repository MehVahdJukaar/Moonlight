package net.mehvahdjukaar.selene.map;

import net.mehvahdjukaar.selene.map.markers.MapWorldMarker;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;

import java.util.Map;

public interface CustomDecorationHolder {
    Map<String, CustomDecoration> getCustomDecorations();
    Map<String, MapWorldMarker<?>> getCustomMarkers();

    void toggleCustomDecoration(IWorld world, BlockPos pos);

    void resetCustomDecoration();
}

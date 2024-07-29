package net.mehvahdjukaar.moonlight.api.map;


import net.mehvahdjukaar.moonlight.api.map.decoration.MLMapMarker;
import net.mehvahdjukaar.moonlight.api.map.decoration.MLMapDecoration;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import org.jetbrains.annotations.ApiStatus;

import java.util.Map;
import java.util.function.Consumer;

public interface ExpandedMapData {

    @ApiStatus.Internal
    Map<String, MLMapDecoration> ml$getCustomDecorations();

    @ApiStatus.Internal
    Map<String, MLMapMarker<?>> ml$getCustomMarkers();

    @ApiStatus.Internal
    Map<ResourceLocation, CustomMapData<?>> ml$getCustomData();

    boolean ml$toggleCustomDecoration(LevelAccessor world, BlockPos pos);

    void ml$resetCustomDecoration();

    int ml$getVanillaDecorationSize();

    <M extends MLMapMarker<?>> void ml$addCustomMarker(M fromMarker);

    boolean ml$removeCustomMarker(String id);

    MapItemSavedData ml$copy();

    void ml$setCustomDecorationsDirty();

    <H extends CustomMapData.DirtyCounter> void ml$setCustomDataDirty(CustomMapData.Type<?> type, Consumer<H> dirtySetter);
}

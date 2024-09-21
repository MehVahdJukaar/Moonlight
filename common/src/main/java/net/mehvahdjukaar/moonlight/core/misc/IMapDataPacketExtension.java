package net.mehvahdjukaar.moonlight.core.misc;

import net.mehvahdjukaar.moonlight.api.map.CustomMapData;
import net.mehvahdjukaar.moonlight.api.map.decoration.MLMapDecoration;
import net.minecraft.resources.ResourceLocation;

import java.util.List;
import java.util.Optional;

public interface IMapDataPacketExtension {

    Optional<List<CustomMapData.DirtyDataPatch<?,?>>> moonlight$getDirtyCustomData();

    Optional<List<MLMapDecoration>> moonlight$getCustomDecorations();

    ResourceLocation moonlight$getDimension();

    int moonlight$getMapCenterX();

    int moonlight$getMapCenterZ();

    void moonlight$setDimension(ResourceLocation dim);

    void moonlight$setMapCenter(int x, int z);

    void moonlight$setCustomDecorations(Optional<List<MLMapDecoration>> deco);

    void moonlight$setDirtyCustomData(Optional<List<CustomMapData.DirtyDataPatch<?, ?>>> tag);
}

package net.mehvahdjukaar.moonlight.core.misc;

import net.mehvahdjukaar.moonlight.api.map.CustomMapData;
import net.mehvahdjukaar.moonlight.api.map.CustomMapDecoration;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import org.joml.Vector2i;

import java.util.Collection;

public interface IMapDataPacketExtension {

    void moonlight$sendCustomDecorations(Collection<CustomMapDecoration> decorations);

    CustomMapDecoration[] moonlight$getCustomDecorations();

    void moonlight$sendCustomMapData(Collection<CustomMapData> data);

    CustomMapData[] moonlight$getCustomMapData();

    Vector2i moonlight$getMapCenter();

    MapItemSavedData.MapPatch moonlight$getColorPatch();

    ResourceKey<Level> moonlight$getDimension();
}

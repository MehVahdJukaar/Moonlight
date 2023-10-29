package net.mehvahdjukaar.moonlight.core.misc;

import net.mehvahdjukaar.moonlight.api.map.CustomMapDecoration;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;

import java.util.Collection;

public interface IMapDataPacketExtension {

    void moonlight$sendCustomDecorations(Collection<CustomMapDecoration> decorations);

    void moonlight$sendCustomMapDataTag(CompoundTag dataUpdateTag);

    CompoundTag moonlight$getCustomMapDataTag();

    MapItemSavedData.MapPatch moonlight$getColorPatch();

    ResourceKey<Level> moonlight$getDimension();
}

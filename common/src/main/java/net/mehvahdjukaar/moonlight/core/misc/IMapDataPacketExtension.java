package net.mehvahdjukaar.moonlight.core.misc;

import net.mehvahdjukaar.moonlight.api.map.decoration.MLMapDecoration;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface IMapDataPacketExtension {

    Optional<CompoundTag> moonlight$getCustomMapDataTag();

    Optional<List<MLMapDecoration>> moonlight$getCustomDecorations();

    ResourceLocation moonlight$getDimension();

    int moonlight$getMapCenterX();

    int moonlight$getMapCenterZ();

    void moonlight$setDimension(ResourceLocation dim);

    void moonlight$setMapCenter(int x, int z);

    void moonlight$setCustomDecorations(Optional<List<MLMapDecoration>> deco);

    void moonlight$setCustomMapDataTag(Optional<CompoundTag> tag);
}

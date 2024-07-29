package net.mehvahdjukaar.moonlight.core.misc;

import net.mehvahdjukaar.moonlight.api.map.decoration.MLMapDecoration;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface IMapDataPacketExtension {

    Optional<CompoundTag> moonlight$getCustomMapDataTag();

    Optional<List<MLMapDecoration>> moonlight$getCustomDecorations();

    ResourceKey<Level> moonlight$getDimension();

    void moonlight$setCustomDecorations(Optional<List<MLMapDecoration>> deco);

    void moonlight$setCustomMapDataTag(Optional<CompoundTag> tag);
}

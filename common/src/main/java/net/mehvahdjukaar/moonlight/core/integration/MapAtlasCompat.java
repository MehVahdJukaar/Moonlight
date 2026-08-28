package net.mehvahdjukaar.moonlight.core.integration;


import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import org.jetbrains.annotations.Nullable;
import pepjebs.mapatlases.MapAtlasesMod;
import pepjebs.mapatlases.item.MapAtlasItem;
import pepjebs.mapatlases.map_collection.MapCollection;
import pepjebs.mapatlases.map_collection.MapGridKey;
import pepjebs.mapatlases.utils.MapDataHolder;
import pepjebs.mapatlases.utils.Slice;

public class MapAtlasCompat {

    @Nullable
    public static MapItemSavedData getSavedDataFromAtlas(ItemStack atlas, Level level, Player player) {
        //TODO: use MapAccessUtils.getSavedDataAt
        if (atlas.getItem() == MapAtlasesMod.MAP_ATLAS.get()) {
            MapCollection maps = MapAtlasItem.getMaps(atlas, level);
            if (maps != null) {
                Slice slice = MapAtlasItem.getSelectedSlice(atlas, level.dimension());
                MapGridKey key = MapGridKey.at(maps.getScale(), slice, player.getX(), player.getZ());
                MapDataHolder select = maps.select(key);
                if (select != null) {
                    return select.data;
                }
            }
        }
        return null;
    }
}

package net.mehvahdjukaar.selene.map.mapatlas;

import lilypuree.mapatlases.item.MapAtlasItem;
import lilypuree.mapatlases.util.MapAtlasesAccessUtils;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public class MapAtlasPlugin {

    public static boolean isAtlas(Item item) {
        return item instanceof MapAtlasItem;
    }

    @Nullable
    public static MapItemSavedData getSavedDataFromAtlas(ItemStack item, Level level, Player player) {
        return MapAtlasesAccessUtils.getActiveAtlasMapState(level, item, player.getName().getString()).getValue();
    }

    @Nullable
    public static Integer getMapIdFromAtlas(ItemStack item, Level level, Object data) {
        Map<String, MapItemSavedData> mapInfos = MapAtlasesAccessUtils.getAllMapInfoFromAtlas(level, item);
        for (var e : mapInfos.entrySet()) {
            if (e.getValue() == data) {
                return MapAtlasesAccessUtils.getMapIntFromString(e.getKey());
            }
        }
        return null;
    }

}
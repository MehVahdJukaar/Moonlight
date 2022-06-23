package net.mehvahdjukaar.moonlight.builtincompat;


import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

/*
import lilypuree.mapatlases.item.MapAtlasItem;
import lilypuree.mapatlases.util.MapAtlasesAccessUtils;
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

        Map<String, MapItemSavedData> mapInfo = MapAtlasesAccessUtils.getAllMapInfoFromAtlas(level, item);
        for (var e : mapInfo.entrySet()) {
            if (e.getValue() == data) {
                return MapAtlasesAccessUtils.getMapIntFromString(e.getKey());
            }
        }
        return null;
    }

}
*/

public class MapAtlasPlugin {

    public static MapItemSavedData getSavedDataFromAtlas(ItemStack stack, Level level, Player player) {
        return null;
    }

    public static Integer getMapIdFromAtlas(ItemStack stack, Level level, Object data) {
        return 1;
    }

    public static boolean isAtlas(Item item) {
        return false;
    }
}

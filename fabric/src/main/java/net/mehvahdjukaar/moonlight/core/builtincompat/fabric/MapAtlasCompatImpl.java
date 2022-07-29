package net.mehvahdjukaar.moonlight.core.builtincompat.fabric;


import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import org.jetbrains.annotations.Nullable;
import pepjebs.mapatlases.item.MapAtlasItem;
import pepjebs.mapatlases.utils.MapAtlasesAccessUtils;

import java.util.Map;


public class MapAtlasCompatImpl {

    public static boolean isAtlas(Item item) {
        return item instanceof MapAtlasItem;
    }

    @Nullable
    public static MapItemSavedData getSavedDataFromAtlas(ItemStack item, Level level, Player player) {
        if (player instanceof ServerPlayer serverPlayer) {
            var data = MapAtlasesAccessUtils.getActiveAtlasMapStateServer(level, item, serverPlayer);
            if (data == null) return null;
            return data.getValue();
        }
        return null;
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

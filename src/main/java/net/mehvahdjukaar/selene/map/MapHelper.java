package net.mehvahdjukaar.selene.map;

import net.mehvahdjukaar.selene.map.mapatlas.MapAtlasPlugin;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.MapItem;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import net.minecraftforge.fml.ModList;
import org.jetbrains.annotations.Nullable;

public class MapHelper {
    public static final boolean mapAtlas = ModList.get().isLoaded("map_atlases");

    @Nullable
    public static MapItemSavedData getMapData(ItemStack stack, Player player) {
        MapItemSavedData data = null;
        if (stack.getItem() instanceof MapItem) data = MapItem.getSavedData(stack, player.level);
        else if (mapAtlas) data = MapAtlasPlugin.getSavedDataFromAtlas(stack, player.level, player);
        return data;
    }

    public static Integer getMapId(ItemStack stack, Player player, Object data) {
        Integer i = MapItem.getMapId(stack);
        if (i == null && mapAtlas) i = MapAtlasPlugin.getMapIdFromAtlas(stack, player.level, data);
        return i;
    }


}

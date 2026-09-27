package net.mehvahdjukaar.moonlight.core.integration;


import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import org.jetbrains.annotations.Nullable;
import pepjebs.mapatlases.api.MapAtlasesApi;

public class MapAtlasCompat {

    @Nullable
    public static MapItemSavedData getSavedDataFromAtlas(ItemStack atlas, Level level, Player player) {
        return MapAtlasesApi.getMapDataAt(atlas, level, player.getX(), player.getZ());
    }
}

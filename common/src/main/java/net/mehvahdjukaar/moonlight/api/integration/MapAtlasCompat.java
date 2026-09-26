package net.mehvahdjukaar.moonlight.api.integration;


import com.mojang.blaze3d.vertex.PoseStack;
import net.mehvahdjukaar.candlelight.api.ClientOnly;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.maps.MapId;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import org.jetbrains.annotations.Nullable;
import pepjebs.mapatlases.api.MapAtlasesApi;
import pepjebs.mapatlases.api.MapAtlasesClientApi;

@Deprecated(forRemoval = true) //Make your own one!
public class MapAtlasCompat {

    public static boolean isAtlas(Item item) {
        return MapAtlasesApi.isAtlas(item);
    }

    @Nullable
    public static MapItemSavedData getSavedDataFromAtlas(ItemStack atlas, Level level, Player player) {
        return net.mehvahdjukaar.moonlight.core.integration.MapAtlasCompat.getSavedDataFromAtlas(atlas, level, player);
    }

    @Nullable
    public static Integer getMapIdFromAtlas(ItemStack atlas, Level level, Object data) {
        if (!(data instanceof MapItemSavedData mapData)) return null;
        MapId id = MapAtlasesApi.getMapId(atlas, level, mapData);
        return id == null ? null : id.id();
    }

    @ClientOnly
    public static void scaleDecoration(PoseStack poseStack) {
        MapAtlasesClientApi.scaleDecoration(poseStack);
    }

    @ClientOnly
    public static void scaleDecorationText(PoseStack poseStack, float textWidth, float textScale) {
        MapAtlasesClientApi.scaleDecorationText(poseStack, textWidth, textScale);
    }
}

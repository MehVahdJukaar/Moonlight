package net.mehvahdjukaar.moonlight.api.integration.platform;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.maps.MapId;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import org.jetbrains.annotations.Nullable;
import pepjebs.mapatlases.api.MapAtlasesApi;
import pepjebs.mapatlases.api.MapAtlasesClientApi;

public class MapAtlasCompatImpl {
    public static boolean isAtlas(Item item) {
        return MapAtlasesApi.isAtlas(item);
    }

    @Nullable
    public static MapItemSavedData getSavedDataFromAtlas(ItemStack atlas, Level level, Player player) {
        return MapAtlasesApi.getMapDataAt(atlas, level, player.getX(), player.getZ());
    }

    @Nullable
    public static Integer getMapIdFromAtlas(ItemStack atlas, Level level, Object data) {
        if (!(data instanceof MapItemSavedData mapData)) return null;
        MapId id = MapAtlasesApi.getMapId(atlas, level, mapData);
        return id == null ? null : id.id();
    }

    public static void scaleDecoration(PoseStack poseStack) {
        MapAtlasesClientApi.scaleDecoration(poseStack);
    }

    public static void scaleDecorationText(PoseStack poseStack, float textWidth, float textScale) {
        MapAtlasesClientApi.scaleDecorationText(poseStack, textWidth, textScale);
    }
}

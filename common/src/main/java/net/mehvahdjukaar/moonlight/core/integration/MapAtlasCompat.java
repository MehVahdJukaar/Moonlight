package net.mehvahdjukaar.moonlight.core.integration;


import com.mojang.blaze3d.vertex.PoseStack;
import net.mehvahdjukaar.candlelight.api.ClientOnly;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import org.jetbrains.annotations.Nullable;
import pepjebs.mapatlases.api.MapAtlasesApi;
import pepjebs.mapatlases.api.MapAtlasesClientApi;

public class MapAtlasCompat {

    @Nullable
    public static MapItemSavedData getSavedDataFromAtlas(ItemStack atlas, Level level, Player player) {
        return MapAtlasesApi.getMapDataAt(atlas, level, player.getX(), player.getZ());
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

package net.mehvahdjukaar.moonlight.api.integration;


import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import org.jetbrains.annotations.Nullable;
import pepjebs.mapatlases.MapAtlasesMod;
import pepjebs.mapatlases.client.MapAtlasesClient;

@Deprecated(forRemoval = true) //Make your own one!
public class MapAtlasCompat {

    public static boolean isAtlas(Item item) {
        return item == MapAtlasesMod.MAP_ATLAS.get();
    }

    @Nullable
    public static MapItemSavedData getSavedDataFromAtlas(ItemStack atlas, Level level, Player player) {
        return net.mehvahdjukaar.moonlight.core.integration.MapAtlasCompat.getSavedDataFromAtlas(atlas, level, player);
    }

    @Nullable
    public static Integer getMapIdFromAtlas(ItemStack atlas, Level level, Object data) {
        return null;
    }

    @Environment(EnvType.CLIENT)
    public static void scaleDecoration(PoseStack poseStack) {
        MapAtlasesClient.modifyDecorationTransform(poseStack);
    }

    @Environment(EnvType.CLIENT)
    public static void scaleDecorationText(PoseStack poseStack, float textWidth, float textScale) {
        MapAtlasesClient.modifyTextDecorationTransform(poseStack, textWidth, textScale);
    }
}

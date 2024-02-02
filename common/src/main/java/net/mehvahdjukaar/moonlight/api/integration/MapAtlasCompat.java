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
import pepjebs.mapatlases.item.MapAtlasItem;
import pepjebs.mapatlases.map_collection.IMapCollection;
import pepjebs.mapatlases.map_collection.MapKey;


public class MapAtlasCompat {

    public static boolean isAtlas(Item item) {
        return item == MapAtlasesMod.MAP_ATLAS.get();
    }

    @Nullable
    public static MapItemSavedData getSavedDataFromAtlas(ItemStack atlas, Level level, Player player) {
        if (isAtlas(atlas.getItem())) {
            IMapCollection maps = MapAtlasItem.getMaps(atlas, level);
            if (maps != null) {
                var slice = MapAtlasItem.getSelectedSlice(atlas, level.dimension());
                var key = MapKey.at(maps.getScale(), player, slice);
                var select = maps.select(key);
                if (select != null) {
                    return select.data;
                }
            }
        }
        return null;
    }

    @Nullable
    public static Integer getMapIdFromAtlas(ItemStack atlas, Level level, Object data) {
        try {
            IMapCollection c = null;
            IMapCollection maps = MapAtlasItem.getMaps(atlas, level);
            if (maps != null) {
                for (var e : maps.getAll()) {
                    if (e.data == data) {
                        return e.id;
                    }
                }
            }
        } catch (Exception ignored) {
        }
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

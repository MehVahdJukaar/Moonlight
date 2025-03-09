package net.mehvahdjukaar.moonlight.core.integration;


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
import pepjebs.mapatlases.map_collection.MapCollection;
import pepjebs.mapatlases.map_collection.MapSearchKey;
import pepjebs.mapatlases.utils.MapDataHolder;
import pepjebs.mapatlases.utils.Slice;

public class MapAtlasCompat {

    public static boolean isAtlas(Item item) {
        return item == MapAtlasesMod.MAP_ATLAS.get();
    }

    @Nullable
    public static MapItemSavedData getSavedDataFromAtlas(ItemStack atlas, Level level, Player player) {
        if(isAtlas(atlas.getItem())) {
            MapCollection maps = MapAtlasItem.getMaps(atlas, level);
            if (maps != null) {
                Slice slice = MapAtlasItem.getSelectedSlice(atlas, level.dimension());
                MapSearchKey key = MapSearchKey.at(maps.getScale(), player, slice);
                MapDataHolder select = maps.select(key);
                if (select != null) {
                    return select.data;
                }
            }
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

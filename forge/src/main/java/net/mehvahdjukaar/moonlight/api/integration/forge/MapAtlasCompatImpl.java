package net.mehvahdjukaar.moonlight.api.integration.forge;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.datafixers.util.Pair;
import net.mehvahdjukaar.moonlight.api.map.MapHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import org.jetbrains.annotations.Nullable;
import pepjebs.mapatlases.client.MapAtlasesClient;
import pepjebs.mapatlases.item.MapAtlasItem;

public class MapAtlasCompatImpl {
    public static boolean isAtlas(Item item) {
        return item instanceof MapAtlasItem;
    }

    @Nullable
    public static MapItemSavedData getSavedDataFromAtlas(ItemStack atlas, Level level, Player player) {
        //TODO: re add
        /*
        if(atlas.is(MapAtlasesMod.MAP_ATLAS.get())) {
            var maps = MapAtlasItem.getMaps(atlas, level);
            if (maps != null) {
                var slice = MapAtlasItem.getSelectedSlice(atlas, level.dimension());
                var key = MapKey.at(maps.getScale(), player, slice);
                var select = maps.select(key);
                if (select != null) {
                    return select.data;
                }
            }
        }*/
        return null;
    }

    @Nullable
    public static Integer getMapIdFromAtlas(ItemStack atlas, Level level, Object data) {
        /* //TODO
        try {
            var maps = MapAtlasItem.getMaps(atlas, level);
            if (maps != null) {
                for (var e : maps.getAll()) {
                    if (e.data == data) {
                        return e.id;
                    }
                }
            }
        } catch (Exception ignored) {
        }*/
        return null;
    }

    public static void scaleDecoration(PoseStack poseStack) {
        MapAtlasesClient.modifyDecorationTransform(poseStack);
    }

    public static void scaleDecorationText(PoseStack poseStack, float textWidth, float textScale) {
        MapAtlasesClient.modifyTextDecorationTransform(poseStack, textWidth, textScale);
    }
}

package net.mehvahdjukaar.moonlight.api.integration.forge;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.datafixers.util.Pair;
import net.mehvahdjukaar.moonlight.api.map.MapHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.Nullable;
import pepjebs.mapatlases.MapAtlasesMod;
import pepjebs.mapatlases.capabilities.MapKey;
import pepjebs.mapatlases.client.MapAtlasesClient;
import pepjebs.mapatlases.item.MapAtlasItem;
import pepjebs.mapatlases.utils.MapAtlasesAccessUtils;

public class MapAtlasCompatImpl {
    public static boolean isAtlas(Item item) {
        return item instanceof MapAtlasItem;
    }

    @Nullable
    public static MapItemSavedData getSavedDataFromAtlas(ItemStack atlas, Level level, Player player) {
        if(atlas.is(MapAtlasesMod.MAP_ATLAS.get())) {
            var maps = MapAtlasItem.getMaps(atlas, level);
            if (maps != null) {
                var slice = MapAtlasItem.getSelectedSlice(atlas, level.dimension());
                var key = MapKey.at(maps.getScale(), player, slice);
                Pair<String, MapItemSavedData> select = maps.select(key);
                if (select != null) {
                    return select.getSecond();
                }
            }
        }
        return null;
    }

    @Nullable
    public static Integer getMapIdFromAtlas(ItemStack atlas, Level level, Object data) {
        try {
            var maps = MapAtlasItem.getMaps(atlas, level);
            if (maps != null) {
                for (var e : maps.getAll()) {
                    if (e.getSecond() == data) {
                        return MapAtlasesAccessUtils.getMapIntFromString(e.getFirst());
                    }
                }
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    @OnlyIn(Dist.CLIENT)
    public static void scaleDecoration(PoseStack poseStack) {
        MapAtlasesClient.modifyDecorationTransform(poseStack);
    }
}

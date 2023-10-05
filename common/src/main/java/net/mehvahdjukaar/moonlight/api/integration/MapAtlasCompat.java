package net.mehvahdjukaar.moonlight.api.integration;


import com.mojang.blaze3d.vertex.PoseStack;
import dev.architectury.injectables.annotations.ExpectPlatform;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public class MapAtlasCompat {

    @ExpectPlatform
    public static boolean isAtlas(Item item) {
        throw new AssertionError();
    }

    @ExpectPlatform
    @Nullable
    public static MapItemSavedData getSavedDataFromAtlas(ItemStack item, Level level, Player player) {
     throw new AssertionError();
    }

    @ExpectPlatform
    @Nullable
    public static Integer getMapIdFromAtlas(ItemStack item, Level level, Object data) {
        throw new AssertionError();
    }

    @ExpectPlatform
    @Environment(EnvType.CLIENT)
    public static void scaleDecoration(PoseStack poseStack) {
        throw new AssertionError();
    }


    @ExpectPlatform
    @Environment(EnvType.CLIENT)
    public static void scaleDecorationText(PoseStack poseStack, float textWidth, float textScale) {
        throw new AssertionError();
    }
}

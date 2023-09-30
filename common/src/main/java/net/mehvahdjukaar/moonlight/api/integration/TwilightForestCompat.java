package net.mehvahdjukaar.moonlight.api.integration;

import com.mojang.datafixers.util.Pair;
import dev.architectury.injectables.annotations.ExpectPlatform;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import org.jetbrains.annotations.Nullable;

public class TwilightForestCompat {
    @ExpectPlatform
    public static void syncTfYLevel(MapItemSavedData mapData, Pair<Boolean, Integer> moonlight$tfData) {
        throw new AssertionError();
    }

    @ExpectPlatform
    @Nullable
    public static Pair<Boolean, Integer> getMapData(MapItemSavedData data) {
        throw new AssertionError();
    }
}

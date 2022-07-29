package net.mehvahdjukaar.moonlight.core.builtincompat;


import dev.architectury.injectables.annotations.ExpectPlatform;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;

public class MapAtlasCompat {

    @ExpectPlatform
    public static MapItemSavedData getSavedDataFromAtlas(ItemStack stack, Level level, Player player) {
        throw new AssertionError();
    }
    @ExpectPlatform
    public static Integer getMapIdFromAtlas(ItemStack stack, Level level, Object data) {
        throw new AssertionError();
    }
    @ExpectPlatform
    public static boolean isAtlas(Item item) {
        throw new AssertionError();
    }
}

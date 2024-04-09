package net.mehvahdjukaar.moonlight.api.util;

import net.mehvahdjukaar.moonlight.api.platform.RegHelper;
import net.minecraft.world.level.ItemLike;

// move to RegHelper?
@Deprecated(forRemoval = true)
public class AnimalFoodHelper {

    @Deprecated(forRemoval = true)
    public static void addChickenFood(ItemLike... food) {
        RegHelper.registerChickenFood(food);
    }

    @Deprecated(forRemoval = true)
    public static void addHorseFood(ItemLike... food) {
        RegHelper.registerHorseFood(food);
    }

    @Deprecated(forRemoval = true)
    public static void addParrotFood(ItemLike... food) {
        RegHelper.registerParrotFood(food);
    }

}

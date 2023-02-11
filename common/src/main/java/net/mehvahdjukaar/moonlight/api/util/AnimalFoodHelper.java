package net.mehvahdjukaar.moonlight.api.util;

import net.minecraft.world.entity.animal.Chicken;
import net.minecraft.world.entity.animal.Parrot;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AnimalFoodHelper {

    public static void addChickenFood(ItemLike... food) {
        List<ItemStack> chickenFood = new ArrayList<>(List.of(Chicken.FOOD_ITEMS.getItems()));
        Arrays.stream(food).forEach(f -> chickenFood.add(f.asItem().getDefaultInstance()));
        Chicken.FOOD_ITEMS = Ingredient.of(chickenFood.stream());
    }

    public static void addHorseFood(ItemLike... food) {
        List<ItemStack> horseFood = new ArrayList<>(List.of(AbstractHorse.FOOD_ITEMS.getItems()));
        Arrays.stream(food).forEach(f -> horseFood.add(f.asItem().getDefaultInstance()));
        AbstractHorse.FOOD_ITEMS = Ingredient.of(horseFood.stream());
    }

    public static void addParrotFood(ItemLike... food) {
        Arrays.stream(food).forEach(f -> Parrot.TAME_FOOD.add(f.asItem()));
    }

}

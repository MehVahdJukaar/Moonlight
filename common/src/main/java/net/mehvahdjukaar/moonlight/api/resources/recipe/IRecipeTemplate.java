package net.mehvahdjukaar.moonlight.api.resources.recipe;

import net.mehvahdjukaar.moonlight.api.set.BlockType;
import net.minecraft.data.recipes.CraftingRecipeBuilder;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.Ingredient;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface IRecipeTemplate<R extends FinishedRecipe> {

    <T extends BlockType> R createSimilar(T originalMat, T destinationMat, Item unlockItem, @Nullable String id);

    //null if it fails to convert at least 1 ingredient
    @Nullable
    default <T extends BlockType> R createSimilar(@NotNull T originalMat, @NotNull T destinationMat, Item unlockItem) {
        return createSimilar(originalMat, destinationMat, unlockItem, null);
    }

    //cast these to ICondition. Forge Only
    void addCondition(Object condition);

    List<Object> getConditions();

    static <T extends BlockType> Ingredient convertIngredients(@NotNull T originalMat, @NotNull T destinationMat, @NotNull Ingredient ing) {
        return BlockTypeSwapIngredient.create(ing, originalMat, destinationMat);
    }

    default RecipeCategory determineBookCategory(CraftingBookCategory recipeCategory) {
        for (var v : RecipeCategory.values()) {
            if (recipeCategory == CraftingRecipeBuilder.determineBookCategory(v)) return v;
        }
        return RecipeCategory.MISC;
    }


}

package net.mehvahdjukaar.moonlight.api.resources.recipe;

import net.mehvahdjukaar.moonlight.api.set.BlockType;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;

import org.jetbrains.annotations.Nullable;
import java.util.List;

public interface IRecipeTemplate<R extends FinishedRecipe> {

    <T extends BlockType> R createSimilar(T originalMat, T destinationMat, Item unlockItem, @Nullable String id);

    //null if it fails to convert at least 1 ingredient
    @Nullable
    default <T extends BlockType> R createSimilar(T originalMat, T destinationMat, Item unlockItem) {
        return createSimilar(originalMat, destinationMat, unlockItem, null);
    }

    //cast these to ICondition. Forge Only
    void addCondition(Object condition);

    List<Object> getConditions();

    @Nullable
     static <T extends BlockType> Ingredient convertIngredients(T originalMat, T destinationMat, Ingredient ing) {
        for (var in : ing.getItems()) {
            Item it = in.getItem();
            if (it != Items.BARRIER) {
                ItemLike i = BlockType.changeItemType(it, originalMat, destinationMat);
                if (i != null) {
                    //converts first ingredient it finds
                    return Ingredient.of(i);
                }
            }
        }
        return null;
    }

}

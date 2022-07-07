package net.mehvahdjukaar.moonlight.api.resources.recipe;

import net.mehvahdjukaar.moonlight.api.set.BlockType;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.world.item.Item;

import javax.annotation.Nullable;
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

}

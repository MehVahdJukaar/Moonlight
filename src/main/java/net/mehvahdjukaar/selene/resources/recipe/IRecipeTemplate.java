package net.mehvahdjukaar.selene.resources.recipe;

import net.mehvahdjukaar.selene.block_set.BlockType;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.world.item.Item;
import net.minecraftforge.common.crafting.conditions.ICondition;

import javax.annotation.Nullable;
import java.util.List;

public interface IRecipeTemplate<R extends FinishedRecipe> {

    <T extends BlockType> R createSimilar(T originalMat, T destinationMat, Item unlockItem, @Nullable String id);

    //null if it fails to convert at least 1 ingredient
    @Nullable
    default <T extends BlockType> R createSimilar(T originalMat, T destinationMat, Item unlockItem) {
        return createSimilar(originalMat, destinationMat, unlockItem, null);
    }

    void addCondition(ICondition condition);

    List<ICondition> getConditions();

}

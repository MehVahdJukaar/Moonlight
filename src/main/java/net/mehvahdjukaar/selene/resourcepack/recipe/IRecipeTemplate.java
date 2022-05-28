package net.mehvahdjukaar.selene.resourcepack.recipe;

import net.mehvahdjukaar.selene.block_set.IBlockType;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.world.item.Item;

public interface IRecipeTemplate<R extends FinishedRecipe> {

    <T extends IBlockType> R createSimilar(T originalMat, T destinationMat, Item unlockItem);

}

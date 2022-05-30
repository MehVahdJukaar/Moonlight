package net.mehvahdjukaar.selene.resourcepack.recipe;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.mehvahdjukaar.selene.block_set.BlockType;
import net.minecraft.advancements.critereon.InventoryChangeTrigger;
import net.minecraft.core.Registry;
import net.minecraft.data.recipes.SingleItemRecipeBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.concurrent.atomic.AtomicReference;

public class StoneCutterRecipeTemplate implements IRecipeTemplate<SingleItemRecipeBuilder.Result> {
    public final Item result;
    public final int count;
    public final String group;
    public final Ingredient ingredient;

    private StoneCutterRecipeTemplate(Item pResult, int pCount, String pGroup, Ingredient ingredient) {
        this.result = pResult;
        this.count = pCount;
        this.group = pGroup;
        this.ingredient = ingredient;
    }

    public static StoneCutterRecipeTemplate fromJson(JsonObject json) {
        JsonElement result = json.get("result");
        ResourceLocation item = new ResourceLocation(result.getAsString());
        int count = 1;
        var c = json.get("count");
        if (c != null) count = c.getAsInt();

        Item i = Registry.ITEM.get(item);

        var g = json.get("group");
        String group = g == null ? "" : g.getAsString();

        Ingredient ingredient = Ingredient.fromJson(json.get("ingredient"));

        return new StoneCutterRecipeTemplate(i, count, group, ingredient);
    }

    public <T extends BlockType> SingleItemRecipeBuilder.Result createSimilar(
            T originalMat, T destinationMat, Item unlockItem, String id) {
        Item newRes = BlockType.changeItemBlockType(this.result, originalMat, destinationMat);
        if (newRes == this.result) throw new UnsupportedOperationException(String.format("Could not convert output item %s",newRes));

        Ingredient ing = ingredient;
        if (ingredient.getItems().length > 0) {
            Item old = ingredient.getItems()[0].getItem();
            if (old != Items.BARRIER) {
                Item i = BlockType.changeItemBlockType(old, originalMat, destinationMat);
                ing = Ingredient.of(i);
            }
        }

        SingleItemRecipeBuilder builder = SingleItemRecipeBuilder.stonecutting(ing, newRes);
        builder.group(group);

        builder.unlockedBy("has_planks", InventoryChangeTrigger.TriggerInstance.hasItems(unlockItem));

        AtomicReference<SingleItemRecipeBuilder.Result> newRecipe = new AtomicReference<>();

        if(id == null) {
            builder.save(r -> newRecipe.set((SingleItemRecipeBuilder.Result) r));
        }else{
            builder.save(r -> newRecipe.set((SingleItemRecipeBuilder.Result) r),id);
        }
        return newRecipe.get();
    }


}

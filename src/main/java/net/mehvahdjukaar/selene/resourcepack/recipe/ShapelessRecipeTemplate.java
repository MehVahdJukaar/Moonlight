package net.mehvahdjukaar.selene.resourcepack.recipe;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.mehvahdjukaar.selene.block_set.BlockType;
import net.minecraft.advancements.critereon.InventoryChangeTrigger;
import net.minecraft.core.Registry;
import net.minecraft.data.recipes.ShapelessRecipeBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class ShapelessRecipeTemplate implements IRecipeTemplate<ShapelessRecipeBuilder.Result> {
    public final Item result;
    public final int count;
    public final String group;
    public final List<Ingredient> ingredients;

    private ShapelessRecipeTemplate(Item pResult, int pCount, String pGroup, List<Ingredient> ingredients) {
        this.result = pResult;
        this.count = pCount;
        this.group = pGroup;
        this.ingredients = ingredients;
    }

    public static ShapelessRecipeTemplate fromJson(JsonObject json) {
        JsonObject result = json.getAsJsonObject("result");
        ResourceLocation item = new ResourceLocation(result.get("item").getAsString());
        int count = 1;
        var c = result.get("count");
        if (c != null) count = c.getAsInt();

        Item i = Registry.ITEM.get(item);

        var g = json.get("group");
        String group = g == null ? "" : g.getAsString();

        List<Ingredient> ingredientsList = new ArrayList<>();
        JsonArray ingredients = json.getAsJsonArray("ingredients");
        ingredients.forEach(p -> ingredientsList.add(Ingredient.fromJson(p)));


        return new ShapelessRecipeTemplate(i, count, group, ingredientsList);
    }

    public <T extends BlockType> ShapelessRecipeBuilder.Result createSimilar(
            T originalMat, T destinationMat, Item unlockItem, String id) {
        ItemLike newRes = BlockType.changeItemBlockType(this.result, originalMat, destinationMat);
        if (newRes == null)
            throw new UnsupportedOperationException(String.format("Could not convert output item %s", this.result));


        ShapelessRecipeBuilder builder = new ShapelessRecipeBuilder(newRes, this.count);

        for (var ing : this.ingredients) {
            if (ing.getItems().length > 0) {
                Item old = ing.getItems()[0].getItem();
                if (old != Items.BARRIER) {
                    ItemLike i = BlockType.changeItemBlockType(old, originalMat, destinationMat);
                    if(i != null) ing = Ingredient.of(i);
                }
            }
            builder.requires(ing);
        }
        builder.group(group);
        builder.unlockedBy("has_planks", InventoryChangeTrigger.TriggerInstance.hasItems(unlockItem));

        AtomicReference<ShapelessRecipeBuilder.Result> newRecipe = new AtomicReference<>();

        if (id == null) {
            builder.save(r -> newRecipe.set((ShapelessRecipeBuilder.Result) r));
        } else {
            builder.save(r -> newRecipe.set((ShapelessRecipeBuilder.Result) r), id);
        }
        return newRecipe.get();
    }

}

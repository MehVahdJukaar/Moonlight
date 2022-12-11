package net.mehvahdjukaar.moonlight.core.recipe;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;
import net.mehvahdjukaar.moonlight.api.platform.PlatformHelper;
import net.mehvahdjukaar.moonlight.api.resources.recipe.IRecipeTemplate;
import net.mehvahdjukaar.moonlight.api.set.BlockType;
import net.mehvahdjukaar.moonlight.core.Moonlight;
import net.minecraft.advancements.critereon.InventoryChangeTrigger;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.recipes.CraftingRecipeBuilder;
import net.minecraft.data.recipes.ShapelessRecipeBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class ShapelessRecipeTemplate implements IRecipeTemplate<ShapelessRecipeBuilder.Result> {

    private final List<Object> conditions = new ArrayList<>();

    public final Item result;
    public final int count;
    public final String group;
    public final List<Ingredient> ingredients;
    public final CraftingBookCategory category;

    ShapelessRecipeTemplate(ShapedRecipeTemplate shaped) {
        this.result = shaped.result;
        this.count = shaped.count;
        this.group = shaped.group;
        this.ingredients = shaped.keys.values().stream().toList();
        this.category = shaped.category;
    }

    public ShapelessRecipeTemplate(JsonObject json) {
        JsonObject result = json.getAsJsonObject("result");
        ResourceLocation item = new ResourceLocation(result.get("item").getAsString());
        int count = 1;
        var c = result.get("count");
        if (c != null) count = c.getAsInt();

        this.result = BuiltInRegistries.ITEM.get(item);
        this.count = count;
        this.category = CraftingBookCategory.CODEC.decode(JsonOps.INSTANCE, json.get("category")).get().orThrow().getFirst();

        var g = json.get("group");
        this.group = g == null ? "" : g.getAsString();

        List<Ingredient> ingredientsList = new ArrayList<>();
        JsonArray ingredients = json.getAsJsonArray("ingredients");
        ingredients.forEach(p -> ingredientsList.add(Ingredient.fromJson(p)));

        this.ingredients = ingredientsList;
    }

    @Override
    public <T extends BlockType> ShapelessRecipeBuilder.Result createSimilar(
            T originalMat, T destinationMat, Item unlockItem, String id) {
        ItemLike newRes = BlockType.changeItemType(this.result, originalMat, destinationMat);
        if (newRes == null) {
            throw new UnsupportedOperationException(String.format("Could not convert output item %s from type %s to %s",
                    this.result, originalMat, destinationMat));
        }
        if (PlatformHelper.getItemTabs(newRes.asItem()).isEmpty()) {
            Moonlight.LOGGER.error("Failed to generate recipe for {} in block type {}: Output item {} cannot have empty creative tab, skipping", this.result, destinationMat, newRes);
            return null;
        }

        ShapelessRecipeBuilder builder = new ShapelessRecipeBuilder(determineBookCategory(this.category),
                newRes, this.count);

        boolean atLeastOneChanged = false;
        for (var ing : this.ingredients) {
            var newIng = IRecipeTemplate.convertIngredients(originalMat, destinationMat, ing);
            if(newIng != null){
                atLeastOneChanged = true;
            }newIng = ing;
            builder.requires(newIng);
        }
        //if recipe fails
        if (!atLeastOneChanged) return null;

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



    @Override
    public void addCondition(Object condition) {
        this.conditions.add(condition);
    }

    @Override
    public List<Object> getConditions() {
        return conditions;
    }
}

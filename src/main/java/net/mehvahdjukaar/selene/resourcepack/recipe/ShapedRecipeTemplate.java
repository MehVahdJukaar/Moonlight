package net.mehvahdjukaar.selene.resourcepack.recipe;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.mehvahdjukaar.selene.block_set.BlockType;
import net.minecraft.advancements.critereon.InventoryChangeTrigger;
import net.minecraft.core.Registry;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

public class ShapedRecipeTemplate implements IRecipeTemplate<ShapedRecipeBuilder.Result> {
    public final Item result;
    public final int count;
    public final String group;
    public final List<String> pattern;
    public final Map<Character, Ingredient> keys;

    private ShapedRecipeTemplate(Item pResult, int pCount, String pGroup, List<String> pPattern, Map<Character, Ingredient> pKey) {
        this.result = pResult;
        this.count = pCount;
        this.group = pGroup;
        this.pattern = pPattern;
        this.keys = pKey;
    }

    public static ShapedRecipeTemplate fromJson(JsonObject json) {
        JsonObject result = json.getAsJsonObject("result");
        ResourceLocation item = new ResourceLocation(result.get("item").getAsString());
        int count = 1;
        var c = result.get("count");
        if (c != null) count = c.getAsInt();

        Item i = Registry.ITEM.get(item);

        var g = json.get("group");
        String group = g == null ? "" : g.getAsString();

        List<String> patternList = new ArrayList<>();
        JsonArray patterns = json.getAsJsonArray("pattern");
        patterns.forEach(p -> patternList.add(p.getAsString()));

        Map<Character, Ingredient> keyMap = new HashMap<>();
        JsonObject keys = json.getAsJsonObject("key");
        keys.entrySet().forEach((e) -> keyMap.put(e.getKey().charAt(0), Ingredient.fromJson(e.getValue())));

        return new ShapedRecipeTemplate(i, count, group, patternList, keyMap);
    }

    public <T extends BlockType> ShapedRecipeBuilder.Result createSimilar(T originalMat, T destinationMat, Item unlockItem, String id) {
        ItemLike newRes = BlockType.changeItemBlockType(this.result, originalMat, destinationMat);
        if (newRes == null)
            throw new UnsupportedOperationException(String.format("Could not convert output item %s", result));

        ShapedRecipeBuilder builder = new ShapedRecipeBuilder(newRes, this.count);

        for (var e : this.keys.entrySet()) {
            Ingredient ing = e.getValue();
            if (ing.getItems().length > 0) {
                Item old = ing.getItems()[0].getItem();
                if (old != Items.BARRIER) {
                    ItemLike i = BlockType.changeItemBlockType(old, originalMat, destinationMat);
                    if(i != null) ing = Ingredient.of(i);
                }
            }
            builder.define(e.getKey(), ing);
        }
        this.pattern.forEach(builder::pattern);
        builder.group(group);
        builder.unlockedBy("has_planks", InventoryChangeTrigger.TriggerInstance.hasItems(unlockItem));

        AtomicReference<ShapedRecipeBuilder.Result> newRecipe = new AtomicReference<>();

        if (id == null) {
            builder.save(r -> newRecipe.set((ShapedRecipeBuilder.Result) r));
        } else {
            builder.save(r -> newRecipe.set((ShapedRecipeBuilder.Result) r), id);
        }
        return newRecipe.get();
    }


}

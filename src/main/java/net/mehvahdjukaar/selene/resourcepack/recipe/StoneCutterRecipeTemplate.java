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
import net.minecraft.world.level.ItemLike;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.atomic.AtomicReference;

public class StoneCutterRecipeTemplate implements IRecipeTemplate<SingleItemRecipeBuilder.Result> {
    public final Item result;
    public final int count;
    public final String group;
    public final Ingredient ingredient;

    public StoneCutterRecipeTemplate(JsonObject json) {
        JsonElement result = json.get("result");
        ResourceLocation item = new ResourceLocation(result.getAsString());
        int count = 1;
        var c = json.get("count");
        if (c != null) count = c.getAsInt();

        this.count = count;
        this.result = Registry.ITEM.get(item);

        var g = json.get("group");
        this.group = g == null ? "" : g.getAsString();

        this.ingredient = Ingredient.fromJson(json.get("ingredient"));
    }

    @Override
    public <T extends BlockType> SingleItemRecipeBuilder.Result createSimilar(
            T originalMat, T destinationMat, Item unlockItem, @Nullable String id) {
        ItemLike newRes = BlockType.changeItemBlockType(this.result, originalMat, destinationMat);
        if (newRes == null)
            throw new UnsupportedOperationException(String.format("Could not convert output item %s", result));

        boolean atLeastOneChanged = false;
        Ingredient ing = ingredient;
        for (var in : ing.getItems()) {
            Item it = in.getItem();
            if (it != Items.BARRIER) {
                ItemLike i = BlockType.changeItemBlockType(it, originalMat, destinationMat);
                if (i != null) {
                    atLeastOneChanged = true;
                    //converts first ingredient it finds
                    ing = Ingredient.of(i);
                    break;
                }
            }
        }
        //if recipe fails
        if (!atLeastOneChanged) return null;

        SingleItemRecipeBuilder builder = SingleItemRecipeBuilder.stonecutting(ing, newRes);
        builder.group(group);

        builder.unlockedBy("has_planks", InventoryChangeTrigger.TriggerInstance.hasItems(unlockItem));

        AtomicReference<SingleItemRecipeBuilder.Result> newRecipe = new AtomicReference<>();

        if (id == null) {
            builder.save(r -> newRecipe.set((SingleItemRecipeBuilder.Result) r));
        } else {
            builder.save(r -> newRecipe.set((SingleItemRecipeBuilder.Result) r), id);
        }
        return newRecipe.get();
    }


}

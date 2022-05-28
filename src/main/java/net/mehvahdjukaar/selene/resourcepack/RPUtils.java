package net.mehvahdjukaar.selene.resourcepack;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.internal.Streams;
import com.google.gson.stream.JsonWriter;
import net.mehvahdjukaar.selene.block_set.IBlockType;
import net.mehvahdjukaar.selene.resourcepack.recipe.IRecipeTemplate;
import net.mehvahdjukaar.selene.resourcepack.recipe.ShapedRecipeTemplate;
import net.mehvahdjukaar.selene.resourcepack.recipe.ShapelessRecipeTemplate;
import net.mehvahdjukaar.selene.resourcepack.recipe.StoneCutterRecipeTemplate;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.common.crafting.conditions.ICondition;
import net.minecraftforge.registries.ForgeRegistries;

import javax.management.openmbean.InvalidOpenTypeException;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.function.Predicate;

public class RPUtils {

    public static String serializeJson(JsonElement json) throws IOException {
        StringWriter stringWriter = new StringWriter();

        JsonWriter jsonWriter = new JsonWriter(stringWriter);
        jsonWriter.setLenient(true);
        jsonWriter.setIndent("  ");

        Streams.write(json, jsonWriter);

        return stringWriter.toString();
    }

    public static JsonObject deserializeJson(InputStream stream) {
        return GsonHelper.parse(new InputStreamReader(stream, StandardCharsets.UTF_8));
    }

    public static ResourceLocation findFirstBlockTextureLocation(ResourceManager manager, Block block) throws FileNotFoundException {
        return findFirstBlockTextureLocation(manager, block, t -> true);
    }

    /**
     * Grabs the first texture from a given block
     *
     * @param manager          resource manager
     * @param block            target block
     * @param texturePredicate predicate that will be applied to the texture name
     * @return found texture location
     */
    public static ResourceLocation findFirstBlockTextureLocation(ResourceManager manager, Block block, Predicate<String> texturePredicate) throws FileNotFoundException {
        try {
            ResourceLocation res = block.getRegistryName();
            Resource blockState = manager.getResource(ResType.BLOCKSTATES.getPath(res));

            JsonElement bsElement = RPUtils.deserializeJson(blockState.getInputStream());

            //grabs the first resource location of a model
            String modelPath = findAllResourcesInJsonRecursive(bsElement.getAsJsonObject(), s -> s.equals("model")).get(0);
            JsonElement modelElement;
            try {
                Resource model = manager.getResource(ResType.MODELS.getPath(modelPath));
                modelElement = RPUtils.deserializeJson(model.getInputStream());
            } catch (Exception e) {
                throw new Exception("Failed to parse model at " + modelPath);
            }

            String value = findAllResourcesInJsonRecursive(modelElement.getAsJsonObject().getAsJsonObject("textures"))
                    .stream().filter(texturePredicate).findAny().get();
            return new ResourceLocation(value);
        } catch (Exception e) {
            throw new FileNotFoundException("Could not find any texture associated to the given block " + block.getRegistryName());
        }
    }

    //TODO: account for parents

    public static ResourceLocation findFirstItemTextureLocation(ResourceManager manager, Item block) throws FileNotFoundException {
        return findFirstItemTextureLocation(manager, block, t -> true);
    }

    /**
     * Grabs the first texture from a given item
     *
     * @param manager          resource manager
     * @param item             target item
     * @param texturePredicate predicate that will be applied to the texture name
     * @return found texture location
     */
    public static ResourceLocation findFirstItemTextureLocation(ResourceManager manager, Item item, Predicate<String> texturePredicate) throws FileNotFoundException {
        try {
            ResourceLocation res = item.getRegistryName();
            Resource itemModel = manager.getResource(ResType.ITEM_MODELS.getPath(res));

            JsonElement bsElement = RPUtils.deserializeJson(itemModel.getInputStream());

            String value = findAllResourcesInJsonRecursive(bsElement.getAsJsonObject().getAsJsonObject("textures"))
                    .stream().filter(texturePredicate).findAny().get();
            return new ResourceLocation(value);
        } catch (Exception e) {
            throw new FileNotFoundException("Could not find any texture associated to the given item " + item.getRegistryName());
        }
    }

    public static String findFirstResourceInJsonRecursive(JsonElement element) throws NoSuchElementException {
        if (element instanceof JsonArray array) {
            return findFirstResourceInJsonRecursive(array.get(0));
        } else if (element instanceof JsonObject) {
            var entries = element.getAsJsonObject().entrySet();
            JsonElement child = entries.stream().findAny().get().getValue();
            return findFirstResourceInJsonRecursive(child);
        } else return element.getAsString();
    }

    public static List<String> findAllResourcesInJsonRecursive(JsonElement element) {
        return findAllResourcesInJsonRecursive(element, s -> true);
    }

    public static List<String> findAllResourcesInJsonRecursive(JsonElement element, Predicate<String> filter) {
        if (element instanceof JsonArray array) {
            List<String> list = new ArrayList<>();

            array.forEach(e -> list.addAll(findAllResourcesInJsonRecursive(e)));
            return list;
        } else if (element instanceof JsonObject json) {
            var entries = json.entrySet();

            List<String> list = new ArrayList<>();
            for (var c : entries) {
                if (c.getValue().isJsonPrimitive() && !filter.test(c.getKey())) continue;
                var l = findAllResourcesInJsonRecursive(c.getValue(), filter);
                list.addAll(l);
            }
            return list;
        } else return List.of(element.getAsString());
    }

    //recipe stuff

    public static Recipe<?> readRecipe(ResourceManager manager, String location) {
        return readRecipe(manager,ResType.RECIPES.getPath(location));
    }

    public static Recipe<?> readRecipe(ResourceManager manager, ResourceLocation location) {
        try {
            JsonObject element = RPUtils.deserializeJson(manager.getResource(location).getInputStream());
            return RecipeManager.fromJson(location, element, ICondition.IContext.EMPTY);
        } catch (Exception e) {
            throw new InvalidOpenTypeException(String.format("Failed to get recipe at %s: %s", location, e));
        }
    }

    public static IRecipeTemplate<?> readRecipeAsTemplate(ResourceManager manager, String location) {
        return readRecipeAsTemplate(manager,ResType.RECIPES.getPath(location));
    }

    public static IRecipeTemplate<?> readRecipeAsTemplate(ResourceManager manager, ResourceLocation location) {
        try {
            JsonObject element = RPUtils.deserializeJson(manager.getResource(location).getInputStream());
            String type = GsonHelper.getAsString(element, "type");
            RecipeSerializer<?> s = ForgeRegistries.RECIPE_SERIALIZERS.getValue(new ResourceLocation(type));
            if (s == RecipeSerializer.SHAPED_RECIPE) {
                return ShapedRecipeTemplate.fromJson(element);
            } else if (s == RecipeSerializer.SHAPELESS_RECIPE) {
                return ShapelessRecipeTemplate.fromJson(element);
            } else if(s == RecipeSerializer.STONECUTTER){
                return StoneCutterRecipeTemplate.fromJson(element);
            }
            throw new UnsupportedOperationException(String.format("Invalid recipe serializer: %s", s));
        } catch (Exception e) {
            throw new InvalidOpenTypeException(String.format("Failed to get recipe at %s: %s", location, e));
        }
    }

    public static <T extends IBlockType> Recipe<?> makeSimilarRecipe(Recipe<?> original, T originalMat, T destinationMat, String baseID) {
        if (original instanceof ShapedRecipe or) {
            List<Ingredient> newList = new ArrayList<>();
            for (var ingredient : or.getIngredients()) {
                if (ingredient != null && ingredient.getItems().length > 0) {
                    Item i = IBlockType.changeItemBlockType(ingredient.getItems()[0].getItem(), originalMat, destinationMat);
                    newList.add(Ingredient.of(i));
                }
            }
            Item originalRes = or.getResultItem().getItem();
            Item newRes = IBlockType.changeItemBlockType(originalRes, originalMat, destinationMat);
            if (newRes == originalRes) throw new UnsupportedOperationException("Failed to convert recipe");
            ItemStack result = newRes.getDefaultInstance();
            ResourceLocation newId = new ResourceLocation(baseID + "/" + destinationMat.getAppendableId());
            NonNullList<Ingredient> ingredients = NonNullList.of(Ingredient.EMPTY, newList.toArray(Ingredient[]::new));
            return new ShapedRecipe(newId, or.getGroup(), or.getWidth(), or.getHeight(), ingredients, result);
        } else if (original instanceof ShapelessRecipe or) {
            List<Ingredient> newList = new ArrayList<>();
            for (var ingredient : or.getIngredients()) {
                if (ingredient != null && ingredient.getItems().length > 0) {
                    Item i = IBlockType.changeItemBlockType(ingredient.getItems()[0].getItem(), originalMat, destinationMat);
                    newList.add(Ingredient.of(i));
                }
            }
            Item originalRes = or.getResultItem().getItem();
            Item newRes = IBlockType.changeItemBlockType(originalRes, originalMat, destinationMat);
            if (newRes == originalRes) throw new UnsupportedOperationException("Failed to convert recipe");
            ItemStack result = newRes.getDefaultInstance();
            ResourceLocation newId = new ResourceLocation(baseID + "/" + destinationMat.getAppendableId());
            NonNullList<Ingredient> ingredients = NonNullList.of(Ingredient.EMPTY, newList.toArray(Ingredient[]::new));
            return new ShapelessRecipe(newId, or.getGroup(), result, ingredients);
        } else {
            throw new UnsupportedOperationException(String.format("Original recipe %s must be Shaped or Shapeless", original));
        }
    }

}

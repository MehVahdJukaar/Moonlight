package net.mehvahdjukaar.moonlight.resources;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.internal.Streams;
import com.google.gson.stream.JsonWriter;
import net.mehvahdjukaar.moonlight.block_set.BlockType;
import net.mehvahdjukaar.moonlight.client.TextureCache;
import net.mehvahdjukaar.moonlight.resources.recipe.IRecipeTemplate;
import net.mehvahdjukaar.moonlight.resources.recipe.TemplateRecipeManager;
import net.mehvahdjukaar.moonlight.util.Utils;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;

import javax.management.openmbean.InvalidOpenTypeException;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
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
        var cached = TextureCache.getCached(block, texturePredicate);
        if (cached != null) {
            return new ResourceLocation(cached);
        }
        ResourceLocation res = Utils.getID(block);
        var blockState = manager.getResource(ResType.BLOCKSTATES.getPath(res));
        try(var bsStream = blockState.get().open()) {


            JsonElement bsElement = RPUtils.deserializeJson(bsStream);

            //grabs the first resource location of a model
            String modelPath = findAllResourcesInJsonRecursive(bsElement.getAsJsonObject(), s -> s.equals("model"))
                    .stream().findAny().get();
            JsonElement modelElement;
            var model = manager.getResource(ResType.MODELS.getPath(modelPath));
            try(var modelStream = model.get().open()) {
                modelElement = RPUtils.deserializeJson(modelStream);
            } catch (Exception e) {
                throw new Exception("Failed to parse model at " + modelPath);
            }
            var textures = findAllResourcesInJsonRecursive(modelElement.getAsJsonObject().getAsJsonObject("textures"));

            for (var t : textures) {
                TextureCache.add(block, t);
                if (texturePredicate.test(t)) return new ResourceLocation(t);
            }
        } catch (Exception ignored) {
        }
        throw new FileNotFoundException("Could not find any texture associated to the given block " + res);

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
        var cached = TextureCache.getCached(item, texturePredicate);
        if (cached != null) return new ResourceLocation(cached);
        ResourceLocation res = Utils.getID(item);
        var itemModel = manager.getResource(ResType.ITEM_MODELS.getPath(res));
        try (var stream = itemModel.get().open()) {

            JsonElement bsElement = RPUtils.deserializeJson(stream);

            var textures = findAllResourcesInJsonRecursive(bsElement.getAsJsonObject().getAsJsonObject("textures"));
            for (var t : textures) {
                TextureCache.add(item, t);
                if (texturePredicate.test(t)) return new ResourceLocation(t);
            }

        } catch (Exception ignored) {
        }
        throw new FileNotFoundException("Could not find any texture associated to the given item " + res);
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

    public static Set<String> findAllResourcesInJsonRecursive(JsonElement element) {
        return findAllResourcesInJsonRecursive(element, s -> true);
    }

    public static Set<String> findAllResourcesInJsonRecursive(JsonElement element, Predicate<String> filter) {
        if (element instanceof JsonArray array) {
            Set<String> list = new HashSet<>();

            array.forEach(e -> list.addAll(findAllResourcesInJsonRecursive(e, filter)));
            return list;
        } else if (element instanceof JsonObject json) {
            var entries = json.entrySet();

            Set<String> list = new HashSet<>();
            for (var c : entries) {
                if (c.getValue().isJsonPrimitive() && !filter.test(c.getKey())) continue;
                var l = findAllResourcesInJsonRecursive(c.getValue(), filter);

                list.addAll(l);
            }
            return list;
        } else {
            return Set.of(element.getAsString());
        }
    }
    /*
    //this is actually slightly slower
        var bbb = BlockModelDefinition.fromStream( new BlockModelDefinition.Context(),
        new InputStreamReader(oakBlockstate.getInputStream(), StandardCharsets.UTF_8));
     */

    //recipe stuff

    public static Recipe<?> readRecipe(ResourceManager manager, String location) {
        return readRecipe(manager, ResType.RECIPES.getPath(location));
    }

    public static Recipe<?> readRecipe(ResourceManager manager, ResourceLocation location) {
        var resource = manager.getResource(location);
        try (var stream = resource.get().open()) {
            JsonObject element = RPUtils.deserializeJson(stream);
            return RecipeManager.fromJson(location, element);
        } catch (Exception e) {
            throw new InvalidOpenTypeException(String.format("Failed to get recipe at %s: %s", location, e));
        }
    }

    public static IRecipeTemplate<?> readRecipeAsTemplate(ResourceManager manager, String location) {
        return readRecipeAsTemplate(manager, ResType.RECIPES.getPath(location));
    }


    public static IRecipeTemplate<?> readRecipeAsTemplate(ResourceManager manager, ResourceLocation location) {
        var resource = manager.getResource(location);
        try (var stream = resource.get().open()) {
            JsonObject element = RPUtils.deserializeJson(stream);
            return TemplateRecipeManager.read(element);

        } catch (Exception e) {
            throw new InvalidOpenTypeException(String.format("Failed to get recipe at %s: %s", location, e));
        }
    }

    public static <T extends BlockType> Recipe<?> makeSimilarRecipe(Recipe<?> original, T originalMat, T destinationMat, String baseID) {
        if (original instanceof ShapedRecipe or) {
            List<Ingredient> newList = new ArrayList<>();
            for (var ingredient : or.getIngredients()) {
                if (ingredient != null && ingredient.getItems().length > 0) {
                    ItemLike i = BlockType.changeItemBlockType(ingredient.getItems()[0].getItem(), originalMat, destinationMat);
                    if (i != null) newList.add(Ingredient.of(i));
                }
            }
            Item originalRes = or.getResultItem().getItem();
            ItemLike newRes = BlockType.changeItemBlockType(originalRes, originalMat, destinationMat);
            if (newRes == null) throw new UnsupportedOperationException("Failed to convert recipe");
            ItemStack result = newRes.asItem().getDefaultInstance();
            ResourceLocation newId = new ResourceLocation(baseID + "/" + destinationMat.getAppendableId());
            NonNullList<Ingredient> ingredients = NonNullList.of(Ingredient.EMPTY, newList.toArray(Ingredient[]::new));
            return new ShapedRecipe(newId, or.getGroup(), or.getWidth(), or.getHeight(), ingredients, result);
        } else if (original instanceof ShapelessRecipe or) {
            List<Ingredient> newList = new ArrayList<>();
            for (var ingredient : or.getIngredients()) {
                if (ingredient != null && ingredient.getItems().length > 0) {
                    ItemLike i = BlockType.changeItemBlockType(ingredient.getItems()[0].getItem(), originalMat, destinationMat);
                    if (i != null) newList.add(Ingredient.of(i));
                }
            }
            Item originalRes = or.getResultItem().getItem();
            ItemLike newRes = BlockType.changeItemBlockType(originalRes, originalMat, destinationMat);
            if (newRes == null) throw new UnsupportedOperationException("Failed to convert recipe");
            ItemStack result = newRes.asItem().getDefaultInstance();
            ResourceLocation newId = new ResourceLocation(baseID + "/" + destinationMat.getAppendableId());
            NonNullList<Ingredient> ingredients = NonNullList.of(Ingredient.EMPTY, newList.toArray(Ingredient[]::new));
            return new ShapelessRecipe(newId, or.getGroup(), result, ingredients);
        } else {
            throw new UnsupportedOperationException(String.format("Original recipe %s must be Shaped or Shapeless", original));
        }
    }

}

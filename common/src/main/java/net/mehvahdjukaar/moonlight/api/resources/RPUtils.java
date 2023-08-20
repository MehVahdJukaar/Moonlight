package net.mehvahdjukaar.moonlight.api.resources;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.internal.Streams;
import com.google.gson.stream.JsonWriter;
import net.mehvahdjukaar.moonlight.api.client.TextureCache;
import net.mehvahdjukaar.moonlight.api.platform.ClientHelper;
import net.mehvahdjukaar.moonlight.api.resources.pack.DynamicTexturePack;
import net.mehvahdjukaar.moonlight.api.resources.recipe.IRecipeTemplate;
import net.mehvahdjukaar.moonlight.api.resources.recipe.TemplateRecipeManager;
import net.mehvahdjukaar.moonlight.api.set.BlockType;
import net.mehvahdjukaar.moonlight.api.set.wood.WoodTypeRegistry;
import net.mehvahdjukaar.moonlight.api.util.Utils;
import net.mehvahdjukaar.moonlight.core.Moonlight;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.renderer.block.model.ItemOverride;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.*;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.NotNull;

import javax.management.openmbean.InvalidOpenTypeException;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

public class RPUtils {

    public static String serializeJson(JsonElement json) throws IOException {
        try (StringWriter stringWriter = new StringWriter();
             JsonWriter jsonWriter = new JsonWriter(stringWriter)) {

            jsonWriter.setLenient(true);
            jsonWriter.setIndent("  ");

            Streams.write(json, jsonWriter);

            return stringWriter.toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    //remember to close this stream
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
        try (var bsStream = blockState.orElseThrow().open()) {


            JsonElement bsElement = RPUtils.deserializeJson(bsStream);

            //grabs the first resource location of a model
            Set<String> models = findAllResourcesInJsonRecursive(bsElement.getAsJsonObject(), s -> s.equals("model"));

            for(var modelPath : models) {

                List<String> textures = findAllTexturesInModelRecursive(manager, modelPath);

                for (var t : textures) {
                    TextureCache.add(block, t);
                    if (texturePredicate.test(t)) return new ResourceLocation(t);
                }
            }
        } catch (Exception ignored) {
        }
        //if texture is not there try to guess location. Hack for better end
        var hack = guessTextureLocation(res, manager, block);
        for (var t : hack) {
            TextureCache.add(block, t);
            if (texturePredicate.test(t)) return new ResourceLocation(t);
        }

        throw new FileNotFoundException("Could not find any texture associated to the given block " + res);
    }

    private static List<String> guessTextureLocation(ResourceLocation id, ResourceManager manager, Block block) {
        String name = id.getPath();
        List<String> textures = new ArrayList<>();
        for (var w : WoodTypeRegistry.getTypes()) {
            if (name.contains(w.id.getPath())) {
                for (var c : w.getChildren()) {
                    if (c.getValue() == block) {
                        if (Objects.equals(c.getKey(), "log") || c.getKey().equals("stripped_log")) {
                            textures.add(id.getNamespace() + ":block/" + name + "_top");
                            textures.add(id.getNamespace() + ":block/" + name + "_side");
                        } else textures.add(id.getNamespace() + ":block/" + name);
                        return textures;
                    }
                }
            }
        }
        return List.of();
    }

    @NotNull
    private static List<String> findAllTexturesInModelRecursive(ResourceManager manager, String modelPath) throws Exception {
        JsonObject modelElement;
        try (var modelStream = manager.getResource(ResType.MODELS.getPath(modelPath)).get().open()) {
            modelElement = RPUtils.deserializeJson(modelStream).getAsJsonObject();
        } catch (Exception e) {
            throw new Exception("Failed to parse model at " + modelPath);
        }
        var textures = new ArrayList<>(findAllResourcesInJsonRecursive(modelElement.getAsJsonObject("textures")));
        if (textures.isEmpty()) {
            if (modelElement.has("parent")) {
                var parentPath = modelElement.get("parent").getAsString();
                textures.addAll(findAllTexturesInModelRecursive(manager, parentPath));
            }
        }
        return textures;
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
    //recipe stuff

    public static Recipe<?> readRecipe(ResourceManager manager, String location) {
        return readRecipe(manager, ResType.RECIPES.getPath(location));
    }

    public static Recipe<?> readRecipe(ResourceManager manager, ResourceLocation location) {
        var resource = manager.getResource(location);
        try (var stream = resource.orElseThrow().open()) {
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
        try (var stream = resource.orElseThrow().open()) {
            JsonObject element = RPUtils.deserializeJson(stream);
            try {
                return TemplateRecipeManager.read(element);
            } catch (Exception e) {
                Moonlight.LOGGER.error(element);
                Moonlight.LOGGER.error(location);
                throw e;
            }

        } catch (Exception e) {
            throw new InvalidOpenTypeException(String.format("Failed to get recipe at %s: %s", location, e));
        }
    }

    public static <T extends BlockType> Recipe<?> makeSimilarRecipe(Recipe<?> original, T originalMat, T destinationMat, String baseID) {
        if (original instanceof ShapedRecipe or) {
            List<Ingredient> newList = new ArrayList<>();
            for (var ingredient : or.getIngredients()) {
                if (ingredient != null && ingredient.getItems().length > 0) {
                    ItemLike i = BlockType.changeItemType(ingredient.getItems()[0].getItem(), originalMat, destinationMat);
                    if (i != null) newList.add(Ingredient.of(i));
                }
            }
            Item originalRes = or.getResultItem(RegistryAccess.EMPTY).getItem();
            ItemLike newRes = BlockType.changeItemType(originalRes, originalMat, destinationMat);
            if (newRes == null) throw new UnsupportedOperationException("Failed to convert recipe");
            ItemStack result = newRes.asItem().getDefaultInstance();
            ResourceLocation newId = new ResourceLocation(baseID + "/" + destinationMat.getAppendableId());
            NonNullList<Ingredient> ingredients = NonNullList.of(Ingredient.EMPTY, newList.toArray(Ingredient[]::new));
            return new ShapedRecipe(newId, or.getGroup(), or.category(), or.getWidth(), or.getHeight(), ingredients, result);
        } else if (original instanceof ShapelessRecipe or) {
            List<Ingredient> newList = new ArrayList<>();
            for (var ingredient : or.getIngredients()) {
                if (ingredient != null && ingredient.getItems().length > 0) {
                    ItemLike i = BlockType.changeItemType(ingredient.getItems()[0].getItem(), originalMat, destinationMat);
                    if (i != null) newList.add(Ingredient.of(i));
                }
            }
            Item originalRes = or.getResultItem(RegistryAccess.EMPTY).getItem();
            ItemLike newRes = BlockType.changeItemType(originalRes, originalMat, destinationMat);
            if (newRes == null) throw new UnsupportedOperationException("Failed to convert recipe");
            ItemStack result = newRes.asItem().getDefaultInstance();
            ResourceLocation newId = new ResourceLocation(baseID + "/" + destinationMat.getAppendableId());
            NonNullList<Ingredient> ingredients = NonNullList.of(Ingredient.EMPTY, newList.toArray(Ingredient[]::new));
            return new ShapelessRecipe(newId, or.getGroup(), or.category(), result, ingredients);
        } else {
            throw new UnsupportedOperationException(String.format("Original recipe %s must be Shaped or Shapeless", original));
        }
    }

    @FunctionalInterface
    public interface OverrideAppender {
        void add(ItemOverride override);
    }

    /**
     * Utility method to add models overrides in a non-destructive way. Provided overrides will be added on top of whatever model is currently provided by vanilla or mod resources. IE: crossbows
     */
    public static void appendModelOverride(ResourceManager manager, DynamicTexturePack pack,
                                           ResourceLocation modelRes, Consumer<OverrideAppender> modelConsumer) {
        var o = manager.getResource(ResType.ITEM_MODELS.getPath(modelRes));
        if (o.isPresent()) {
            try (var model = o.get().open()) {
                var json = RPUtils.deserializeJson(model);
                JsonArray overrides;
                if(json.has("overrides")){
                    overrides = json.getAsJsonArray("overrides");;
                }else overrides = new JsonArray();

                modelConsumer.accept(ov -> overrides.add(serializeOverride(ov)));

                json.add("overrides", overrides);
                pack.addItemModel(modelRes, json);
            } catch (Exception ignored) {
            }
        }
    }

    private static JsonObject serializeOverride(ItemOverride override) {
        JsonObject json = new JsonObject();
        json.addProperty("model", override.getModel().toString());
        JsonObject predicates = new JsonObject();
        override.getPredicates().forEach(p -> {
            predicates.addProperty(p.getProperty().toString(), p.getValue());
        });
        json.add("predicate", predicates);
        return json;
    }


}

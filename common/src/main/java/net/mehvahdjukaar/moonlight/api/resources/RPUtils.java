package net.mehvahdjukaar.moonlight.api.resources;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.internal.Streams;
import com.google.gson.stream.JsonWriter;
import com.mojang.serialization.JsonOps;
import net.mehvahdjukaar.moonlight.api.client.TextureCache;
import net.mehvahdjukaar.moonlight.api.resources.pack.ResourceSink;
import net.mehvahdjukaar.moonlight.api.set.BlockType;
import net.mehvahdjukaar.moonlight.api.set.wood.WoodType;
import net.mehvahdjukaar.moonlight.api.set.wood.WoodTypeRegistry;
import net.mehvahdjukaar.moonlight.api.util.Utils;
import net.mehvahdjukaar.moonlight.core.Moonlight;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.NotNull;

import javax.management.openmbean.InvalidOpenTypeException;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class RPUtils {

    public static String serializeJson(JsonElement json) throws IOException {
        try (StringWriter stringWriter = new StringWriter();
             JsonWriter jsonWriter = new JsonWriter(stringWriter)) {

            jsonWriter.setLenient(true);
            jsonWriter.setIndent("  ");

            Streams.write(json, jsonWriter);

            return stringWriter.toString();
        } catch (Exception e) {
            throw new IOException(e);
        }
    }

    //remember to close this stream
    public static JsonObject deserializeJson(InputStream stream) {
        return GsonHelper.parse(new InputStreamReader(stream, StandardCharsets.UTF_8));
    }

    /** Whole content of a text resource, or an empty string when it can't be read. */
    public static String readTextFile(ResourceManager manager, Identifier path) {
        var resource = manager.getResource(path);
        if (resource.isPresent()) {
            try (BufferedReader reader = resource.get().openAsReader()) {
                return reader.lines().collect(Collectors.joining("\n"));
            } catch (IOException ignored) {
            }
        }
        Moonlight.LOGGER.error("Failed to read text file {}", path);
        return "";
    }

    public static Identifier findFirstBlockTextureLocation(ResourceManager manager, Block block) throws FileNotFoundException {
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
    public static Identifier findFirstBlockTextureLocation(ResourceManager manager, Block block, Predicate<String> texturePredicate) throws FileNotFoundException {
        Identifier blockId = Utils.getID(block);
        var cached = TextureCache.getCachedTexture(block, texturePredicate);
        if (cached.isSuccess()) {
            return Identifier.parse(cached.getObject());
        } else if (cached.isPass()) {
            var blockState = manager.getResource(ResType.BLOCKSTATES.getPath(blockId));
            try (var bsStream = blockState.orElseThrow().open()) {

                JsonElement bsElement = RPUtils.deserializeJson(bsStream);
                //grabs the first resource location of a model
                Set<String> models = findAllResourcesInJsonRecursive(bsElement.getAsJsonObject(), s -> s.equals("model"));

                for (var modelPath : models) {
                    List<String> textures = findAllTexturesInModelRecursive(manager, modelPath);

                    for (var t : textures) {
                        TextureCache.add(block, t);
                    }
                }
                cached = TextureCache.getCachedTexture(block, texturePredicate);
                if (cached.isSuccess()) {
                    return Identifier.parse(cached.getObject());
                }

            } catch (Exception ignored) {
            }


            //if texture is not there try to guess location. Hack for better end
            var hack = guessBlockTextureLocation(blockId, block);
            for (var t : hack) {
                TextureCache.add(block, t);
                if (texturePredicate.test(t)) return Identifier.parse(t);
            }
        }
        throw new FileNotFoundException("Could not find any texture associated to the given block " + blockId);
    }

    //if texture is not there try to guess location. Hack for better end
    private static Set<String> guessItemTextureLocation(Identifier id, Item item) {
        return Set.of(id.getNamespace() + ":item/" + item);
    }

    private static List<String> guessBlockTextureLocation(Identifier id, Block block) {
        String name = id.getPath();
        List<String> textures = new ArrayList<>();
        //just works for wood types
        WoodType w = WoodTypeRegistry.INSTANCE.getBlockTypeOf(block);
        if (w != null) {
            String key = w.getChildKey(block);
            if (Objects.equals(key, "log") || Objects.equals(key, "stripped_log")) {
                textures.add(id.getNamespace() + ":block/" + name + "_top");
                textures.add(id.getNamespace() + ":block/" + name + "_side");
            }
        }
        textures.add(id.getNamespace() + ":block/" + name);
        return textures;
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

    public static Identifier findFirstItemTextureLocation(ResourceManager manager, Item block) throws FileNotFoundException {
        return findFirstItemTextureLocation(manager, block, t -> true);
    }

    private static List<String> findItemDefinitionTextures(ResourceManager manager, Identifier itemId) {
        List<String> textures = new ArrayList<>();
        var definition = manager.getResource(ResType.ITEMS.getPath(itemId));
        if (definition.isEmpty()) return textures;
        try (var stream = definition.get().open()) {
            JsonElement json = RPUtils.deserializeJson(stream);
            for (var modelPath : findAllResourcesInJsonRecursive(json, s -> s.equals("model"))) {
                textures.addAll(findAllTexturesInModelRecursive(manager, modelPath));
            }
        } catch (Exception ignored) {
        }
        return textures;
    }

    /**
     * Grabs the first texture from a given item
     *
     * @param manager          resource manager
     * @param item             target item
     * @param texturePredicate predicate that will be applied to the texture name
     * @return found texture location
     */
    public static Identifier findFirstItemTextureLocation(ResourceManager manager, Item item, Predicate<String> texturePredicate) throws FileNotFoundException {
        Identifier itemId = Utils.getID(item);
        var cached = TextureCache.getCachedTexture(item, texturePredicate);
        if (cached.isSuccess()) {
            return Identifier.parse(cached.getObject());
        } else if (cached.isPass()) {

            Collection<String> textures = findItemDefinitionTextures(manager, itemId);
            if (textures.isEmpty()) {
                //no definition (or an exotic one we can't follow), assume the conventional geometry model
                try {
                    textures = findAllTexturesInModelRecursive(manager, itemId.withPrefix("item/").toString());
                } catch (Exception ignored) {
                }
            }
            if (textures.isEmpty()) {
                //if texture is not there try to guess location. Hack for better end
                textures = guessItemTextureLocation(itemId, item);
            }
            for (var t : textures) {
                TextureCache.add(item, t);
            }
            cached = TextureCache.getCachedTexture(item, texturePredicate);
            if (cached.isSuccess()) {
                return Identifier.parse(cached.getObject());
            }
        }
        throw new FileNotFoundException("Could not find any texture associated to the given item " + itemId);
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
        return readRecipeAbsolute(manager, ResType.RECIPES.getPath(location));
    }

    // path is relative to recipe folder
    public static Recipe<?> readRecipe(ResourceManager manager, Identifier location) {
        return readRecipeAbsolute(manager, ResType.RECIPES.getPath(location));
    }

    private static Recipe<?> readRecipeAbsolute(ResourceManager manager, Identifier location) {
        var resource = manager.getResource(location);
        try (var stream = resource.orElseThrow().open()) {
            JsonObject element = RPUtils.deserializeJson(stream);
            return readRecipe(element);
        } catch (Exception e) {
            throw new InvalidOpenTypeException(String.format("Failed to get recipe at %s: %s", location, e));
        }
    }

    public static Recipe<?> readRecipe(JsonElement element) {
        return Recipe.CODEC.parse(JsonOps.INSTANCE, element).getOrThrow();
    }

    public static <T extends Recipe<?>> JsonElement writeRecipe(T recipe) {
        return Recipe.CODEC.encodeStart(JsonOps.INSTANCE, recipe).getOrThrow();
    }

    public static Path getResourcePath(Path path, Identifier k, PackType packType) {
        return path.resolve(packType.getDirectory())
                .resolve(k.getNamespace())
                .resolve(k.getPath());
    }

    public static void writeResource(Identifier id, byte[] bytes, Path path, PackType packType) {
        Path p = getResourcePath(path, id, packType);
        try {
            Files.createDirectories(p.getParent());
            Files.write(p, bytes);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}

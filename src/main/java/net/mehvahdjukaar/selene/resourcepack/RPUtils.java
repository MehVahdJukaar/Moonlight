package net.mehvahdjukaar.selene.resourcepack;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.internal.Streams;
import com.google.gson.stream.JsonWriter;
import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

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
        return findFirstBlockTextureLocation(manager,block,t->true);
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
        return findFirstItemTextureLocation(manager,block,t->true);
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

}

package net.mehvahdjukaar.selene.resourcepack;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.internal.Streams;
import com.google.gson.stream.JsonWriter;
import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

import java.io.*;
import java.util.*;
import java.util.function.Predicate;

public class RPUtils {

    /**
     * Represents a generic resource that can be read multiple times. Consumes the original resource
     */
    public static class StaticResource{
        public final byte[] data;
        public final ResourceLocation location;
        public final String sourceName;

        public StaticResource(Resource original) {
            byte[] data1;
            try {
                data1 = original.getInputStream().readAllBytes();
            } catch (IOException e) {
                data1 = new byte[]{};
            }

            this.data = data1;
            this.location = original.getLocation();
            this.sourceName = original.getSourceName();
        }
    }

    public enum ResType {
        GENERIC("%s"),
        TAGS("tags/%s.json"),
        LOOT_TABLES("loot_tables/%s.json"),
        RECIPES("recipes/%s.json"),
        ADVANCEMENTS("advancements/%s.json"),

        LANG("lang/%s.json"),
        TEXTURES("textures/%s.png"),
        BLOCK_TEXTURES("textures/block/%s.png"),
        ITEM_TEXTURES("textures/item/%s.png"),
        ENTITY_TEXTURES("textures/entity/%s.png"),
        MCMETA("textures/%s.png.mcmeta"),
        BLOCK_MCMETA("textures/block/%s.png.mcmeta"),
        ITEM_MCMETA("textures/item/%s.png.mcmeta"),
        MODELS("models/%s.json"),
        BLOCK_MODELS("models/block/%s.json"),
        ITEM_MODELS("models/item/%s.json"),
        BLOCKSTATES("blockstates/%s.json");

        public final String loc;

        ResType(String loc){
            this.loc = loc;
        }

    }

    public static ResourceLocation resPath(ResourceLocation relativeLocation, ResType type) {
        return new ResourceLocation(relativeLocation.getNamespace(), String.format(type.loc, relativeLocation.getPath()));
    }

    public static ResourceLocation resPath(String relativeLocation, ResType type) {
        return resPath(new ResourceLocation(relativeLocation), type);
    }

    public static String serializeJson(JsonElement json) throws IOException {
        StringWriter stringWriter = new StringWriter();

        JsonWriter jsonWriter = new JsonWriter(stringWriter);
        jsonWriter.setLenient(true);
        jsonWriter.setIndent("  ");

        Streams.write(json, jsonWriter);
        return stringWriter.toString();
    }

    public static JsonObject deserializeJson(InputStream stream) {
        JsonElement element = new JsonParser().parse(
                new InputStreamReader(stream)
        );
        return element.getAsJsonObject();
    }


    public static NativeImage findFirstBlockTexture(ResourceManager manager, Block block) throws FileNotFoundException {
        String loc = findFirstBlockTextureLocation(manager, block, s -> true);
        try {
            return NativeImage.read(manager.getResource(RPUtils.resPath(loc, ResType.TEXTURES)).getInputStream());
        } catch (IOException e) {
            throw new FileNotFoundException("Could not resolve texture "+loc);
        }
    }

    public static NativeImage findFirstItemTexture(ResourceManager manager, Item item) throws FileNotFoundException{
        String loc = findFirstItemTextureLocation(manager, item, s -> true);
        try {
            return NativeImage.read(manager.getResource(RPUtils.resPath(loc, ResType.TEXTURES)).getInputStream());
        } catch (IOException e) {
            throw new FileNotFoundException("Could not resolve texture "+loc);
        }
    }

    /**
     * Grabs the first texture from a given block
     * @param manager resource manager
     * @param block target block
     * @param texturePredicate predicate that will be applied to the texture name
     * @return found texture location
     */
    public static String findFirstBlockTextureLocation(ResourceManager manager, Block block, Predicate<String> texturePredicate) throws FileNotFoundException {
        try {
            ResourceLocation res = block.getRegistryName();
            Resource blockState = manager.getResource(RPUtils.resPath(res, ResType.BLOCKSTATES));

            JsonElement bsElement = RPUtils.deserializeJson(blockState.getInputStream());

            String modelPath = findFirstResourceInJsonRecursive(bsElement.getAsJsonObject().get("variants"));
            Resource model = manager.getResource(RPUtils.resPath(modelPath, ResType.MODELS));
            JsonElement modelElement = RPUtils.deserializeJson(model.getInputStream());

            return findAllResourcesInJsonRecursive(modelElement.getAsJsonObject().getAsJsonObject("textures"))
                    .stream().filter(texturePredicate).findAny().get();
        } catch (Exception e) {
            throw new FileNotFoundException("Could not find any texture associated to the given block "+block.getRegistryName());
        }
    }

    /**
     * Grabs the first texture from a given item
     * @param manager resource manager
     * @param item target item
     * @param texturePredicate predicate that will be applied to the texture name
     * @return found texture location
     */
    public static String findFirstItemTextureLocation(ResourceManager manager, Item item, Predicate<String> texturePredicate) throws FileNotFoundException {
        try {
            ResourceLocation res = item.getRegistryName();
            Resource itemModel = manager.getResource(RPUtils.resPath(res, ResType.ITEM_MODELS));

            JsonElement bsElement = RPUtils.deserializeJson(itemModel.getInputStream());

            return findAllResourcesInJsonRecursive(bsElement.getAsJsonObject().getAsJsonObject("textures"))
                    .stream().filter(texturePredicate).findAny().get();
        } catch (Exception e) {
            throw new FileNotFoundException("Could not find any texture associated to the given item "+item.getRegistryName());
        }
    }

    public static String findFirstResourceInJsonRecursive(JsonElement element) throws NoSuchElementException {
        if (element instanceof JsonObject) {
            var entries = element.getAsJsonObject().entrySet();
            JsonElement child = entries.stream().findAny().get().getValue();
            return findFirstResourceInJsonRecursive(child);
        } else return element.getAsString();
    }

    public static List<String> findAllResourcesInJsonRecursive(JsonElement element) {
        if (element instanceof JsonObject) {
            var entries = element.getAsJsonObject().entrySet();
            var children = entries.stream().map(Map.Entry::getValue);
            List<String> list = new ArrayList<>();
            children.map(RPUtils::findAllResourcesInJsonRecursive).forEach(list::addAll);
            return list;
        } else return List.of(element.getAsString());
    }

}

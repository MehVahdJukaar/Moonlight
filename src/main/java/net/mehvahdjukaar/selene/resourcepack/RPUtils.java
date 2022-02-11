package net.mehvahdjukaar.selene.resourcepack;

import com.google.common.collect.Maps;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.internal.Streams;
import com.google.gson.stream.JsonWriter;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraftforge.client.model.generators.VariantBlockStateBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.util.*;
import java.util.function.Function;

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

    public static JsonObject deserializeJson(InputStream stream) throws Exception{
        JsonElement element = new JsonParser().parse(
                new InputStreamReader(stream)
        );
        return element.getAsJsonObject();
    }



    public static class LangBuilder {
        private final Map<String, String> entries = new LinkedHashMap<>();

        public void addGenericEntry(String key, String translation) {
            entries.put(key, translation);
        }

        public void addEntry(Block block, String translation) {
            entries.put(block.getDescriptionId(), translation);
        }

        public void addEntry(Item item, String translation) {
            entries.put(item.getDescriptionId(), translation);
        }

        public void addEntry(EntityType<?> entityType, String translation) {
            entries.put(entityType.getDescriptionId(), translation);
        }

        public JsonElement build() {
            JsonObject json = new JsonObject();
            for (var e : entries.entrySet()) {
                json.addProperty(e.getKey(), e.getValue());
            }
            return json;
        }

    }

    public static class SimpleModelBuilder {
        private final ResourceLocation parent;
        private final Map<String, ResourceLocation> textures = new HashMap<>();

        public SimpleModelBuilder(ResourceLocation parent) {
            this.parent = parent;
        }

        public SimpleModelBuilder texture(String name, ResourceLocation texture) {
            this.textures.put(name, texture);
            return this;
        }

        public JsonElement build() {
            JsonObject json = new JsonObject();
            json.addProperty("parent", this.parent.toString());
            JsonObject text = new JsonObject();

            textures.forEach((key, value) -> text.addProperty(key, value.toString()));
            json.add("textures", text);

            return json;
        }
    }

    public static class ModelConfiguration {
        public final ResourceLocation modelLocation;
        public int xRot = 0;
        public int yRot = 0;
        public boolean uvLock = false;

        public ModelConfiguration(ResourceLocation modelLocation) {
            this.modelLocation = modelLocation;
        }

        public JsonObject toJson() {
            JsonObject model = new JsonObject();
            model.addProperty("model", this.modelLocation.toString());
            if (this.xRot != 0) {
                model.addProperty("x", this.xRot);
            }
            if (this.yRot != 0) {
                model.addProperty("y", this.xRot);
            }
            if (this.uvLock) {
                model.addProperty("uvlock", true);
            }
            return model;
        }
    }

    public static class BlockstateBuilder {
        private final Block block;
        private final Map<Map<Property<?>, Comparable<?>>, ModelConfiguration> blockModelMappings = new HashMap<>();

        public BlockstateBuilder(Block block) {
            this.block = block;
        }

        public static BlockstateBuilder forAllStatesExcept(
                Block block,
                Function<BlockState, ModelConfiguration> modelLocationProvider,
                Property<?>... ignored) {
            var builder = new BlockstateBuilder(block);
            //forge code
            BlockState defaultState = block.defaultBlockState();
            Set<VariantBlockStateBuilder.PartialBlockstate> seen = new HashSet<>();
            loop:
            for (BlockState fullState : block.getStateDefinition().getPossibleStates()) {
                Map<Property<?>, Comparable<?>> propertyValues = Maps.newLinkedHashMap(fullState.getValues());
                //only allows default values for ignored ones so they are essentially removed
                for (Property<?> p : ignored) {
                    if (propertyValues.get(p) != defaultState.getValue(p)) continue loop;
                    propertyValues.remove(p);
                }


                builder.setModel(propertyValues, modelLocationProvider.apply(fullState));

            }
            return builder;
        }

        public BlockstateBuilder setModel(Map<Property<?>, Comparable<?>> properties, ModelConfiguration modelLocation) {
            this.blockModelMappings.put(properties, modelLocation);
            return this;
        }

        public JsonObject build() {
            JsonObject main = new JsonObject();

            JsonObject variants = new JsonObject();

            for (var entry : blockModelMappings.entrySet()) {
                StringBuilder builder = new StringBuilder();

                for (var v : entry.getKey().entrySet()) {
                    builder.append(v.getKey().getName()).append("=").append(v.getValue());
                }

                variants.add(builder.toString(), entry.getValue().toJson());
            }

            main.add("variants", variants);

            return main;
        }

    }
}

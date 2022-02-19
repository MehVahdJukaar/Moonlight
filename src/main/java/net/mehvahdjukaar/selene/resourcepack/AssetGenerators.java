package net.mehvahdjukaar.selene.resourcepack;

import com.google.common.collect.Maps;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraftforge.client.model.generators.VariantBlockStateBuilder;
import org.apache.commons.lang3.StringUtils;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

//util to generate some assets
public class AssetGenerators {


    public static class LangBuilder {
        private final Map<String, String> entries = new LinkedHashMap<>();

        //helper to make lang strings
        public static String getReadableName(String name) {
            return Arrays.stream((name).replace(":","_").split("_"))
                    .map(StringUtils::capitalize).collect(Collectors.joining(" "));
        }

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

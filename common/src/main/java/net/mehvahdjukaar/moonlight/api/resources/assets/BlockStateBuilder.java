package net.mehvahdjukaar.moonlight.api.resources.assets;

//import net.minecraftforge.client.model.generators.VariantBlockStateBuilder;

/*
public class BlockStateBuilder {
    private final Block block;
    private final Map<Map<Property<?>, Comparable<?>>, ModelConfiguration> blockModelMappings = new HashMap<>();

    public BlockStateBuilder(Block block) {
        this.block = block;
    }

    public static BlockStateBuilder forAllStatesExcept(
            Block block,
            Function<BlockState, ModelConfiguration> modelLocationProvider,
            Property<?>... ignored) {
        var builder = new BlockStateBuilder(block);
        //forge code
        BlockState defaultState = block.defaultBlockState();
        Set<VariantBlockStateBuilder.PartialBlockstate> seen = new HashSet<>();
        loop:
        for (BlockState fullState : block.getStateDefinition().getPossibleStates()) {
            Map<Property<?>, Comparable<?>> propertyValues = Maps.newLinkedHashMap(fullState.getValues());
            //only allows default getValues for ignored ones so they are essentially removed
            for (Property<?> p : ignored) {
                if (propertyValues.get(p) != defaultState.getValue(p)) continue loop;
                propertyValues.remove(p);
            }


            builder.setModel(propertyValues, modelLocationProvider.apply(fullState));

        }
        return builder;
    }

    public BlockStateBuilder setModel(Map<Property<?>, Comparable<?>> properties, ModelConfiguration modelLocation) {
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

}
*/
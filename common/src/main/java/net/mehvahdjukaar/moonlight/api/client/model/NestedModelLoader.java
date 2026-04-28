package net.mehvahdjukaar.moonlight.api.client.model;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelState;

import java.util.function.BiFunction;

/**
 * Simple implementation of a dynamic model that accepts another model as a parameter
 */
public class NestedModelLoader implements CustomModelLoader {

    private final BiFunction<BakedModel, ModelState, CustomBakedModel> factory;
    private final String path;

    public NestedModelLoader(String modelPath, BiFunction<BakedModel, ModelState, CustomBakedModel> bakedModelFactory) {
        this.factory = bakedModelFactory;
        this.path = modelPath;
    }

    @Override
    public CustomGeometry deserialize(JsonObject json, JsonDeserializationContext context) throws JsonParseException {
        var j = json.get(path);
        return (modelBaker, spriteGetter, transform) -> {

            var baked = CustomModelLoader.parseModel(j, modelBaker, spriteGetter, transform);
            return factory.apply(baked, transform);
        };
    }

}
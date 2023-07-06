package net.mehvahdjukaar.moonlight.api.client.model;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import net.mehvahdjukaar.moonlight.api.platform.ClientHelper;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.*;
import net.minecraft.resources.ResourceLocation;

import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * Simple implementation of a dynamic model that accepts another model as a parameter
 */
public class NestedModelLoader implements CustomModelLoader {

    private final BiFunction<BakedModel, ModelState, CustomBakedModel> factory;
    private final String path;

    @Deprecated(forRemoval = true)
    public NestedModelLoader(String modelPath, Function<BakedModel, CustomBakedModel> bakedModelFactory) {
        this(modelPath, (a, b) -> bakedModelFactory.apply(a));
    }

    public NestedModelLoader(String modelPath, BiFunction<BakedModel, ModelState, CustomBakedModel> bakedModelFactory) {
        this.factory = bakedModelFactory;
        this.path = modelPath;
    }

    @Override
    public CustomGeometry deserialize(JsonObject json, JsonDeserializationContext context) throws JsonParseException {
        var j = json.get(path);
        return (modelBaker, spriteGetter, transform, location) -> {

            var baked = parseModel(j, modelBaker, spriteGetter, transform, location);
            return factory.apply(baked, transform);
        };
    }

    public static BakedModel parseModel(JsonElement j, ModelBaker modelBaker, Function<Material, TextureAtlasSprite> spriteGetter, ModelState transform, ResourceLocation location) {
        BlockModel model;
        if (j.isJsonPrimitive()) {
            model = (BlockModel) modelBaker.getModel(ResourceLocation.tryParse(j.getAsString()));
        } else {
            model = ClientHelper.parseBlockModel(j);
        }
        model.resolveParents(modelBaker::getModel);
        if (model == modelBaker.getModel(ModelBakery.MISSING_MODEL_LOCATION)) {
            throw new JsonParseException("Found missing model while parsing nested model " + location);
        }
        return model.bake(modelBaker, model, spriteGetter, transform, location, true);
    }

}
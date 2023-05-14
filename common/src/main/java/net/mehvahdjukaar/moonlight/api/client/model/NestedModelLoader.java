package net.mehvahdjukaar.moonlight.api.client.model;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import net.mehvahdjukaar.moonlight.api.platform.ClientHelper;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.*;
import net.minecraft.resources.ResourceLocation;

import java.util.function.Function;

/**
 * Simple implementation of a dynamic model that accepts another model as a parameter
 */
public class NestedModelLoader implements CustomModelLoader {

    private final Function<BakedModel, CustomBakedModel> factory;
    private final String path;

    public NestedModelLoader(String modelPath, Function<BakedModel, CustomBakedModel> bakedModelFactory) {
        this.factory = bakedModelFactory;
        this.path = modelPath;
    }

    @Override
    public CustomGeometry deserialize(JsonObject json, JsonDeserializationContext context) throws JsonParseException {
        var j = json.get(path);
        if(j.isJsonPrimitive()){
            return new GeometryIndirect(ResourceLocation.tryParse(j.getAsString()));
        }else{
            return new GeometryDirect(ClientHelper.parseBlockModel(j));
        }
    }

    private class GeometryIndirect implements CustomGeometry {

        private final ResourceLocation modelLoc;

        private GeometryIndirect(ResourceLocation model) {
            this.modelLoc = model;
        }

        @Override
        public CustomBakedModel bake(ModelBaker modelBaker, Function<Material, TextureAtlasSprite> spriteGetter, ModelState transform, ResourceLocation location) {
            UnbakedModel model = modelBaker.getModel(modelLoc);
            return getCustomBakedModel(modelBaker, spriteGetter, transform, location, model, modelLoc);
        }
    }

    private class GeometryDirect implements CustomGeometry {

        private final UnbakedModel model;

        private GeometryDirect(UnbakedModel model) {
            this.model = model;
        }

        @Override
        public CustomBakedModel bake(ModelBaker modelBaker, Function<Material, TextureAtlasSprite> spriteGetter, ModelState transform, ResourceLocation location) {
            return getCustomBakedModel(modelBaker, spriteGetter, transform, location, model, location);
        }
    }
    private CustomBakedModel getCustomBakedModel(ModelBaker modelBaker, Function<Material, TextureAtlasSprite> spriteGetter, ModelState transform, ResourceLocation location, UnbakedModel model, ResourceLocation modelLoc) {
        model.resolveParents(modelBaker::getModel);
        BakedModel bakedModel = model.bake(modelBaker, spriteGetter, transform, modelLoc);
        if (model == modelBaker.getModel(ModelBakery.MISSING_MODEL_LOCATION)) {
            throw new JsonParseException("Found missing model for location " + modelLoc + " while parsing nested model " + location);
        }
        return NestedModelLoader.this.factory.apply(bakedModel);
    }
}

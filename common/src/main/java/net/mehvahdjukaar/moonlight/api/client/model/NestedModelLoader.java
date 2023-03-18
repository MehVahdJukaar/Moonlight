package net.mehvahdjukaar.moonlight.api.client.model;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import net.mehvahdjukaar.moonlight.api.platform.CPlatHelper;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.*;
import net.minecraft.resources.ResourceLocation;

import java.util.function.Function;

/**
 * Simple implementation of a dynamic model that accepts another model as a parameter
 */
public class NestedModelLoader implements CustomModelLoader{

    private final Function<BakedModel, CustomBakedModel> factory;
    private final String path;

    public NestedModelLoader(String modelPath, Function<BakedModel, CustomBakedModel> bakedModelFactory){
        this.factory = bakedModelFactory;
        this.path = modelPath;
    }

    @Override
    public CustomGeometry deserialize(JsonObject json, JsonDeserializationContext context) throws JsonParseException {
        return new Geometry(CPlatHelper.parseBlockModel(json.get(path)));
    }

    private class Geometry implements CustomGeometry{

        private final BlockModel model;

        private Geometry(BlockModel model){
            this.model = model;
        }

        @Override
        public CustomBakedModel bake(ModelBakery modelBakery, Function<Material, TextureAtlasSprite> spriteGetter, ModelState transform, ResourceLocation location) {
            BakedModel bakedModel = this.model.bake(modelBakery, model, spriteGetter, transform, location, true);
            return NestedModelLoader.this.factory.apply(bakedModel);
        }
    }
}

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

import java.util.function.Function;

public interface CustomModelLoader {

    CustomGeometry deserialize(JsonObject json, JsonDeserializationContext context) throws JsonParseException;


    // helper function you'll likely only need here
    static BakedModel parseModel(JsonElement j, ModelBaker modelBaker, Function<Material, TextureAtlasSprite> spriteGetter, ModelState transform, ResourceLocation location) {
        BlockModel model;
        if (j.isJsonPrimitive()) {
            model = (BlockModel) modelBaker.getModel(ResourceLocation.parse(j.getAsString()));
        } else {
            model = ClientHelper.parseBlockModel(j);
        }
        model.resolveParents(modelBaker::getModel);
        if (model == modelBaker.getModel(ModelBakery.MISSING_MODEL_LOCATION)) {
            throw new JsonParseException("Found missing model while parsing nested model " + location);
        }

        return model.bake(modelBaker, spriteGetter, transform);
    }
}

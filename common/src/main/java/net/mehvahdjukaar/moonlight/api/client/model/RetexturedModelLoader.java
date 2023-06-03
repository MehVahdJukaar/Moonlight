package net.mehvahdjukaar.moonlight.api.client.model;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.mojang.datafixers.util.Either;
import net.minecraft.client.renderer.block.model.BlockElement;
import net.minecraft.client.renderer.block.model.BlockElementFace;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.*;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

/**
 * Simple implementation of a dynamic model that accepts another model as a parameter
 */
public class RetexturedModelLoader implements CustomModelLoader {

    private final Function<BakedModel, CustomBakedModel> factory;
    private final String path;

    public RetexturedModelLoader(String modelPath, Function<BakedModel, CustomBakedModel> bakedModelFactory) {
        this.factory = bakedModelFactory;
        this.path = modelPath;
    }

    @Override
    public CustomGeometry deserialize(JsonObject json, JsonDeserializationContext context) throws JsonParseException {
        String string = GsonHelper.getAsString(json, "copy_from");
        return new Geometry(ResourceLocation.tryParse(string));
    }


    private static class Geometry implements CustomGeometry {

        private final ResourceLocation modelLoc;

        private Geometry(ResourceLocation model) {
            this.modelLoc = model;
        }

        @Override
        public CustomBakedModel bake(ModelBaker modelBaker, Function<Material, TextureAtlasSprite> spriteGetter, ModelState transform, ResourceLocation location) {
            UnbakedModel model = modelBaker.getModel(modelLoc);
            model.resolveParents(modelBaker::getModel);
            if(model instanceof BlockModel bm) {
                getSpriteOrder(bm);
            }

            return new RetexturedModel()
            return getCustomBakedModel(modelBaker, spriteGetter, transform, location, model, modelLoc);
        }


        private final List<String> spriteOrder = new ArrayList<>();
        private final List<String> spriteOrderUnculled = new ArrayList<>();


        public void getSpriteOrder(BlockModel model) {
            for (BlockElement blockElement : model.getElements()) {
                for (Direction direction : blockElement.faces.keySet()) {
                    BlockElementFace blockElementFace = blockElement.faces.get(direction);
                    if (blockElementFace.cullForDirection == null) {
                        spriteOrderUnculled.add(blockElementFace.texture);
                    }else{
                        spriteOrder.add(blockElementFace.texture);
                    }
                }
            }
        }
    }

}

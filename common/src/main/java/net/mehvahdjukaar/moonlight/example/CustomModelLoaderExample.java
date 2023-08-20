package net.mehvahdjukaar.moonlight.example;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import net.mehvahdjukaar.moonlight.api.client.model.*;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

public class CustomModelLoaderExample implements CustomModelLoader {
    @Override
    public CustomGeometry deserialize(JsonObject json, JsonDeserializationContext context) throws JsonParseException {
        JsonElement innerModel1 = json.get("inner_model");
        boolean prop = json.get("is_translated").getAsBoolean();
        return (modelBaker, spriteGetter, transform, location) -> {
            var innerModel = CustomModelLoader.parseModel(innerModel1,modelBaker, spriteGetter, transform,location );
            return new CustomBakedModelExample(innerModel, prop, transform);
        };
    }


    // simple cross loader custom baked model implementation
    public static class CustomBakedModelExample implements CustomBakedModel{

        private final BakedModel innerModel;
        private final boolean translated;
        private final ModelState modelState;

        public CustomBakedModelExample(BakedModel innerModel, boolean prop, ModelState modelState) {
            this.innerModel = innerModel;
            this.translated = prop;
            this.modelState = modelState;
        }

        @Override
        public List<BakedQuad> getBlockQuads(BlockState state, Direction direction, RandomSource randomSource, RenderType renderType, ExtraModelData extraModelData) {
            return null;
        }

        @Override
        public TextureAtlasSprite getBlockParticle(ExtraModelData extraModelData) {
            return innerModel.getParticleIcon();
        }

        @Override
        public boolean useAmbientOcclusion() {
            return true;
        }

        @Override
        public boolean isGui3d() {
            return false;
        }

        @Override
        public boolean usesBlockLight() {
            return false;
        }

        @Override
        public boolean isCustomRenderer() {
            return true;
        }

        @Override
        public ItemTransforms getTransforms() {
            return ItemTransforms.NO_TRANSFORMS;
        }

        @Override
        public ItemOverrides getOverrides() {
            return ItemOverrides.EMPTY;
        }
    }
}

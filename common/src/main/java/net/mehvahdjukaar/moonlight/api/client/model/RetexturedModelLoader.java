package net.mehvahdjukaar.moonlight.api.client.model;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import net.minecraft.client.renderer.block.model.BlockElement;
import net.minecraft.client.renderer.block.model.BlockElementFace;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.*;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.level.block.Block;

import java.util.*;
import java.util.function.Function;

/**
 * Simple implementation of a dynamic model that accepts another model as a parameter
 */
public class RetexturedModelLoader implements CustomModelLoader {

    public RetexturedModelLoader() {
    }

    @Override
    public CustomGeometry deserialize(JsonObject json, JsonDeserializationContext context) throws JsonParseException {
        ResourceLocation parentLoc = ResourceLocation.tryParse(GsonHelper.getAsString(json, "parent_model"));
        ResourceLocation parent = ResourceLocation.tryParse(GsonHelper.getAsString(json, "parent_block"));
        var b = BuiltInRegistries.BLOCK.getOptional(parent);
        if (b.isEmpty()) throw new JsonParseException("Could not find block with id " + parent);
        return new Geometry(b.get(), parentLoc);
    }

    private static class Geometry implements CustomGeometry {

        private final ResourceLocation parentLoc;
        private final Block parentBlock;

        private Geometry(Block parentBlock, ResourceLocation model) {
            this.parentLoc = model;
            this.parentBlock = parentBlock;
        }

        @Override
        public CustomBakedModel bake(ModelBaker modelBaker, Function<Material, TextureAtlasSprite> spriteGetter, ModelState transform, ResourceLocation location) {
            BlockModel myModel = (BlockModel) modelBaker.getModel(location); // cursed
            UnbakedModel parentModel = modelBaker.getModel(parentLoc);
            parentModel.resolveParents(modelBaker::getModel);
            Map<Direction, List<String>> spriteOrder = new EnumMap<>(Direction.class);
            if (parentModel instanceof BlockModel bm) {
                for (BlockElement blockElement : bm.getElements()) {
                    for (Direction direction : blockElement.faces.keySet()) {
                        BlockElementFace blockElementFace = blockElement.faces.get(direction);
                        if (blockElementFace.cullForDirection == null) {
                            spriteOrder.computeIfAbsent(null, d -> new ArrayList<>())
                                    .add(blockElementFace.texture);
                        } else {
                            spriteOrder.computeIfAbsent(direction, d -> new ArrayList<>())
                                    .add(blockElementFace.texture);
                        }
                    }
                }
            }
            Function<String, TextureAtlasSprite> newSpriteResolver = s-> spriteGetter.apply(myModel.getMaterial(s));
            return new RetexturedModel(parentBlock, newSpriteResolver, spriteOrder, myModel.hasAmbientOcclusion());
        }
    }

}

package net.mehvahdjukaar.moonlight.example;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.mojang.blaze3d.vertex.PoseStack;
import net.mehvahdjukaar.moonlight.api.client.model.*;
import net.mehvahdjukaar.moonlight.api.client.util.VertexUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.List;

public class CustomModelLoaderExample implements CustomModelLoader {
    @Override
    public CustomGeometry deserialize(JsonObject json, JsonDeserializationContext context) throws JsonParseException {
        JsonElement innerModel1 = json.get("inner_model");
        boolean prop = json.get("is_translated").getAsBoolean();
        return (modelBaker, spriteGetter, transform, location) -> {
            var innerModel = CustomModelLoader.parseModel(innerModel1, modelBaker, spriteGetter, transform, location);
            return new CustomBakedModelExample(innerModel, prop, transform);
        };
    }


    // simple cross loader custom baked model implementation
    public static class CustomBakedModelExample implements CustomBakedModel {

        private final BakedModel innerModel;
        private final boolean translated;
        private final ModelState modelState;

        public CustomBakedModelExample(BakedModel innerModel, boolean prop, ModelState modelState) {
            this.innerModel = innerModel;
            this.translated = prop;
            this.modelState = modelState;
        }

        // specify your model behavior here
        @Override
        public List<BakedQuad> getBlockQuads(BlockState state, Direction direction, RandomSource randomSource,
                                             RenderType renderType, ExtraModelData extraModelData) {
            List<BakedQuad> quads = new ArrayList<>();
            if (translated) {
                List<BakedQuad> original = innerModel.getQuads(state, direction, randomSource);

                // we use this to transform existing quads. its better if you store it in a class field so it wont be created every time
                BakedQuadsTransformer transformer = BakedQuadsTransformer.create();
                Matrix4f mat = new Matrix4f();
                mat.translate(0, 1, 0);
                // changing model sprite
                transformer.applyingSprite(Minecraft.getInstance().getPaintingTextures().getBackSprite());
                transformer.applyingTransform(mat);
                transformer.applyingEmissivity(2);
                // applying the transformation
                List<BakedQuad> transformed = transformer.transformAll(original);
                quads.addAll(transformed);
            }
            //only adding to translucent render type. remember to register this block to render on multiple layers
            if (renderType == RenderType.translucent()) {

                //showing off baked quad builder use to create new quads
                BakedQuadBuilder builder = BakedQuadBuilder.create(getParticleIcon());
                builder.setAutoDirection();
                //needed so all finished quads will be added to the list
                builder.setAutoBuild(quads::add);
                //you can also use this helper with vanilla vertex consumers in your entity renderers
                PoseStack pose = new PoseStack();
                pose.translate(0,0,1);
                //adding a simple quad using helper function. You can also add vertices manually
                VertexUtil.addQuad(builder,pose,0, 0, 0.2f, 0.2f, 0,0);
            }

            // accessing extra model data
            Block data = extraModelData.get(TrollBlockEntity.MIMIC_BLOCK_KEY);
            if(data != null){
                BakedModel blockModel = Minecraft.getInstance().getBlockRenderer().getBlockModel(data.defaultBlockState());
                //adds blocks quads to the model
                quads.addAll(blockModel.getQuads(data.defaultBlockState(),direction, randomSource));
            }

            return quads;
        }

        // you can override this method to get the data of your block.
        // By default, the implementation just grabs it from the tile entiy at pos if it implements IExtraModelDataProvider
        @Override
        public ExtraModelData getModelData(@NotNull ExtraModelData tileData, BlockPos pos, BlockState state, BlockAndTintGetter level) {
            return CustomBakedModel.super.getModelData(tileData, pos, state, level);
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


    // for an easy implementation like this you can also use the class MimicBlockTile
    public static class TrollBlockEntity extends BlockEntity implements IExtraModelDataProvider{

        public static final ModelDataKey<Block> MIMIC_BLOCK_KEY = new ModelDataKey<>(Block.class);

        private Block mimic;

        public TrollBlockEntity(BlockEntityType<?> blockEntityType, BlockPos blockPos, BlockState blockState) {
            super(blockEntityType, blockPos, blockState);
        }

        @Override
        public ExtraModelData getExtraModelData() {
            // this is the data that will be passed to the custom model
            // this just wraps Forge system
            return ExtraModelData.builder()
                    .with(MIMIC_BLOCK_KEY, mimic)
                    .build();
        }

        public void toggle(){
            this.mimic = this.level.random.nextBoolean() ? Blocks.STONE : Blocks.DIAMOND_ORE;
            this.requestModelReload();
        }
    }
}

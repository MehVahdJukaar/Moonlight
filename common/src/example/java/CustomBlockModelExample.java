import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.mehvahdjukaar.moonlight.api.client.model.CustomBlockModel;
import net.mehvahdjukaar.moonlight.api.client.model.CustomUnbakedModel;
import net.mehvahdjukaar.moonlight.api.client.model.ExtraModelData;
import net.mehvahdjukaar.moonlight.api.client.model.ModelDataKey;
import net.mehvahdjukaar.moonlight.api.client.model.QuadEmitter;
import net.mehvahdjukaar.moonlight.api.client.model.IExtraModelDataProvider;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ResolvableModel;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

/**
 * Wraps another model and pastes a mimicked block's geometry on top of it.
 * Blockstate json (neoforge reads "type", fabric reads "fabric:type"):
 * <pre>
 * "": { "type": "moonlight:custom_model", "fabric:type": "moonlight:custom_model",
 *       "model": { "model": "moonlight:block/troll" }, "tinted": true }
 * </pre>
 */
public class CustomBlockModelExample implements CustomBlockModel {

    private final BlockStateModel inner;
    private final boolean tinted;

    public CustomBlockModelExample(BlockStateModel inner, boolean tinted) {
        this.inner = inner;
        this.tinted = tinted;
    }

    @Override
    public void emitQuads(QuadEmitter emitter, @Nullable BlockAndTintGetter level, @Nullable BlockPos pos,
                          @Nullable BlockState state, RandomSource random, ExtraModelData data) {
        if (this.tinted) {
            for (BakedQuad q : CustomBlockModel.collectQuads(this.inner, level, pos, state, random)) {
                emitter.fromQuad(q).color(0xFFFF8800).lightEmission(7).emit();
            }
        } else {
            emitter.emitAll(this.inner, level, pos, state, random);
        }

        Block mimic = data.get(TrollBlockEntity.MIMIC_BLOCK_KEY);
        if (mimic != null && level != null) {
            BlockState mimicState = mimic.defaultBlockState();
            BlockStateModel mimicModel = Minecraft.getInstance().getBlockRenderer().getBlockModel(mimicState);
            emitter.emitAll(mimicModel, level, pos, mimicState, random);
        }
    }

    @Override
    public TextureAtlasSprite getParticle(ExtraModelData data) {
        return this.inner.particleMaterial().sprite();
    }

    // loaders reuse the mesh between blocks with the same key. Keep it cheap, no level or pos in here
    @Override
    public @Nullable Object geometryKey(BlockAndTintGetter level, BlockPos pos, BlockState state,
                                        RandomSource random, ExtraModelData data) {
        return data.get(TrollBlockEntity.MIMIC_BLOCK_KEY);
    }

    public record Unbaked(BlockStateModel.Unbaked model, boolean tinted) implements CustomUnbakedModel {

        public static final MapCodec<Unbaked> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
                BlockStateModel.Unbaked.CODEC.fieldOf("model").forGetter(Unbaked::model),
                Codec.BOOL.optionalFieldOf("tinted", false).forGetter(Unbaked::tinted)
        ).apply(i, Unbaked::new));

        @Override
        public CustomBlockModel bake(ModelBaker baker) {
            return new CustomBlockModelExample(this.model.bake(baker), this.tinted);
        }

        @Override
        public MapCodec<? extends CustomUnbakedModel> codec() {
            return CODEC;
        }

        // nested models will never resolve their parents unless this is forwarded
        @Override
        public void resolveDependencies(ResolvableModel.Resolver resolver) {
            this.model.resolveDependencies(resolver);
        }
    }

    // for an easy implementation like this you can also use the class MimicBlockTile
    public static class TrollBlockEntity extends BlockEntity implements IExtraModelDataProvider {

        public static final ModelDataKey<Block> MIMIC_BLOCK_KEY = new ModelDataKey<>(Block.class);

        private Block mimic;

        public TrollBlockEntity(BlockEntityType<?> blockEntityType, BlockPos blockPos, BlockState blockState) {
            super(blockEntityType, blockPos, blockState);
        }

        @Override
        public void addExtraModelData(ExtraModelData.Builder builder) {
            // whatever goes in here is read from the meshing threads, so it must be immutable
            builder.with(MIMIC_BLOCK_KEY, this.mimic);
        }

        public void toggle() {
            this.mimic = this.level.getRandom().nextBoolean() ? Blocks.STONE : Blocks.DIAMOND_ORE;
            this.requestModelReload();
        }
    }
}

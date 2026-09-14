package net.mehvahdjukaar.moonlight.api.client.model.platform;

import net.fabricmc.fabric.api.renderer.v1.Renderer;
import net.fabricmc.fabric.api.renderer.v1.RendererAccess;
import net.fabricmc.fabric.api.renderer.v1.material.RenderMaterial;
import net.fabricmc.fabric.api.renderer.v1.mesh.QuadEmitter;
import net.fabricmc.fabric.api.renderer.v1.model.FabricBakedModel;
import net.fabricmc.fabric.api.renderer.v1.model.ModelHelper;
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext;
import net.fabricmc.fabric.api.util.TriState;
import net.mehvahdjukaar.moonlight.api.client.model.CustomBakedModel;
import net.mehvahdjukaar.moonlight.api.client.model.ExtraModelData;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Supplier;

//needed cause fabric
public final class ModelWrapper implements BakedModel, FabricBakedModel {

    private final CustomBakedModel father;

    @Nullable
    private final ExtraModelData data;

    public ModelWrapper(CustomBakedModel father, @Nullable ExtraModelData data) {
        this.father = father;
        this.data = data;
    }

    private static RenderMaterial standard;
    private static RenderMaterial noAmbientOcclusion;
    private static RenderMaterial emissive;

    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState blockState, @Nullable Direction direction, RandomSource randomSource) {
        return father.getBlockQuads(blockState, direction, randomSource, RenderType.cutout(), data);
    }

    @Override
    public boolean isVanillaAdapter() {
        return false;
    }

    /**
     * Same walk fabric's own VanillaModelEncoder does, except each quad picks its own material so the
     * ones marked emissive keep their light. Going through the encoder would flatten them all onto one.
     */
    @Override
    public void emitBlockQuads(BlockAndTintGetter level, BlockState state, BlockPos pos,
                               Supplier<RandomSource> random, RenderContext context) {
        setupMaterials();
        QuadEmitter emitter = context.getEmitter();
        RenderMaterial base = useAmbientOcclusion() ? standard : noAmbientOcclusion;

        for (int i = 0; i <= ModelHelper.NULL_FACE_ID; i++) {
            Direction cullFace = ModelHelper.faceFromIndex(i);
            if (!context.hasTransform() && context.isFaceCulled(cullFace)) continue;

            for (BakedQuad quad : getQuads(state, cullFace, random.get())) {
                emitter.fromVanilla(quad, quad instanceof EmissiveBakedQuad ? emissive : base, cullFace);
                emitter.emit();
            }
        }
    }

    @Override
    public void emitItemQuads(ItemStack stack, Supplier<RandomSource> random, RenderContext context) {
        setupMaterials();
        QuadEmitter emitter = context.getEmitter();

        for (int i = 0; i <= ModelHelper.NULL_FACE_ID; i++) {
            Direction cullFace = ModelHelper.faceFromIndex(i);
            for (BakedQuad quad : getQuads(null, cullFace, random.get())) {
                emitter.fromVanilla(quad, quad instanceof EmissiveBakedQuad ? emissive : standard, cullFace);
                emitter.emit();
            }
        }
    }

    private static void setupMaterials() {
        if (standard != null) return;
        Renderer renderer = RendererAccess.INSTANCE.getRenderer();
        standard = renderer.materialFinder().find();
        noAmbientOcclusion = renderer.materialFinder().ambientOcclusion(TriState.FALSE).find();
        emissive = renderer.materialFinder().emissive(true).find();
    }

    @Override
    public boolean useAmbientOcclusion() {
        return father.useAmbientOcclusion();
    }

    @Override
    public boolean isGui3d() {
        return father.isGui3d();
    }

    @Override
    public boolean usesBlockLight() {
        return father.usesBlockLight();
    }

    @Override
    public boolean isCustomRenderer() {
        return father.isCustomRenderer();
    }

    @Override
    public TextureAtlasSprite getParticleIcon() {
        return father.getBlockParticle(data);
    }

    @Override
    public ItemTransforms getTransforms() {
        return father.getTransforms();
    }

    @Override
    public ItemOverrides getOverrides() {
        return father.getOverrides();
    }
}

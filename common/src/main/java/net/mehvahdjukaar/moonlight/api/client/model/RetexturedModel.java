package net.mehvahdjukaar.moonlight.api.client.model;

import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import net.mehvahdjukaar.moonlight.api.client.util.VertexUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockModelShaper;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.block.model.multipart.MultiPart;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

public class RetexturedModel implements CustomBakedModel {

    private static final Map<Block, RetexturedModel> parentCache = new Object2ObjectLinkedOpenHashMap<>();

    //TODO: reload this
    private static final Map<BlockState, BakedModel> modelCache = new IdentityHashMap<>();

    public static RetexturedModel getInstance(Block block){
        return parentCache.computeIfAbsent(block, RetexturedModel::new);
    }

    private final Block parent;

    private RetexturedModel(Block parent) {
        this.parent = parent;
        MultiPart
    }

    @Override
    public List<BakedQuad> getBlockQuads(BlockState state, Direction side, RandomSource rand, RenderType renderType, ExtraModelData extraModelData) {

        var shaper = Minecraft.getInstance().getModelManager().getBlockModelShaper();


        BlockState parentState = parent.withPropertiesOf(state);
        var parentModel = shaper.getBlockModel(parentState);

        try {
            var supportQuads = parentModel.getQuads(parentState, side, rand);
            if (!supportQuads.isEmpty()) {
                if (sprite != null) {
                    supportQuads = VertexUtil.swapSprite(supportQuads, sprite);
                }
                quads.addAll(supportQuads);
            }

        } catch (Exception ignored) {
        }
    }

    @Override
    public TextureAtlasSprite getBlockParticle(ExtraModelData extraModelData) {
        return null;
    }

    @Override
    public boolean useAmbientOcclusion() {
        return false;
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
        return false;
    }

    @Override
    public ItemTransforms getTransforms() {
        return null;
    }

    @Override
    public ItemOverrides getOverrides() {
        return null;
    }
}

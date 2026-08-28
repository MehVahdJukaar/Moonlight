package net.mehvahdjukaar.moonlight.api.client.model;

import net.mehvahdjukaar.moonlight.api.platform.ClientHelper;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * A block model that builds its geometry from the world or the block entity data.
 * Emit all quads at once, the render layer of each one comes from its sprite (see QuadEmitter.forceTranslucent).
 */
public interface CustomBlockModel {

    /**
     * Runs on the chunk meshing threads: don't touch the level or block entities, read data instead.
     * Level, pos and state are null when baked for an item.
     */
    void emitQuads(QuadEmitter emitter, @Nullable BlockAndTintGetter level, @Nullable BlockPos pos,
                   @Nullable BlockState state, RandomSource random, ExtraModelData data);

    TextureAtlasSprite getParticle(ExtraModelData data);

    /** Key both loaders cache the mesh by. Null disables caching. */
    @Nullable
    default Object geometryKey(BlockAndTintGetter level, BlockPos pos, BlockState state, RandomSource random,
                               ExtraModelData data) {
        return null;
    }

    static List<BakedQuad> collectQuads(BlockStateModel model, @Nullable BlockAndTintGetter level,
                                        @Nullable BlockPos pos, @Nullable BlockState state, RandomSource random) {
        List<BlockStateModelPart> parts = new ArrayList<>();
        ClientHelper.collectModelParts(model, level, pos, state, random, parts);
        List<BakedQuad> quads = new ArrayList<>();
        for (BlockStateModelPart part : parts) {
            for (Direction dir : Direction.values()) quads.addAll(part.getQuads(dir));
            quads.addAll(part.getQuads(null));
        }
        return quads;
    }
}

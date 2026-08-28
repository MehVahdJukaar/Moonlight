package net.mehvahdjukaar.moonlight.api.client.model.platform;

import net.mehvahdjukaar.moonlight.api.client.model.CollectedModelPart;
import net.mehvahdjukaar.moonlight.api.client.model.CustomBlockModel;
import net.mehvahdjukaar.moonlight.api.client.model.ExtraModelData;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public record CustomBlockModelWrapper(CustomBlockModel inner) implements BlockStateModel {

    @Override
    public void collectParts(BlockAndTintGetter level, BlockPos pos, BlockState state, RandomSource random,
                             List<BlockStateModelPart> parts) {
        parts.add(this.buildPart(level, pos, state, random, dataAt(level, pos)));
    }

    @Override
    public void collectParts(RandomSource random, List<BlockStateModelPart> parts) {
        parts.add(this.buildPart(null, null, null, random, ExtraModelData.EMPTY));
    }

    @Override
    public @Nullable Object createGeometryKey(BlockAndTintGetter level, BlockPos pos, BlockState state,
                                              RandomSource random) {
        return this.inner.geometryKey(level, pos, state, random, dataAt(level, pos));
    }

    @Override
    public Material.Baked particleMaterial(BlockAndTintGetter level, BlockPos pos, BlockState state) {
        return particle(this.inner, dataAt(level, pos));
    }

    @Override
    public Material.Baked particleMaterial() {
        return particle(this.inner, ExtraModelData.EMPTY);
    }

    @Override
    public int materialFlags() {
        // only reachable through the level-less path, where we have no quads to look at yet
        return 0;
    }

    private CollectedModelPart buildPart(@Nullable BlockAndTintGetter level, @Nullable BlockPos pos,
                                         @Nullable BlockState state, RandomSource random, ExtraModelData data) {
        CollectedModelPart part = new CollectedModelPart(particle(this.inner, data));
        this.inner.emitQuads(new QuadEmitterImpl(part::bucket), level, pos, state, random, data);
        part.computeFlags();
        return part;
    }

    private static ExtraModelData dataAt(BlockAndTintGetter level, BlockPos pos) {
        return ExtraModelDataImpl.of(level.getModelData(pos));
    }

    private static Material.Baked particle(CustomBlockModel model, ExtraModelData data) {
        return new Material.Baked(model.getParticle(data), false);
    }
}

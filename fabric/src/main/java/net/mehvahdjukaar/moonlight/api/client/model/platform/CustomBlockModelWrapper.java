package net.mehvahdjukaar.moonlight.api.client.model.platform;

import net.fabricmc.fabric.api.blockgetter.v2.FabricBlockGetter;
import net.fabricmc.fabric.api.client.renderer.v1.model.FabricBlockStateModel;
import net.mehvahdjukaar.moonlight.api.client.model.CollectedModelPart;
import net.mehvahdjukaar.moonlight.api.client.model.CustomBlockModel;
import net.mehvahdjukaar.moonlight.api.client.model.ExtraModelData;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Predicate;

public record CustomBlockModelWrapper(CustomBlockModel inner) implements BlockStateModel, FabricBlockStateModel {

    @Override
    public void emitQuads(net.fabricmc.fabric.api.client.renderer.v1.mesh.QuadEmitter emitter,
                          BlockAndTintGetter level, BlockPos pos, BlockState state, RandomSource random,
                          Predicate<@Nullable Direction> cullTest) {
        this.inner.emitQuads(new QuadEmitterImpl(emitter), level, pos, state, random, dataAt(level, pos));
    }

    // vanilla path (block items, particles). per vertex colors are lost here
    @Override
    public void collectParts(RandomSource random, List<BlockStateModelPart> parts) {
        CollectedModelPart part = new CollectedModelPart(particle(this.inner, ExtraModelData.EMPTY));
        this.inner.emitQuads(part.emitter(), null, null, null, random, ExtraModelData.EMPTY);
        part.computeFlags();
        parts.add(part);
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
        return 0;
    }

    private static ExtraModelData dataAt(BlockAndTintGetter level, BlockPos pos) {
        Object data = ((FabricBlockGetter) level).getBlockEntityRenderData(pos);
        return data instanceof ExtraModelData d ? d : ExtraModelData.EMPTY;
    }

    private static Material.Baked particle(CustomBlockModel model, ExtraModelData data) {
        return new Material.Baked(model.getParticle(data), false);
    }
}

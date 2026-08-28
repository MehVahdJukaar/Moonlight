package net.mehvahdjukaar.moonlight.api.client.model.platform;

import net.fabricmc.fabric.api.client.renderer.v1.mesh.MutableQuadView;
import net.fabricmc.fabric.api.client.renderer.v1.model.FabricBlockStateModel;
import net.fabricmc.fabric.api.util.TriState;
import net.mehvahdjukaar.moonlight.api.client.model.QuadEmitter;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.BlockPos;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class QuadEmitterImpl extends QuadEmitter {

    private final net.fabricmc.fabric.api.client.renderer.v1.mesh.QuadEmitter target;

    public QuadEmitterImpl(net.fabricmc.fabric.api.client.renderer.v1.mesh.QuadEmitter target) {
        this.target = target;
    }

    @Override
    protected void pushQuad() {
        for (int i = 0; i < VERTICES; i++) {
            this.target.pos(i, this.positions[i]);
            this.target.uv(i, this.us[i], this.vs[i]);
            this.target.color(i, this.colors[i]);
            if (this.hasNormals) this.target.normal(i, this.normals[i]);
        }
        this.target.nominalFace(this.lightFace);
        this.target.cullFace(this.cullFace);
        this.target.tintIndex(this.tintIndex);
        this.target.diffuseShade(this.shade);
        this.target.ambientOcclusion(this.ambientOcclusion ? TriState.DEFAULT : TriState.FALSE);
        // bakes the 0 to 1 uvs onto the sprite, so it has to come after them
        this.target.materialBake(new Material.Baked(this.sprite, this.forceTranslucent), MutableQuadView.BAKE_NORMALIZED);
        if (this.lightEmission > 0) {
            this.target.minLightmap(LightCoordsUtil.pack(this.lightEmission, this.lightEmission));
        }
        this.target.emit();
    }

    @Override
    public QuadEmitter emitAll(BlockStateModel model, @Nullable BlockAndTintGetter level, @Nullable BlockPos pos,
                               @Nullable BlockState state, RandomSource random) {
        if (level == null || pos == null || state == null) {
            return super.emitAll(model, level, pos, state, random);
        }
        ((FabricBlockStateModel) model).emitQuads(this.target, level, pos, state, random, dir -> false);
        return this;
    }
}

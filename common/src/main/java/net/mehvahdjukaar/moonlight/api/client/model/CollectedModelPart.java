package net.mehvahdjukaar.moonlight.api.client.model;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.Direction;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public final class CollectedModelPart implements BlockStateModelPart {

    private static final int NO_CULL = 6;

    private final List<BakedQuad>[] byCullFace;
    private final Material.Baked particle;
    private int materialFlags = 0;
    private boolean ambientOcclusion = true;

    @SuppressWarnings("unchecked")
    public CollectedModelPart(Material.Baked particle) {
        this.particle = particle;
        this.byCullFace = new List[NO_CULL + 1];
        for (int i = 0; i <= NO_CULL; i++) this.byCullFace[i] = new ObjectArrayList<>();
    }

    public List<BakedQuad> bucket(@Nullable Direction cullFace) {
        return this.byCullFace[cullFace == null ? NO_CULL : cullFace.ordinal()];
    }

    public BakedQuadEmitter emitter() {
        return new BakedQuadEmitter(this::bucket) {
            @Override
            protected void pushQuad() {
                // vanilla only has a per part flag, so one quad opting out turns it off for all
                CollectedModelPart.this.ambientOcclusion &= this.ambientOcclusion;
                super.pushQuad();
            }
        };
    }

    public void computeFlags() {
        int flags = 0;
        for (List<BakedQuad> bucket : this.byCullFace) {
            for (BakedQuad q : bucket) flags |= q.materialInfo().flags();
        }
        this.materialFlags = flags;
    }

    @Override
    public List<BakedQuad> getQuads(@Nullable Direction direction) {
        return this.bucket(direction);
    }

    @Override
    public boolean useAmbientOcclusion() {
        return this.ambientOcclusion;
    }

    @Override
    public Material.Baked particleMaterial() {
        return this.particle;
    }

    @Override
    public int materialFlags() {
        return this.materialFlags;
    }
}

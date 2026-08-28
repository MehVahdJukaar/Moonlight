package net.mehvahdjukaar.moonlight.api.client.model;

import com.mojang.blaze3d.platform.Transparency;
import net.minecraft.client.model.geom.builders.UVPair;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.Direction;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import java.util.List;
import java.util.function.Function;

/** Emits plain vanilla BakedQuads. Per vertex colors and normals are dropped. */
public class BakedQuadEmitter extends QuadEmitter {

    private final Function<@Nullable Direction, List<BakedQuad>> byCullFace;

    public BakedQuadEmitter(Function<@Nullable Direction, List<BakedQuad>> byCullFace) {
        this.byCullFace = byCullFace;
    }

    @Override
    protected void pushQuad() {
        Transparency transparency = this.forceTranslucent ? Transparency.TRANSLUCENT : this.sprite.transparency();
        // vanilla keeps ambient occlusion on the part, not the quad, so it is not passed here
        var material = BakedQuad.MaterialInfo.of(new Material.Baked(this.sprite, this.forceTranslucent),
                transparency, this.tintIndex, this.shade, this.lightEmission);
        this.byCullFace.apply(this.cullFace).add(new BakedQuad(
                new Vector3f(this.positions[0]), new Vector3f(this.positions[1]),
                new Vector3f(this.positions[2]), new Vector3f(this.positions[3]),
                this.packUv(0), this.packUv(1), this.packUv(2), this.packUv(3),
                this.lightFace, material));
    }

    private long packUv(int vertex) {
        return UVPair.pack(this.sprite.getU(this.us[vertex]), this.sprite.getV(this.vs[vertex]));
    }
}

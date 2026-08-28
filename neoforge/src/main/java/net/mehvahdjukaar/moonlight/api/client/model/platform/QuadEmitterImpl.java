package net.mehvahdjukaar.moonlight.api.client.model.platform;

import net.mehvahdjukaar.moonlight.api.client.model.QuadEmitter;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.Direction;
import net.neoforged.neoforge.client.model.quad.MutableQuad;

import java.util.List;
import java.util.function.Function;

public class QuadEmitterImpl extends QuadEmitter {

    private final MutableQuad quad = new MutableQuad();
    private final Function<Direction, List<BakedQuad>> byCullFace;

    public QuadEmitterImpl(Function<Direction, List<BakedQuad>> byCullFace) {
        this.byCullFace = byCullFace;
    }

    @Override
    protected void pushQuad() {
        this.quad.reset();
        this.quad.setDirection(this.lightFace);
        this.quad.setSprite(new Material.Baked(this.sprite, this.forceTranslucent));
        for (int i = 0; i < VERTICES; i++) {
            this.quad.setPosition(i, this.positions[i]);
            this.quad.setUvFromSprite(i, this.us[i], this.vs[i]);
            this.quad.setColor(i, this.colors[i]);
            if (this.hasNormals) this.quad.setNormal(i, this.normals[i]);
        }
        this.quad.setTintIndex(this.tintIndex)
                .setShade(this.shade)
                .setLightEmission(this.lightEmission)
                .setAmbientOcclusion(this.ambientOcclusion);
        this.byCullFace.apply(this.cullFace).add(this.quad.toBakedQuad());
    }
}

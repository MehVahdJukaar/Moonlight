package net.mehvahdjukaar.moonlight.api.client.model.platform;

import net.minecraft.client.renderer.block.model.BakedQuad;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public class EmissiveBakedQuad extends BakedQuad {

    private final int lightEmission;

    public EmissiveBakedQuad(BakedQuad quad, int lightEmission) {
        super(quad.getVertices(), quad.getTintIndex(), quad.getDirection(), quad.getSprite(), quad.isShade());
        this.lightEmission = lightEmission;
    }

    public int lightEmission() {
        return lightEmission;
    }
}

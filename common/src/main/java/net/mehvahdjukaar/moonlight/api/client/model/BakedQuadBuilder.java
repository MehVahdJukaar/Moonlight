package net.mehvahdjukaar.moonlight.api.client.model;

import com.mojang.math.Transformation;
import dev.architectury.injectables.annotations.ExpectPlatform;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

/**
 * Cross loader utility to create baked quad
 */
public interface BakedQuadBuilder {

    static BakedQuadBuilder create() {
        return create(null);
    }

    @ExpectPlatform
    static BakedQuadBuilder create(@Nullable Transformation transformation) {
        throw new AssertionError();
    }

    BakedQuadBuilder setSprite(TextureAtlasSprite sprite);

    BakedQuadBuilder setDirection(Direction direction);

    BakedQuadBuilder setAmbientOcclusion(boolean ambientOcclusion);

    BakedQuadBuilder setShade(boolean shade);

    BakedQuadBuilder pos(float x, float y, float z);

    default BakedQuadBuilder pos(Vector3f vec3) {
        return pos(vec3.x(), vec3.y(), vec3.z());
    }

    BakedQuadBuilder normal(float x, float y, float z);

    default BakedQuadBuilder normal(Vector3f vector3f) {
        return normal(vector3f.x(), vector3f.y(), vector3f.z());
    }

    BakedQuadBuilder color(int rgba);


    BakedQuadBuilder uv(float u, float v);

    default BakedQuadBuilder spriteUV(TextureAtlasSprite sprite, float u, float v) {
        return uv(sprite.getU(u), sprite.getV(v)).setSprite(sprite);
    }

    BakedQuadBuilder lightEmission(int light);

    BakedQuadBuilder endVertex();

    BakedQuadBuilder fromVanilla(BakedQuad quad);

    BakedQuad build();


}

package net.mehvahdjukaar.moonlight.api.client.model.forge;

import com.google.common.base.Preconditions;
import com.mojang.math.Transformation;
import net.mehvahdjukaar.moonlight.api.client.model.BakedQuadBuilder;
import net.mehvahdjukaar.moonlight.api.client.util.RotHlpr;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraftforge.client.model.QuadTransformers;
import net.minecraftforge.client.model.pipeline.QuadBakingVertexConsumer;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;

public class BakedQuadBuilderImpl implements BakedQuadBuilder {

    public static BakedQuadBuilder create(@Nullable Transformation transformation) {
        return new BakedQuadBuilderImpl(transformation == null ? null : transformation.getMatrix());
    }

    private final QuadBakingVertexConsumer inner;
    private int emissivity = 0;
    private BakedQuad output;
    private Matrix4f transform;

    private BakedQuadBuilderImpl(@Nullable Matrix4f transformation) {
        this.inner = new QuadBakingVertexConsumer(s -> this.output = s);
        this.transform = transformation;
        inner.setShade(true);
        inner.setHasAmbientOcclusion(true);
    }

    @Override
    public BakedQuadBuilder setSprite(TextureAtlasSprite sprite) {
        inner.setSprite(sprite);
        return this;
    }

    @Override
    public BakedQuadBuilder setShade(boolean shade) {
        inner.setShade(shade);
        return this;
    }

    @Override
    public BakedQuadBuilder setAmbientOcclusion(boolean ambientOcclusion) {
        inner.setHasAmbientOcclusion(ambientOcclusion);
        return this;
    }

    public BakedQuadBuilder setDirection(Direction direction) {
        if (transform != null) {
            direction = RotHlpr.rotateDirection(direction, transform);
        }
        inner.setDirection(direction);
        return this;
    }

    @Override
    public BakedQuadBuilder pos(float x, float y, float z) {
        inner.vertex(x, y, z);
        return this;
    }

    @Override
    public BakedQuadBuilder normal(float x, float y, float z) {
        inner.normal(x, y, z);
        return this;
    }

    @Override
    public BakedQuadBuilder lightEmission(int lightLevel) {
        this.emissivity = lightLevel;
        return this;
    }

    @Override
    public BakedQuadBuilder color(int rgba) {
        inner.color(rgba);
        return this;
    }

    @Override
    public BakedQuadBuilder uv(float u, float v) {
        inner.uv(u, v);
        return this;
    }

    @Override
    public BakedQuadBuilder endVertex() {
        inner.endVertex();
        return this;
    }

    public BakedQuadBuilder fromVanilla(BakedQuad quad){
        output = quad;
        return this;
    }

    @Override
    public BakedQuad build() {
        Preconditions.checkNotNull(output, "vertex data has not been fully filled");
        if (transform != null) {
            QuadTransformers.applying(new Transformation(transform).blockCenterToCorner()).processInPlace(output);
        }
        if (emissivity != 0) {
            QuadTransformers.settingEmissivity(emissivity).processInPlace(output);
        }
        return output;
    }

    public BakedQuadBuilder useTransform(Matrix4f matrix4f) {
        this.transform = matrix4f;
        return this;
    }


}

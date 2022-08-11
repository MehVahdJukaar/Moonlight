package net.mehvahdjukaar.moonlight.api.client.model.forge;

import com.google.common.base.Preconditions;
import com.google.common.collect.UnmodifiableIterator;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormatElement;
import com.mojang.math.Matrix4f;
import net.mehvahdjukaar.moonlight.api.client.model.BakedQuadBuilder;
import net.minecraft.Util;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraftforge.client.model.IModelBuilder;
import net.minecraftforge.client.model.pipeline.QuadBakingVertexConsumer;

import java.util.IdentityHashMap;
import java.util.Map;

public class BakedQuadBuilderImpl implements BakedQuadBuilder {

    public static BakedQuadBuilder create() {
        return new BakedQuadBuilderImpl();
    }

    private final QuadBakingVertexConsumer inner;
    private BakedQuad output;
    private Matrix4f transform = null;

    private BakedQuadBuilderImpl() {
        this.inner = new QuadBakingVertexConsumer(s -> this.output = s);
    }

    @Override
    public BakedQuadBuilder setSprite(TextureAtlasSprite sprite) {
        inner.setSprite(sprite);
        return null;
    }

    public BakedQuadBuilder setDirection(Direction direction) {
        if (transform != null) {
            var normal = direction.getNormal();
            var v = BakedQuadBuilder.applyModelRotation(normal.getX(), normal.getY(), normal.getZ(), transform);
            inner.setDirection(Direction.getNearest(v.x(), v.y(), v.z()));
            return this;
        }
        inner.setDirection(direction);
        return this;
    }

    @Override
    public BakedQuadBuilder pos(float x, float y, float z) {
        if (transform != null) {
            var v = BakedQuadBuilder.applyModelRotation(x, y, z, transform);
            inner.vertex(v.x(), v.y(), v.z());
            return this;
        }
        inner.vertex(x, y, z);
        return this;
    }

    @Override
    public BakedQuadBuilder normal(float x, float y, float z) {
        if (transform != null) {
            var v = BakedQuadBuilder.applyModelRotation(x, y, z, transform);
            inner.normal(v.x(), v.y(), v.z());
            return this;
        }
        inner.normal(x, y, z);
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

    @Override
    public BakedQuad build() {
        Preconditions.checkNotNull(output, "vertex data has not been fully filled");
        return output;
    }

    public BakedQuadBuilder useTransform(Matrix4f matrix4f) {
        this.transform = matrix4f;
        return this;
    }
}

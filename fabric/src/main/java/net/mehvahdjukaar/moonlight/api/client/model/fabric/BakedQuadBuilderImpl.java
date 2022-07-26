package net.mehvahdjukaar.moonlight.api.client.model.fabric;

import com.google.common.base.Preconditions;
import com.mojang.math.Matrix4f;
import net.fabricmc.fabric.api.renderer.v1.RendererAccess;
import net.fabricmc.fabric.api.renderer.v1.mesh.MeshBuilder;
import net.fabricmc.fabric.api.renderer.v1.mesh.MutableQuadView;
import net.fabricmc.fabric.api.renderer.v1.mesh.QuadEmitter;
import net.mehvahdjukaar.moonlight.api.client.model.BakedQuadBuilder;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;

public class BakedQuadBuilderImpl implements BakedQuadBuilder {

    public static BakedQuadBuilder create() {
        return new BakedQuadBuilderImpl();
    }

    private final QuadEmitter inner;
    private int vertexIndex = 0;
    private Matrix4f transform = null;
    TextureAtlasSprite sprite = null;

    private BakedQuadBuilderImpl() {
        MeshBuilder meshBuilder = RendererAccess.INSTANCE.getRenderer().meshBuilder();
        this.inner = meshBuilder.getEmitter();
    }

    @Override
    public BakedQuadBuilder setSprite(TextureAtlasSprite sprite) {
        this.sprite = sprite;
        inner.spriteBake(0, sprite, MutableQuadView.BAKE_LOCK_UV);
        return null;
    }

    public BakedQuadBuilder setDirection(Direction direction) {
        if (transform != null) {
            var normal = direction.getNormal();
            var v = BakedQuadBuilder.applyModelRotation(normal.getX(), normal.getY(), normal.getZ(), transform);
            inner.nominalFace(Direction.getNearest(v.x(), v.y(), v.z()));
            return this;
        }
        inner.nominalFace(direction);
        return this;
    }

    @Override
    public BakedQuadBuilder pos(float x, float y, float z) {
        if (transform != null) {
            var v = BakedQuadBuilder.applyModelRotation(x, y, z, transform);
            inner.pos(vertexIndex, v.x(), v.y(), v.z());
            return this;
        }
        inner.pos(vertexIndex, x, y, z);
        return this;
    }

    @Override
    public BakedQuadBuilder normal(float x, float y, float z) {
        if (transform != null) {
            var v = BakedQuadBuilder.applyModelRotation(x, y, z, transform);
            inner.normal(vertexIndex, v.x(), v.y(), v.z());
            return this;
        }
        inner.normal(vertexIndex, x, y, z);
        return this;
    }

    @Override
    public BakedQuadBuilder color(int rgba) {
        inner.spriteColor(vertexIndex, 0, rgba);
        return this;
    }

    @Override
    public BakedQuadBuilder uv(float u, float v) {
        inner.sprite(vertexIndex, 0, u, v);
        return this;
    }

    @Override
    public BakedQuadBuilder endVertex() {
        vertexIndex++;
        return this;
    }

    @Override
    public BakedQuad build() {
        Preconditions.checkNotNull(sprite, "sprite cannot be null");
        return inner.toBakedQuad(0, sprite, false);
    }

    public BakedQuadBuilder useTransform(Matrix4f matrix4f) {
        this.transform = matrix4f;
        return this;
    }
}


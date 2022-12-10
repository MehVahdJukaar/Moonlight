package net.mehvahdjukaar.moonlight.api.client.model.fabric;

import com.google.common.base.Preconditions;
import com.mojang.math.Transformation;
import net.fabricmc.fabric.api.renderer.v1.RendererAccess;
import net.fabricmc.fabric.api.renderer.v1.mesh.MeshBuilder;
import net.fabricmc.fabric.api.renderer.v1.mesh.MutableQuadView;
import net.fabricmc.fabric.api.renderer.v1.mesh.QuadEmitter;
import net.mehvahdjukaar.moonlight.api.client.model.BakedQuadBuilder;
import net.mehvahdjukaar.moonlight.api.client.util.RotHlpr;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Vector3f;

public class BakedQuadBuilderImpl implements BakedQuadBuilder {

    public static BakedQuadBuilder create(@Nullable Transformation transformation) {
        return new BakedQuadBuilderImpl(transformation == null ? null : transformation.getMatrix());
    }

    private final QuadEmitter inner;
    private int vertexIndex = 0;
    private Matrix4f transform;
    TextureAtlasSprite sprite = null;

    private BakedQuadBuilderImpl(@Nullable Matrix4f transform) {
        MeshBuilder meshBuilder = RendererAccess.INSTANCE.getRenderer().meshBuilder();
        this.inner = meshBuilder.getEmitter();
        this.transform = transform;
    }

    @Override
    public BakedQuadBuilder setSprite(TextureAtlasSprite sprite) {
        this.sprite = sprite;
        inner.spriteBake(0, sprite, MutableQuadView.BAKE_LOCK_UV);
        return null;
    }

    @Override
    public BakedQuadBuilder setShade(boolean shade) {
        return this;
    }

    @Override
    public BakedQuadBuilder setAmbientOcclusion(boolean ambientOcclusion) {
        return this;
    }

    public BakedQuadBuilder setDirection(Direction direction) {
        if (transform != null) {
            direction = RotHlpr.rotateDirection(direction, transform);
        }
        inner.nominalFace(direction);
        return this;
    }


    @Override
    public BakedQuadBuilder pos(float x, float y, float z) {
        if (transform != null) {
            var v = RotHlpr.rotateVertexOnCenterBy(x, y, z, transform);
            inner.pos(vertexIndex, v.x(), v.y(), v.z());
            return this;
        }
        inner.pos(vertexIndex, x, y, z);
        return this;
    }

    @Override
    public BakedQuadBuilder normal(float x, float y, float z) {
        if (transform != null) {
            Vector3f normal = new Vector3f(x, y, z);
            RotHlpr.rotateVertexBy(normal, new Vector3f(), transform);
            inner.normal(vertexIndex, normal.x(), normal.y(), normal.z());
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
    public BakedQuadBuilder lightEmission(int lightLevel) {
        inner.material(RendererAccess.INSTANCE.getRenderer().materialFinder().emissive(0, true).find());
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

    @Deprecated(forRemoval = true)
    public BakedQuadBuilder useTransform(Matrix4f matrix4f) {
        this.transform = matrix4f;
        return this;
    }

}


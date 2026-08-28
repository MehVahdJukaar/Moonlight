package net.mehvahdjukaar.moonlight.api.client.model;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;

/** Recorded quads, replayed with QuadEmitter.emitAll. Keeps per vertex colors and normals, unlike a BakedQuad list. */
public final class QuadBatch {

    private final List<Recorded> quads;

    private QuadBatch(List<Recorded> quads) {
        this.quads = quads;
    }

    public boolean isEmpty() {
        return this.quads.isEmpty();
    }

    public int size() {
        return this.quads.size();
    }

    public static Recorder recorder() {
        return new Recorder();
    }

    void replay(QuadEmitter target) {
        for (Recorded q : this.quads) {
            q.loadInto(target);
            target.emit();
        }
    }

    public static final class Recorder extends QuadEmitter {

        private final List<Recorded> quads = new ArrayList<>();

        private Recorder() {
        }

        @Override
        protected void pushQuad() {
            this.quads.add(Recorded.of(this));
        }

        public QuadBatch build() {
            return new QuadBatch(List.copyOf(this.quads));
        }
    }

    private record Recorded(Vector3f[] positions, float[] us, float[] vs, int[] colors,
                            Vector3f @Nullable [] normals, TextureAtlasSprite sprite,
                            @Nullable Direction cullFace, Direction lightFace, int tintIndex,
                            int lightEmission, boolean shade, boolean ambientOcclusion,
                            boolean forceTranslucent) {

        static Recorded of(QuadEmitter e) {
            Vector3f[] positions = new Vector3f[QuadEmitter.VERTICES];
            Vector3f[] normals = e.hasNormals ? new Vector3f[QuadEmitter.VERTICES] : null;
            for (int i = 0; i < QuadEmitter.VERTICES; i++) {
                positions[i] = new Vector3f(e.positions[i]);
                if (normals != null) normals[i] = new Vector3f(e.normals[i]);
            }
            return new Recorded(positions, e.us.clone(), e.vs.clone(), e.colors.clone(), normals,
                    e.sprite, e.cullFace, e.lightFace, e.tintIndex, e.lightEmission, e.shade,
                    e.ambientOcclusion, e.forceTranslucent);
        }

        void loadInto(QuadEmitter e) {
            for (int i = 0; i < QuadEmitter.VERTICES; i++) {
                e.positions[i].set(this.positions[i]);
                e.us[i] = this.us[i];
                e.vs[i] = this.vs[i];
                e.colors[i] = this.colors[i];
                if (this.normals != null) e.normals[i].set(this.normals[i]);
            }
            e.hasNormals = this.normals != null;
            e.sprite = this.sprite;
            e.cullFace = this.cullFace;
            e.lightFace = this.lightFace;
            e.tintIndex = this.tintIndex;
            e.lightEmission = this.lightEmission;
            e.shade = this.shade;
            e.ambientOcclusion = this.ambientOcclusion;
            e.forceTranslucent = this.forceTranslucent;
        }
    }
}

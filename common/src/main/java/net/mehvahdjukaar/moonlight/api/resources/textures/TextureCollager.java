package net.mehvahdjukaar.moonlight.api.resources.textures;

import net.mehvahdjukaar.moonlight.api.resources.textures.Palette;
import net.mehvahdjukaar.moonlight.api.resources.textures.PaletteColor;
import net.mehvahdjukaar.moonlight.api.resources.textures.Sampler2D;
import net.mehvahdjukaar.moonlight.api.resources.textures.TextureImage;
import net.minecraft.world.level.block.Rotation;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class TextureCollager {
    protected final int originFrameW;
    protected final int originFrameH;
    protected final int targetFrameW;
    protected final int targetFrameH;
    private final List<Operation> operations;

    public void apply(TextureImage source, TextureImage destination) {

        float scaleSourceX = source.frameWidth() / (float) originFrameW;
        float scaleSourceY = source.frameHeight() / (float) originFrameH;
        float scaleTargetX = destination.frameWidth() / (float) targetFrameW;
        float scaleTargetY = destination.frameHeight() / (float) targetFrameH;

        int sourceFrames = source.frameCount();
        int targetFrames = destination.frameCount();
        int maxFrames = Math.max(sourceFrames, targetFrames);

        for (int i = 0; i < maxFrames; i++) {
            int cappedSourceFrame = Math.min(i, sourceFrames - 1);
            int cappedTargetFrame = Math.min(i, targetFrames - 1);

            Sampler2D sourceFrameSampler = source.frameSampler(cappedSourceFrame);

            for (Operation op : operations) {

                int scaledSourceX = Math.round(op.sourceX * scaleSourceX);
                int scaledSourceY = Math.round(op.sourceY * scaleSourceY);
                int scaledSourceW = Math.round(op.sourceW * scaleSourceX);
                int scaledSourceH = Math.round(op.sourceH * scaleSourceY);

                int scaledTargetX = Math.round(op.targetX * scaleTargetX);
                int scaledTargetY = Math.round(op.targetY * scaleTargetY);
                int scaledTargetW = Math.round(op.targetW * scaleTargetX);
                int scaledTargetH = Math.round(op.targetH * scaleTargetY);

                Sampler2D sampler = Sampler2D.offset(
                        sourceFrameSampler,
                        scaledSourceX,
                        scaledSourceY
                );

                sampler = Sampler2D.clamp(sampler, scaledSourceW, scaledSourceH);

                if (op.bilinear) {
                    sampler = Sampler2D.bilinear(sampler);
                }

                // ----- FLIP IN PIXEL SPACE -----
                int flipW = scaledSourceW;
                int flipH = scaledSourceH;
                if (op.rotation == Rotation.CLOCKWISE_90
                        || op.rotation == Rotation.COUNTERCLOCKWISE_90) {
                    flipW = scaledSourceH;
                    flipH = scaledSourceW;
                }

                if (op.flipX) {
                    sampler = Sampler2D.flippedX(sampler, flipW);
                }
                if (op.flipY) {
                    sampler = Sampler2D.flippedY(sampler, flipH);
                }

                // ----- ROTATION -----
                if (op.rotation != Rotation.NONE) {
                    sampler = Sampler2D.rotate(
                            sampler,
                            op.rotation,
                            scaledSourceW,
                            scaledSourceH
                    );
                }

                // ----- MOVE TO PIXEL CENTERS -----
                sampler = Sampler2D.offset(sampler, -0.5f, -0.5f);

                float opScaleX = scaledSourceW / (float) scaledTargetW;
                float opScaleY = scaledSourceH / (float) scaledTargetH;

                if (opScaleX != 1.0f || opScaleY != 1.0f) {
                    sampler = Sampler2D.scale(sampler, opScaleX, opScaleY);
                }

                sampler = Sampler2D.offset(sampler, 0.5f, 0.5f);

                int actualW = Math.min(
                        scaledTargetW,
                        destination.frameWidth() - scaledTargetX
                );
                int actualH = Math.min(
                        scaledTargetH,
                        destination.frameHeight() - scaledTargetY
                );

                for (int ty = 0; ty < actualH; ty++) {
                    for (int tx = 0; tx < actualW; tx++) {

                        float srcX = (tx + 0.5f) * opScaleX;
                        float srcY = (ty + 0.5f) * opScaleY;

                        srcX = Math.min(srcX, scaledSourceW - 0.5f);
                        srcY = Math.min(srcY, scaledSourceH - 0.5f);

                        int color = sampler.sample(srcX, srcY);

                        if (op.palettes != null) {
                            int maxPaletteIndex =
                                    Math.min(source.frameCount(), op.palettes.size() - 1);
                            color = op.palettes
                                    .get(maxPaletteIndex)
                                    .getColorClosestTo(new PaletteColor(color))
                                    .value();
                        }

                        if (op.blended) {
                            destination.blendFramePixel(
                                    cappedTargetFrame,
                                    scaledTargetX + tx,
                                    scaledTargetY + ty,
                                    color
                            );
                        } else {
                            destination.setFramePixel(
                                    cappedTargetFrame,
                                    scaledTargetX + tx,
                                    scaledTargetY + ty,
                                    color
                            );
                        }
                    }
                }
            }
        }
    }


    private TextureCollager(int originalW, int originalH, int targetW, int targetH, List<Operation> list) {
        this.originFrameW = originalW;
        this.originFrameH = originalH;
        this.targetFrameW = targetW;
        this.targetFrameH = targetH;
        this.operations = list;
    }

    //needed to support packs of different resolutions
    public static Builder builder(int originFrameW, int originFrameH, int targetFrameW, int targetFrameH) {
        return new Builder(originFrameW, originFrameH, targetFrameW, targetFrameH);
    }

    private record Operation(int sourceX, int sourceY, int sourceW, int sourceH,
                             int targetX, int targetY, int targetW, int targetH,
                             boolean flipX, boolean flipY, Rotation rotation, boolean bilinear,
                             boolean blended, @Nullable List<Palette> palettes) {
    }

    public static class Builder {
        private final int originalFrameW, originalFrameH, targetFrameW, targetFrameH;
        private final List<Operation> operations = new ArrayList<>();

        private Integer fromX, fromY, fromW, fromH;
        private Integer targetX, targetY, targetW, targetH;
        private boolean flipX = false, flipY = false;
        private Rotation rotation = Rotation.NONE;
        private boolean bilinear = false;
        private boolean blended = false;
        @Nullable
        private List<Palette> palettes = null;

        public Builder(int originalW, int originalH, int targetW, int targetH) {
            this.originalFrameW = originalW;
            this.originalFrameH = originalH;
            this.targetFrameW = targetW;
            this.targetFrameH = targetH;
        }

        public TextureCollager build() {
            addLast();
            return new TextureCollager(originalFrameW, originalFrameH, targetFrameW, targetFrameH, List.copyOf(operations));
        }

        public Builder copyFrom(int x, int y, int w, int h) {
            addLast();
            this.fromX = x;
            this.fromY = y;
            this.fromW = w;
            this.fromH = h;
            this.targetH = fromH;
            this.targetW = fromW;
            return this;
        }

        public Builder to(int x, int y, int w, int h) {
            to(x, y);
            this.targetW = w;
            this.targetH = h;
            return this;
        }

        public Builder to(int x, int y) {
            this.targetX = x;
            this.targetY = y;
            return this;
        }

        public Builder flippedX() {
            this.flipX = true;
            return this;
        }

        public Builder flippedY() {
            this.flipY = true;
            return this;
        }

        public Builder rotated(Rotation r) {
            this.rotation = r == null ? Rotation.NONE : r;
            return this;
        }

        public Builder blended() {
            this.blended = true;
            return this;
        }

        public Builder paletted(List<Palette> palettes) {
            this.palettes = palettes;
            return this;
        }

        public Builder bilinearScaling() {
            this.bilinear = true;
            return this;
        }


        private void addLast() {
            if (targetX == null) return;
            validate();

            // Handle automatic dimension adjustment for rotations
            if (targetW == null || targetH == null) {
                // Check if rotation swaps dimensions
                boolean dimensionsSwapped = rotation == Rotation.CLOCKWISE_90 ||
                        rotation == Rotation.COUNTERCLOCKWISE_90;

                if (targetW == null) {
                    targetW = dimensionsSwapped ? fromH : fromW;
                }
                if (targetH == null) {
                    targetH = dimensionsSwapped ? fromW : fromH;
                }
            }

            // Add operation to parent builder list
            operations.add(new Operation(
                    fromX, fromY, fromW, fromH,
                    targetX, targetY, targetW, targetH,
                    flipX, flipY, rotation,
                    bilinear, blended, palettes));

            // Clear operation state for next operation
            fromX = fromY = fromW = fromH = null;
            targetX = targetY = null;
            targetW = targetH = null;
            flipX = flipY = false;
            rotation = Rotation.NONE;
            bilinear = false;
            blended = false;
            palettes = null;
        }

        private void validate() {
            if (fromX == null) throw new IllegalStateException("sourceX must be set");
            if (fromY == null) throw new IllegalStateException("sourceY must be set");
            if (fromW == null) throw new IllegalStateException("sourceW must be set");
            if (fromH == null) throw new IllegalStateException("sourceH must be set");
            if (targetX == null) throw new IllegalStateException("targetX must be set");
            if (targetY == null) throw new IllegalStateException("targetY must be set");

            if (fromX < 0 || fromX + fromW > originalFrameW)
                throw new IllegalArgumentException("Source rectangle out of bounds: fromX");
            if (fromY < 0 || fromY + fromH > originalFrameH)
                throw new IllegalArgumentException("Source rectangle out of bounds: fromY");
            if (targetX < 0 || targetX + targetW > targetFrameW)
                throw new IllegalArgumentException("Target rectangle out of bounds: targetX");
            if (targetY < 0 || targetY + targetH > targetFrameH)
                throw new IllegalArgumentException("Target rectangle out of bounds: targetY");

        }
    }
}

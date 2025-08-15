package net.mehvahdjukaar.moonlight.api.resources.textures;

import net.minecraft.world.level.block.Rotation;

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
            sourceFrameSampler = Sampler2D.scale(sourceFrameSampler, scaleSourceX, scaleSourceY);
            for (Operation op : operations) {

                Sampler2D sampler = op.makeSampler(sourceFrameSampler);


                //we sample in target space now since original scaling sampler takes care of it
                for (int ty = 0; ty < op.targetH; ty++) {
                    for (int tx = 0; tx < op.targetW; tx++) {
                        int color = sampler.sample(tx, ty);
                        destination.setFramePixel(cappedTargetFrame,
                                (int) ((op.targetX + tx) * scaleTargetX),
                                (int) ((op.targetY + ty) * scaleTargetY),
                                color);
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
                             boolean flipX, boolean flipY, Rotation rotation, boolean bilinear) {


        private Sampler2D makeSampler(Sampler2D sampler) {

            sampler = Sampler2D.offset(sampler, sourceX, sourceY);
            //   sampler = Sampler2D.clamp(sampler, 0, 0, sourceW, sourceH);

            if (bilinear) {
                sampler = Sampler2D.bilinear(sampler);
            }

            sampler = Sampler2D.offset(sampler, -0.5f, -0.5f);

            if (flipX) {
                // Width & sourceH may swap after rotation
                int w = (rotation == Rotation.CLOCKWISE_90 || rotation == Rotation.COUNTERCLOCKWISE_90) ? sourceH : sourceW;
                sampler = Sampler2D.flippedX(sampler, w);
            }

            // Add flip Y support - you might want a new boolean flag, e.g. flippedY
            if (flipY) {  // <-- You’ll need to add this boolean to your Operation class
                int h = (rotation == Rotation.CLOCKWISE_90 || rotation == Rotation.COUNTERCLOCKWISE_90) ? sourceW : sourceH;
                sampler = Sampler2D.flippedY(sampler, h);
            }

            if (rotation != Rotation.NONE) {
                sampler = Sampler2D.rotate(sampler, rotation, sourceW, sourceH);
            }

            sampler = Sampler2D.offset(sampler, 0.5f, 0.5f);

            float opScaleW = sourceW / (float) targetW;
            float opScaleH = sourceH / (float) targetH;

            if (opScaleW != 1 || opScaleH != 1) {
                sampler = Sampler2D.scale(sampler, opScaleW, opScaleH);
            }


            return sampler;
        }
    }

    public static class Builder {
        private final int originalFrameW, originalFrameH, targetFrameW, targetFrameH;
        private final List<Operation> operations = new ArrayList<>();

        private Integer fromX, fromY, fromW, fromH;
        private Integer targetX, targetY, targetW, targetH;
        private boolean flipX = false, flipY = false;
        private Rotation rotation = Rotation.NONE;
        private boolean bilinear = false;

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

        public Builder bilinearScaling() {
            this.bilinear = true;
            return this;
        }


        private void addLast() {
            if (targetX == null) return;
            validate();
            // Default target size
            if (targetW == null) targetW = fromW;
            if (targetH == null) targetH = fromH;

            // Add operation to parent builder list
            operations.add(new Operation(
                    fromX, fromY, fromW, fromH,
                    targetX, targetY, targetW, targetH,
                    flipX, flipY, rotation, bilinear));

            //clear
            fromX = fromY = fromW = fromH = null;
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

package net.mehvahdjukaar.moonlight.api.resources.textures;

import net.minecraft.world.level.block.Rotation;

import java.util.ArrayList;
import java.util.List;

public class TextureCollager {
    protected final int originalW;
    protected final int originalH;
    protected final int targetW;
    protected final int targetH;
    private final List<Operation> operations;

    public void apply(TextureImage source, TextureImage destination) {
        float scaleSourceX = source.imageWidth() / (float) originalW;
        float scaleSourceY = source.imageHeight() / (float) originalH;
        float scaleTargetX = destination.imageWidth() / (float) targetW;
        float scaleTargetY = destination.imageHeight() / (float) targetH;

        Sampler2D sourceScaleSampler = Sampler2D.scale(source,
                scaleSourceX, scaleSourceY);


        for (Operation op : operations) {

            Sampler2D sampler = Sampler2D.offset(sourceScaleSampler, op.startX, op.startY);


            // Apply rotation
            if (op.rotation != Rotation.NONE) {
                sampler = Sampler2D.rotate(sampler, op.rotation, op.width, op.height);
            }

            // Apply flip
            if (op.flipX) {
                // Width & height may swap after rotation
                int w = (op.rotation == Rotation.CLOCKWISE_90 || op.rotation == Rotation.COUNTERCLOCKWISE_90) ? op.height : op.width;
                sampler = Sampler2D.flippedX(sampler, w);
            }

            // Add flip Y support - you might want a new boolean flag, e.g. op.flippedY
            if (op.flipY) {  // <-- You’ll need to add this boolean to your Operation class
                int h = (op.rotation == Rotation.CLOCKWISE_90 || op.rotation == Rotation.COUNTERCLOCKWISE_90) ? op.width : op.height;
                sampler = Sampler2D.flippedY(sampler, h);
            }

            sampler = Sampler2D.scale(sampler, op.width, op.height, op.targetW, op.targetH);

            if (op.bilinear) {
                sampler = Sampler2D.bilinear(sampler);
            }

            for (int ty = 0; ty < op.targetH; ty++) {
                for (int tx = 0; tx < op.targetW; tx++) {
                    int color = sampler.sample(tx, ty);
                    destination.setPixel(
                            (int) ((op.targetX + tx) * scaleTargetX),
                            (int) ((op.targetY + ty) * scaleTargetY),
                            color);
                }
            }
            // Write to destination
            for (int ty = 0; ty < op.targetH; ty++) {
                for (int tx = 0; tx < op.targetW; tx++) {
                    int color = sampler.sample(tx, ty);
                    destination.setPixel(op.targetX + tx, op.targetY + ty, color);
                }
            }
        }
    }


    private TextureCollager(int originalW, int originalH, int targetW, int targetH, List<Operation> list) {
        this.originalW = originalW;
        this.originalH = originalH;
        this.targetW = targetW;
        this.targetH = targetH;
        this.operations = list;
    }

    //needed to support packs of different resolutions
    public static Builder builder(int originalW, int originalH, int targetW, int targetH) {
        return new Builder(originalW, originalH, targetW, targetH);
    }

    private record Operation(int startX, int startY, int width, int height, int targetX, int targetY, int targetW,
                             int targetH, boolean flipX, boolean flipY, Rotation rotation, boolean bilinear) {

    }

    public static class Builder {
        private final int originalImageW, originalImageH, targetImageW, targetImageH;
        private final List<Operation> operations = new ArrayList<>();

        private Integer startX, startY, width, height;
        private Integer targetX, targetY, targetW, targetH;
        private boolean flipX = false, flipY = false;
        private Rotation rotation = Rotation.NONE;
        private boolean bilinear = false;

        public Builder(int originalW, int originalH, int targetW, int targetH) {
            this.originalImageW = originalW;
            this.originalImageH = originalH;
            this.targetImageW = targetW;
            this.targetImageH = targetH;
        }

        public TextureCollager build() {
            addLast();
            return new TextureCollager(originalImageW, originalImageH, targetImageW, targetImageH, List.copyOf(operations));
        }

        public Builder copyFrom(int x, int y, int w, int h) {
            addLast();
            this.startX = x;
            this.startY = y;
            this.width = w;
            this.height = h;
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
            if (targetW == null) targetW = width;
            if (targetH == null) targetH = height;

            // Add operation to parent builder list
            operations.add(new Operation(
                    startX, startY, width, height,
                    targetX, targetY, targetW, targetH,
                    flipX, flipY, rotation, bilinear));

            //clear
            startX = startY = width = height = null;
        }

        private void validate() {
            if (startX == null) throw new IllegalStateException("startX must be set");
            if (startY == null) throw new IllegalStateException("startY must be set");
            if (width == null) throw new IllegalStateException("width must be set");
            if (height == null) throw new IllegalStateException("height must be set");
            if (targetX == null) throw new IllegalStateException("targetX must be set");
            if (targetY == null) throw new IllegalStateException("targetY must be set");

            if (startX < 0 || startX + width > originalImageW)
                throw new IllegalArgumentException("Source rectangle out of bounds");
            if (startY < 0 || startY + height > originalImageH)
                throw new IllegalArgumentException("Source rectangle out of bounds");
            if (targetX < 0 || targetX + targetW > targetImageW)
                throw new IllegalArgumentException("Target rectangle out of bounds");
            if (targetY < 0 || targetY + targetH > targetImageH)
                throw new IllegalArgumentException("Target rectangle out of bounds");
        }
    }
}

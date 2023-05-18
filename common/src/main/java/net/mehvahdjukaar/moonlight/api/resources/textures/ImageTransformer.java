package net.mehvahdjukaar.moonlight.api.resources.textures;

import com.google.common.base.Preconditions;

import java.util.ArrayList;
import java.util.List;

public class ImageTransformer {
    protected final int originalW;
    protected final int originalH;
    protected final int targetW;
    protected final int targetH;
    private final List<Tpos> transforms;

    public void apply(TextureImage original, TextureImage target) {
        int actualOrW = original.frameWidth();
        int actualOrH = original.frameHeight();
        int actualTarW = target.frameHeight();
        int actualTarH = target.frameHeight();
        var orIm = original.getImage();
        for (var tr : transforms) {
            var t = tr.scaled(actualOrW, actualOrH, actualTarW, actualTarH, originalW, originalH, targetW, targetH);
            original.forEachFrame((frameIndex, globalX, globalY) -> {
                int frameX = globalX - original.getFrameStartX(frameIndex);
                int frameY = globalY - original.getFrameStartX(frameIndex);
                if (frameX >= t.startX() && frameX < t.maxX() && frameY >= t.startY() && frameY < t.maxY()) {
                    int col = orIm.getPixelRGBA(globalX, globalY);
                    int targetX = t.targetX + frameX - t.startX(); //assumes the scale is the same. wont fail but results might be off
                    int targetY = t.targetY + frameY - t.startY();
                    if (targetX < actualTarW && targetY < actualTarH) {
                        target.setFramePixel(frameIndex, targetX, targetY, col);
                    }
                }
            });
        }
    }

    private ImageTransformer(int originalW, int originalH, int targetW, int targetH, List<Tpos> list) {
        this.originalW = originalW;
        this.originalH = originalH;
        this.targetW = targetW;
        this.targetH = targetH;
        this.transforms = list;
    }

    public static Builder builder(int originalW, int originalH, int targetW, int targetH) {
        return new Builder(originalW, originalH, targetW, targetH);
    }

    private record Tpos(int startX, int startY, int width, int height, int targetX, int targetY) {

        public Tpos scaled(int aOW, int aOH, int aTW, int aTH, int oW, int oH, int tW, int tH) {
            float scaleOW = aOW / (float) oW;
            float scaleOH = aOH / (float) oH;
            float scaleTW = aTW / (float) tW;
            float scaleTH = aTH / (float) tH;
            return new Tpos((int) (scaleOW * startX), (int) (scaleOH * startY), (int) (scaleOW * width), (int) (scaleOH * height),
                    (int) (scaleTW * targetX), (int) (scaleTH * targetY));
        }

        public int maxX() {
            return startX + width;
        }

        public int maxY() {
            return startY + height;
        }
    }

    public static class Builder {
        protected final int originalW;
        protected final int originalH;
        protected final int targetW;
        protected final int targetH;
        private final List<Tpos> transforms = new ArrayList<>();

        protected Builder(int originalW, int originalH, int targetW, int targetH) {
            this.originalW = originalW;
            this.originalH = originalH;
            this.targetW = targetW;
            this.targetH = targetH;
        }

        public Builder copyRect(int startX, int startY, int width, int height, int targetX, int targetY) {
            Preconditions.checkArgument(startX + width <= originalW, "Invalid dimensions: original width");
            Preconditions.checkArgument(startY + height <= originalH, "Invalid dimensions: original height");
            Preconditions.checkArgument(targetX <= targetW, "Invalid dimensions: target width");
            Preconditions.checkArgument(targetY <= targetH, "Invalid dimensions: target height");
            transforms.add(new Tpos(startX, startY, width, height, targetX, targetY));
            return this;
        }

        public ImageTransformer build() {
            return new ImageTransformer(originalW, originalH, targetW, targetH, transforms.stream().toList());
        }
    }
}

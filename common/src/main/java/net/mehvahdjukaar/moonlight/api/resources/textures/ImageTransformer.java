package net.mehvahdjukaar.moonlight.api.resources.textures;

import com.google.common.base.Preconditions;
import com.mojang.blaze3d.platform.NativeImage;
import io.netty.util.internal.UnstableApi;

import java.util.ArrayList;
import java.util.List;

public class ImageTransformer {
    protected final int originalW;
    protected final int originalH;
    protected final int targetW;
    protected final int targetH;
    private final List<Tpos> transforms;

    public void apply(TextureImage original, TextureImage target) {
        int oFrameW = original.frameWidth();
        int oFrameH = original.frameHeight();
        int tFrameW = target.frameWidth();
        int tFrameH = target.frameHeight();
        NativeImage orIm = original.getImage();
        for (Tpos tr : transforms) {
            //TODO: fix scale. use STBImageResize.nstbir_resize_uint8
            Tpos t = tr.scaled(oFrameW, oFrameH, tFrameW, tFrameH, originalW, originalH, targetW, targetH);
            original.forEachFramePixel((frameIndex, globalX, globalY) -> {
                int frameX = globalX - original.getFrameStartX(frameIndex);
                int frameY = globalY - original.getFrameStartX(frameIndex);
                if (frameX >= t.startX() && frameX < t.maxX() && frameY >= t.startY() && frameY < t.maxY()) {
                    int col = orIm.getPixelRGBA(globalX, globalY);
                    int targetX = t.targetX + frameX - t.startX(); //assumes the scale is the same. wont fail but results might be off
                    int targetY = t.targetY + frameY - t.startY();
                    if (targetX < tFrameW && targetY < tFrameH) {
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

    /**
     * @param originalW original image width
     * @param originalH original image height
     * @param targetW target image width
     * @param targetH target image height
     */
    public static Builder builder(int originalW, int originalH, int targetW, int targetH) {
        return new Builder(originalW, originalH, targetW, targetH);
    }

    private record Tpos(int startX, int startY, int width, int height, int targetX, int targetY, int targetW, int targetH) {

        //global to local (frame) pos I think
        public Tpos scaled(int oFrameW, int oFrameH, int tFrameW, int tFrameH, int oW, int oH, int tW, int tH) {
            float scaleOW = oFrameW / (float) oW; //usually 1
            float scaleOH = oFrameH / (float) oH;
            float scaleTW = tFrameW / (float) tW;
            float scaleTH = tFrameH / (float) tH;
            return new Tpos((int) (scaleOW * startX), (int) (scaleOH * startY), (int) (scaleOW * width), (int) (scaleOH * height),
                    (int) (scaleTW * targetX), (int) (scaleTH * targetY), (int) (scaleTW * targetW), (int) (scaleTH * targetH));
        }

        public int maxX() {
            return startX + width;
        }

        public int maxY() {
            return startY + height;
        }
    }

    public static class Builder {
        protected final int originalImageW;
        protected final int originalImageH;
        protected final int targetImageW;
        protected final int targetImageH;
        private final List<Tpos> transforms = new ArrayList<>();

        protected Builder(int originalW, int originalH, int targetW, int targetH) {
            this.originalImageW = originalW;
            this.originalImageH = originalH;
            this.targetImageW = targetW;
            this.targetImageH = targetH;
        }
        public Builder copyRect(int startX, int startY, int width, int height, int targetX, int targetY) {
            return copyRect(startX, startY, width, height, targetX, targetY, width, height);
        }


        @UnstableApi//not implemented
        public Builder copyRect(int startX, int startY, int width, int height, int targetX, int targetY, int targetW, int targetH) {
            Preconditions.checkArgument(startX + width <= originalImageW, "Invalid dimensions: original width");
            Preconditions.checkArgument(startY + height <= originalImageH, "Invalid dimensions: original height");
            Preconditions.checkArgument(targetX <= targetImageW, "Invalid dimensions: target width");
            Preconditions.checkArgument(targetY <= targetImageH, "Invalid dimensions: target height");
            transforms.add(new Tpos(startX, startY, width, height, targetX, targetY, targetW, targetH));
            return this;
        }

        public ImageTransformer build() {
            return new ImageTransformer(originalImageW, originalImageH, targetImageW, targetImageH, transforms.stream().toList());
        }
    }
}

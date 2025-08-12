package net.mehvahdjukaar.moonlight.api.resources.textures;

public final class PixelContext {
    private final TextureImage image;

    // updated on each pixel iteration
    int frameIndex;
    int localX, localY;
    int globalX, globalY;

    public PixelContext(TextureImage image) {
        this.image = image;
    }

    // get pixel value at current position
    public int getValue() {
        return image.getPixel(globalX, globalY);
    }

    // set pixel value at current position (mutates the image)
    public void setValue(int value) {
        image.setPixel(globalX, globalY, value);
    }

    public void blendValue(int value) {
        image.blendPixel(globalX, globalY, value);
    }

    public int frameIndex() {
        return frameIndex;
    }

    public int frameX() {
        return localX;
    }

    public int frameY() {
        return localY;
    }

    public int x() {
        return globalX;
    }

    public int y() {
        return globalY;
    }
}
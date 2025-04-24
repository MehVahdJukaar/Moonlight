package net.mehvahdjukaar.moonlight.api.resources.textures;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.mojang.blaze3d.platform.NativeImage;
import net.mehvahdjukaar.moonlight.api.resources.ResType;
import net.mehvahdjukaar.moonlight.api.util.math.colors.RGBColor;
import net.mehvahdjukaar.moonlight.core.misc.McMetaFile;
import net.minecraft.client.resources.metadata.animation.AnimationFrame;
import net.minecraft.client.resources.metadata.animation.AnimationMetadataSection;
import net.minecraft.client.resources.metadata.animation.FrameSize;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.FastColor;
import net.minecraft.world.level.block.Rotation;
import org.apache.logging.log4j.util.TriConsumer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.List;

//like a native image that also has its metadata
public class TextureImage implements AutoCloseable {

    @Nullable
    private final McMetaFile metadata;
    private final NativeImage image;
    //width of a frame
    private final FrameSize frameSize;
    //All frames. Includes unused ones
    private final int frameCount;
    private final int frameScale;


    private TextureImage(NativeImage image, @Nullable McMetaFile metadata) {
        this.image = image;
        this.metadata = metadata;
        int imgWidth = this.imageWidth(); // 16
        int imgHeight = this.imageHeight(); // 48
        this.frameSize = metadata == null ? new FrameSize(imgWidth, imgHeight) : metadata.animation()
                .calculateFrameSize(imgWidth, imgHeight);
        this.frameScale = imgWidth / frameSize.width(); // 1
        int frameScaleHeight = imgHeight / frameSize.height(); // 2
        this.frameCount = frameScale * frameScaleHeight; // 2
    }

    /**
     * Accepts a consumer that iterates over all image pixels, ordered by frame.
     * The given coordinates are global texture coordinates while the index represents the currently viewed frame
     */
    public void forEachFramePixel(FramePixelConsumer framePixelConsumer) {
        for (int ind = 0; ind < frameCount; ind++) {
            int xOff = getFrameStartX(ind);
            int yOff = getFrameStartY(ind);
            for (int x = 0; x < frameWidth(); x++) {
                for (int y = 0; y < frameHeight(); y++) {
                    framePixelConsumer.accept(ind, x + xOff, y + yOff);
                }
            }
        }
    }

    public void toGrayscale() {
        SpriteUtils.grayscaleImage(this.image);
    }

    public RGBColor getAverageColor() {
        return SpriteUtils.averageColor(this.image);
    }


    public int frameWidth() {
        return frameSize.width();
    }

    public int frameHeight() {
        return frameSize.height();
    }

    @FunctionalInterface
    public interface FramePixelConsumer extends TriConsumer<Integer, Integer, Integer> {
        @Override
        void accept(Integer frameIndex, Integer globalX, Integer globalY);
    }


    //local frame coord from global
    public int getFrameStartX(int frameIndex) {
        return (frameIndex % frameScale) * frameWidth(); //(2 % 1) * 16
    }

    public int getFrameStartY(int frameIndex) {
        return (frameIndex / frameScale) * frameHeight(); // (2/1) * 32
    }

    public int getFramePixel(int frameIndex, int x, int y) {
        return image.getPixelRGBA(getFrameStartX(frameIndex) + x, getFrameStartY(frameIndex) + y);
    }

    public void setFramePixel(int frameIndex, int x, int y, int color) {
        image.setPixelRGBA(getFrameStartX(frameIndex) + x, getFrameStartY(frameIndex) + y, color);
    }

    public NativeImage getImage() {
        return image;
    }

    public int frameCount() {
        return frameCount;
    }

    @Deprecated(forRemoval = true)
    @Nullable
    public AnimationMetadataSection getMetadata() {
        return metadata == null ? null : metadata.animation();
    }

    public McMetaFile getMcMeta() {
        return metadata;
    }

    public TextureImage makeCopy() {
        NativeImage im = new NativeImage(this.imageWidth(), this.imageHeight(), false);
        im.copyFrom(image);
        return new TextureImage(im, metadata);
    }

    /**
     * Creates a new image using the first frame of this one. Its frame data and frame length will be the one provided
     */
    public TextureImage createAnimationTemplate(int length, McMetaFile useDataFrom) {
        McMetaFile newMetadata = useDataFrom.cloneWithSize(this.frameWidth(), this.frameHeight());
        NativeImage im = new NativeImage(this.frameWidth(), this.frameHeight() * length, false);
        TextureImage t = new TextureImage(im, newMetadata);

        t.forEachFramePixel((i, x, y) -> {
            int xo = getFrameX(i, x);
            int yo = getFrameY(i, y);
            t.image.setPixelRGBA(x, y, this.image.getPixelRGBA(xo, yo));
        });
        return t;
    }

    @Deprecated(forRemoval = true)
    public TextureImage createAnimationTemplate(int length, @NotNull AnimationMetadataSection useDataFrom) {
        return createAnimationTemplate(length, McMetaFile.of(useDataFrom));
    }

    @Deprecated(forRemoval = true)
    public TextureImage createAnimationTemplate(int length, List<AnimationFrame> frameData, int frameTime, boolean interpolate) {
        return createAnimationTemplate(length, new AnimationMetadataSection(frameData, this.frameWidth(), this.frameHeight(), frameTime, interpolate));
    }

    /**
     * Opens a texture image from the given resource path. A texture image is composed of a NativeImage and its associated McMeta file
     *
     * @param manager      resource manager
     * @param relativePath relative texture path (does not include /textures)
     */
    public static TextureImage open(ResourceManager manager, ResourceLocation relativePath) throws IOException {
        try {
            if (relativePath.getPath().endsWith(".png")) {
                relativePath = relativePath.withPath(relativePath.getPath().substring(0, relativePath.getPath().length() - 4));
            }
            ResourceLocation textureLoc = ResType.TEXTURES.getPath(relativePath);
            NativeImage i = SpriteUtils.readImage(manager, textureLoc);
            //try getting metadata for animated textures
            ResourceLocation metadataLoc = ResType.MCMETA.getPath(relativePath);
            McMetaFile metadata = null;

            var res = manager.getResource(metadataLoc);
            if (res.isPresent()) {
                try {
                    metadata = McMetaFile.read(res.get());
                } catch (Exception e) {
                    throw new IOException("Failed to open texture at location " + relativePath + ": failed to read mcmeta file", e);
                }
            }

            return new TextureImage(i, metadata);
        } catch (Exception e) {
            throw new IOException("Failed to open texture at location " + relativePath + ": no such file");
        }
    }

    public static TextureImage createNew(int width, int height) {
        return createNew(width, height, (McMetaFile) null);
    }

    public static TextureImage createNew(int width, int height, @Nullable McMetaFile metadata) {
        var v = new TextureImage(new NativeImage(width, height, false), metadata);
        v.clear();
        return v;
    }

    @Deprecated(forRemoval = true)
    public static TextureImage createNew(int width, int height, @Nullable AnimationMetadataSection animation) {
        return createNew(width, height, animation == null ? null : McMetaFile.of(animation));
    }

    /**
     * Create a mask texture from the original texture and the given palette where all colors not in the palette will be black. the rest transparent
     */
    public static TextureImage createMask(TextureImage original, Palette palette) {
        TextureImage copy = original.makeCopy();
        NativeImage nativeImage = copy.getImage();
        SpriteUtils.forEachPixel(nativeImage, (x, y) -> {
            int color = nativeImage.getPixelRGBA(x, y);
            if (palette.hasColor(color)) {
                nativeImage.setPixelRGBA(x, y, 0);
            } else {
                nativeImage.setPixelRGBA(x, y, 0xFF000000);
            }
        });
        return copy;
    }

    public TextureImage createResized(float widthScale, float heightScale) {
        int newW = (int) (this.imageWidth() * widthScale);
        int newH = (int) (this.imageHeight() * heightScale);
        McMetaFile meta = null;
        if (metadata != null) {
            int mW = (int) (metadata.animation().frameWidth * widthScale);
            int mH = (int) (metadata.animation().frameHeight * heightScale);
            meta = metadata.cloneWithSize(mW, mH);
        }
        var im = TextureImage.createNew(newW, newH, meta);
        var t = ImageTransformer.builder(this.frameWidth(), this.frameHeight(), im.frameWidth(), im.frameHeight())
                .copyRect(0, 0, this.frameWidth(), this.frameHeight(), 0, 0).build();
        t.apply(this, im);
        return im;
    }

    public void clear() {
        image.fillRect(0, 0, image.getWidth(), image.getHeight(), 0);
    }

    @Deprecated(forRemoval = true)
    public static TextureImage of(NativeImage image, @Nullable AnimationMetadataSection animation) {
        return new TextureImage(image, animation == null ? null : McMetaFile.of(animation));
    }

    public static TextureImage of(NativeImage image) {
        return new TextureImage(image, null);
    }

    public static TextureImage of(NativeImage image, @Nullable McMetaFile metadata) {
        return new TextureImage(image, metadata);
    }

    @Override
    public void close() {
        this.image.close();
    }

    public int imageWidth() {
        return this.image.getWidth();
    }

    public int imageHeight() {
        return this.image.getHeight();
    }


    public ImmutableList<NativeImage> splitFrames() {
        var builder = ImmutableList.<NativeImage>builder();
        if (metadata == null) {
            builder.add(image);
            return builder.build();
        }
        int imgWidth = this.imageWidth(); // 16
        int imgHeight = this.imageHeight(); // 48
        var fs = metadata.animation().calculateFrameSize(imgWidth, imgHeight);


        int frameScaleWidth = imgWidth / fs.width(); // 1
        int frameScaleHeight = imgHeight / fs.height(); // 2
        int maxFrames = frameScaleWidth * frameScaleHeight; // 2

        List<Integer> indexList = Lists.newArrayList();

        metadata.animation().forEachFrame((index, time) -> indexList.add(index));
        if (indexList.isEmpty()) {
            for (int l = 0; l < maxFrames; ++l) {
                indexList.add(l);
            }
        }

        if (indexList.size() <= 1) {
            builder.add(image);
        } else {
            for (int index : indexList) { // 2, 1

                int xOffset = (index % frameScaleWidth) * frameWidth(); //(2 % 1) * 16
                int yOffset = (index / frameScaleWidth) * frameHeight(); // (2/1) * 32 =

                if (index >= 0 && xOffset + frameWidth() < imgWidth && yOffset + frameHeight() < imgHeight) {
                    NativeImage f = new NativeImage(frameWidth(), frameHeight(), false);
                    for (int x = 0; x < frameWidth(); x++) {
                        for (int y = 0; y < frameHeight(); y++) {
                            f.setPixelRGBA(x, y, this.image.getPixelRGBA(x + xOffset, y + yOffset));
                        }
                    }
                    builder.add(f);
                }
            }
        }
        return builder.build();
    }


    private void applyOverlay(boolean onlyOnExisting, TextureImage... overlays) throws IllegalStateException {
        for (var o : overlays) {
            if (o.frameWidth() < frameWidth()) {
                throw new IllegalStateException("Could not apply overlay onto images because overlay was too small (overlay W: " + o.frameWidth() + ", image W: " + frameWidth());
            }
            if (o.frameHeight() < frameHeight()) {
                throw new IllegalStateException("Could not apply overlay onto images because overlay was too small (overlay H: " + o.frameHeight() + ", image H: " + frameHeight());
            }
        }
        for (var o : overlays) {
            this.forEachFramePixel((frameIndex, globalX, globalY) -> {
                int frameX = getFrameX(frameIndex, globalX);
                int frameY = getFrameY(frameIndex, globalY);
                int targetOverlayFrame = Math.min(frameIndex, o.frameCount - 1);
                int overlayPixel = o.getFramePixel(targetOverlayFrame, frameX, frameY);
                if (onlyOnExisting && FastColor.ABGR32.alpha(overlayPixel) == 0) return;
                image.blendPixel(globalX, globalY, overlayPixel);
            });
            o.close();
        }
    }

    private int getFrameY(Integer frameIndex, Integer globalY) {
        return globalY - this.getFrameStartY(frameIndex);
    }

    private int getFrameX(Integer frameIndex, Integer globalX) {
        return globalX - this.getFrameStartX(frameIndex);
    }

    /**
     * Creates an image by combining two others taking alpha into consideration. Overlays are applied first in first out
     * Closes all given overlay images
     */
    public void applyOverlay(TextureImage... overlays) throws IllegalStateException {
        applyOverlay(false, overlays);
    }

    /**
     * Same as before but only applies them on non-transparent pixels
     * Overlays are applied first in first out
     * Closes all given overlays images
     */
    public void applyOverlayOnExisting(TextureImage... overlays) throws IllegalStateException {
        applyOverlay(true, overlays);
    }

    /**
     * Increases alpha of all pixels and sets the one that have alpha = 0 to background color
     */
    public void removeAlpha(int backgroundColor) {
        for (int x = 0; x < image.getWidth(); ++x) {
            for (int y = 0; y < image.getHeight(); ++y) {
                int oldValue = image.getPixelRGBA(x, y);
                int a = FastColor.ABGR32.alpha(oldValue);
                if (a == 0) {
                    image.setPixelRGBA(x, y, backgroundColor);
                } else {
                    image.setPixelRGBA(x, y, FastColor.ABGR32.color(255,
                            FastColor.ABGR32.blue(oldValue),
                            FastColor.ABGR32.green(oldValue),
                            FastColor.ABGR32.red(oldValue)));
                }
            }
        }
    }

    public void crop(TextureImage mask) {
        crop(mask, true);
    }

    /**
     * Crop the given image with the provided mask. All that isnt transparent will be erased
     * Closes the given mask
     *
     * @param mask         mask
     * @param discardInner crops pixels that are not transparent. False for opposite behavior
     */
    public void crop(TextureImage mask, boolean discardInner) {
        int width = imageWidth();
        int height = imageHeight();
        if (mask.imageHeight() < height || mask.imageWidth() < width) {
            throw new IllegalStateException("Could not merge images because they had different dimensions");
        }
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                if (FastColor.ABGR32.alpha(mask.image.getPixelRGBA(x, y)) != 0 == discardInner) {
                    image.setPixelRGBA(x, y, 0);
                }
            }
        }
        mask.close();
    }

    public TextureImage createRotated(Rotation rotation) {

        TextureImage flippedImage = TextureImage.createNew(frameHeight(), frameWidth() * frameCount, metadata);

        this.forEachFramePixel((frameIndex, globalX, globalY) -> {

            int frameX = getFrameX(frameIndex, globalX);
            int frameY = getFrameY(frameIndex, globalY);

            int newFrameX = frameX;
            int newFrameY = frameY;
            int frameWidth = this.frameWidth();
            int frameHeight = this.frameHeight();

            if (rotation == Rotation.CLOCKWISE_90) {
                newFrameX = frameHeight - frameY - 1;
                newFrameY = frameX;
            } else if (rotation == Rotation.CLOCKWISE_180) {
                newFrameX = frameWidth - frameX - 1;
                newFrameY = frameHeight - frameY - 1;
            } else if (rotation == Rotation.COUNTERCLOCKWISE_90) {
                newFrameX = frameY;
                newFrameY = frameWidth - frameX - 1;
            }

            int newGlobalX = flippedImage.getFrameStartX(frameIndex) + newFrameX;
            int newGlobalY = flippedImage.getFrameStartY(frameIndex) + newFrameY;

            int pixel = this.getImage().getPixelRGBA(globalX, globalY);
            flippedImage.getImage().setPixelRGBA(newGlobalX, newGlobalY, pixel);
        });

        return flippedImage;
    }

}
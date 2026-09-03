package net.mehvahdjukaar.moonlight.api.resources.textures;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.mojang.blaze3d.platform.NativeImage;
import net.mehvahdjukaar.moonlight.api.resources.ResType;
import net.mehvahdjukaar.moonlight.api.util.math.colors.RGBColor;
import net.mehvahdjukaar.moonlight.core.Moonlight;
import net.mehvahdjukaar.moonlight.core.misc.McMetaFile;
import net.minecraft.client.resources.metadata.animation.AnimationFrame;
import net.minecraft.client.resources.metadata.animation.AnimationMetadataSection;
import net.minecraft.client.resources.metadata.animation.FrameSize;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.Rotation;
import org.apache.logging.log4j.util.TriConsumer;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

//like a native image that also has its metadata
public class TextureImage implements AutoCloseable, Sampler2D {

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

            return new TextureImage(i, metadata, relativePath.toString());
        } catch (Exception e) {
            //try paletted permutations
            TextureImage virtual = PalettedPermutationsHelper.tryResolveImage(manager, relativePath);
            if (virtual != null) return virtual;
            throw new IOException("Failed to open texture at location " + relativePath + ": no such file");
        }
    }

    public static TextureImage createNew(int width, int height) {
        return createNew(width, height, (McMetaFile) null);
    }

    public static TextureImage createNew(int width, int height, @Nullable McMetaFile metadata) {
        if (width <= 0 || height <= 0) {
            throw new IllegalArgumentException("Width and height must be positive integers");
        }
        var v = new TextureImage(new NativeImage(width, height, false), metadata);
        v.clear();
        return v;
    }

    public static TextureImage of(NativeImage image) {
        return of(image, (McMetaFile) null);
    }

    public static TextureImage of(NativeImage image, @Nullable McMetaFile metadata) {
        return new TextureImage(image, metadata);
    }


    @Nullable
    private final McMetaFile metadata;
    private final NativeImage image;
    //width of a frame
    private final FrameSize frameSize;
    //All frames. Includes unused ones
    private final int frameCount;
    private final int frameScale;

    final String debugPath;

    private TextureImage(NativeImage image, @Nullable McMetaFile metadata) {
        this(image, metadata, "unknown");
    }

    private TextureImage(NativeImage image, @Nullable McMetaFile metadata, String debugPath) {
        this.image = image;
        this.metadata = metadata;
        this.debugPath = debugPath;

        int imgWidth = imageWidth();
        int imgHeight = imageHeight();

        FrameSize metaSize = (metadata == null || !metadata.hasAnimation())
                ? new FrameSize(imgWidth, imgHeight)
                : metadata.animation().calculateFrameSize(imgWidth, imgHeight);

        int fw = metaSize.width();
        int fh = metaSize.height();

        // --- Hard sanity checks ---
        if (fw <= 0 || fh <= 0) {
            Moonlight.LOGGER.error(
                    "Texture '{}' has invalid metadata frame size {}x{}, using full image size {}x{} instead",
                    debugPath, fw, fh, imgWidth, imgHeight
            );
            fw = imgWidth;
            fh = imgHeight;
        }

        if (fw > imgWidth || fh > imgHeight) {
            Moonlight.LOGGER.error(
                    "Texture '{}' frame size {}x{} is larger than image {}x{}, using full image size instead",
                    debugPath, fw, fh, imgWidth, imgHeight
            );
            fw = imgWidth;
            fh = imgHeight;
        }

        this.frameSize = new FrameSize(fw, fh); // final, assigned once

        // --- SAFE grid (floor division) ---
        int gridW = imgWidth / fw;
        int gridH = imgHeight / fh;

        if (gridW == 0 || gridH == 0) {
            Moonlight.LOGGER.error(
                    "Texture '{}' frame size {}x{} cannot fit in image {}x{}, defaulting to 1x1 grid",
                    debugPath, fw, fh, imgWidth, imgHeight
            );
            gridW = 1;
            gridH = 1;
        }

        this.frameScale = gridW;
        this.frameCount = gridW * gridH;
    }

    public int imageWidth() {
        return this.image.getWidth();
    }

    public int imageHeight() {
        return this.image.getHeight();
    }

    public int frameCount() {
        return frameCount;
    }

    public int frameWidth() {
        return frameSize.width();
    }

    public int frameHeight() {
        return frameSize.height();
    }

    public McMetaFile getMcMeta() {
        return metadata;
    }

    @ApiStatus.Internal
    public NativeImage getImage() {
        return image;
    }

    @Override
    public String toString() {
        return "TextureImage{" +
                debugPath + ", allocated = " + isAllocated() +
                '}';
    }

    public boolean isAllocated() {
        return image.pixels != 0;
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

    public int getPixel(int x, int y) {
        return image.getPixelRGBA(x, y);
    }

    @Override
    public int sample(float x, float y) {
        int ix = Mth.clamp(Math.round(x), 0, imageWidth() - 1);
        int iy = Mth.clamp(Math.round(y), 0, imageHeight() - 1);
        return getPixel(ix, iy);
    }

    public Sampler2D frameSampler(int frameIndex) {
        return (x, y) -> {
            int ix = Mth.clamp(Math.round(x), 0, frameWidth() - 1);
            int iy = Mth.clamp(Math.round(y), 0, frameHeight() - 1);
            return getFramePixel(frameIndex, ix, iy);
        };
    }

    public void setFramePixel(int frameIndex, int x, int y, int color) {
        image.setPixelRGBA(getFrameStartX(frameIndex) + x, getFrameStartY(frameIndex) + y, color);
    }

    public void setPixel(int x, int y, int color) {
        image.setPixelRGBA(x, y, color);
    }

    public void blendPixel(int x, int y, int color) {
        image.blendPixel(x, y, color);
    }

    public void blendFramePixel(int frameIndex, int x, int y, int color) {
        image.blendPixel(getFrameStartX(frameIndex) + x, getFrameStartY(frameIndex) + y, color);
    }

    public void forEachPixel(Consumer<PixelContext> consumer) {
        PixelContext pixel = new PixelContext(this);
        for (int frameIdx = 0; frameIdx < frameCount; frameIdx++) {
            int xOff = getFrameStartX(frameIdx);
            int yOff = getFrameStartY(frameIdx);
            //row major, same order as SpriteUtils.forEachPixel
            for (int y = 0; y < frameHeight(); y++) {
                for (int x = 0; x < frameWidth(); x++) {
                    pixel.frameIndex = frameIdx;
                    pixel.localX = x;
                    pixel.localY = y;
                    pixel.globalX = x + xOff;
                    pixel.globalY = y + yOff;
                    consumer.accept(pixel);
                }
            }
        }
    }

    public TextureImage makeCopy() {
        return makeCopyWithMetadata(this.metadata);
    }

    public TextureImage makeCopyWithMetadata(McMetaFile mcMetaFile) {
        NativeImage im = new NativeImage(this.imageWidth(), this.imageHeight(), false);
        im.copyFrom(image);
        return new TextureImage(im, mcMetaFile);
    }

    @Override
    public void close() {
        this.image.close();
    }


    @FunctionalInterface
    public interface ThrowingRunnable {
        void run() throws Exception;
    }

    // alternative to try with resources
    public void doAndClose(ThrowingRunnable action) {
        try (this) {
            action.run();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void clear() {
        image.fillRect(0, 0, image.getWidth(), image.getHeight(), 0);
    }

    @Deprecated(forRemoval = true)
    public RGBColor getAverageColor() {
        return SpriteUtils.averageColor(this.image);
    }

    public ImmutableList<NativeImage> splitFrames() {
        var builder = ImmutableList.<NativeImage>builder();

        int imgWidth = imageWidth();
        int imgHeight = imageHeight();

        int fw = frameWidth();
        int fh = frameHeight();

        int gridW = imgWidth / fw;
        int gridH = imgHeight / fh;
        int maxFrames = gridW * gridH;

        // If there is no animation metadata, just return whole image
        if (metadata == null || !metadata.hasAnimation() || metadata.animation().frames.isEmpty()) {
            builder.add(image);
            return builder.build();
        }

        List<Integer> indexList = Lists.newArrayList();
        metadata.animation().forEachFrame((index, time) -> indexList.add(index));

        if (indexList.isEmpty()) {
            for (int i = 0; i < maxFrames; i++) {
                indexList.add(i);
            }
        }

        // If effectively single-frame, return original image
        if (indexList.size() <= 1) {
            builder.add(image);
            return builder.build();
        }

        for (int index : indexList) {

            if (index < 0 || index >= maxFrames) {
                continue; // ignore invalid metadata safely
            }

            int xOffset = (index % gridW) * fw;
            int yOffset = (index / gridW) * fh;

            // Hard bounds guarantee (should always pass now)
            if (xOffset + fw <= imgWidth && yOffset + fh <= imgHeight) {
                NativeImage frame = new NativeImage(fw, fh, false);

                for (int x = 0; x < fw; x++) {
                    for (int y = 0; y < fh; y++) {
                        frame.setPixelRGBA(x, y, image.getPixelRGBA(x + xOffset, y + yOffset));
                    }
                }

                builder.add(frame);
            }
        }

        return builder.build();
    }

    //deprecated stuff

    //texture ops stuff


    @Deprecated
    public void toGrayscale() {
        TextureOps.grayscale(this);
    }


    @Deprecated(forRemoval = true)
    public TextureImage createAnimationTemplate(int length, McMetaFile useDataFrom) {
        return TextureOps.createSingleFrameAnimation(this, length, useDataFrom);
    }


    @Deprecated(forRemoval = true)
    public void applyOverlay(TextureImage... overlays) throws IllegalStateException {
        TextureOps.applyOverlay(this, overlays);
        Arrays.stream(overlays).forEach(TextureImage::close);
    }

    @Deprecated(forRemoval = true)
    public void applyOverlayOnExisting(TextureImage... overlays) throws IllegalStateException {
        TextureOps.applyOverlayOnExisting(this, overlays);
        Arrays.stream(overlays).forEach(TextureImage::close);
    }


    @Deprecated(forRemoval = true)
    public void removeAlpha(int backgroundColor) {
        TextureOps.makeOpaque(this, backgroundColor);
    }

    @Deprecated(forRemoval = true)
    public TextureImage createRotated(Rotation rotation) {
        return TextureOps.createRotated(this, rotation);
    }

    @Deprecated(forRemoval = true)
    public TextureImage createResized(float widthScale, float heightScale) {
        return TextureOps.createScaled(this, widthScale, heightScale);
    }

    @Deprecated(forRemoval = true)
    public void crop(TextureImage mask) {
        crop(mask, true);
    }

    @Deprecated(forRemoval = true)
    public void crop(TextureImage mask, boolean discardInner) {
        if (discardInner) TextureOps.applyMask(this, mask);
        else TextureOps.applyMaskInverted(this, mask);
        mask.close();
    }


    //old stuff

    @Deprecated(forRemoval = true)
    public TextureImage createAnimationTemplate(int length, @NotNull AnimationMetadataSection useDataFrom) {
        return createAnimationTemplate(length, McMetaFile.of(useDataFrom));
    }

    @Deprecated(forRemoval = true)
    public TextureImage createAnimationTemplate(int length, List<AnimationFrame> frameData, int frameTime, boolean interpolate) {
        return createAnimationTemplate(length, new AnimationMetadataSection(frameData, this.frameWidth(), this.frameHeight(), frameTime, interpolate));
    }

    @Deprecated(forRemoval = true)
    public void forEachFrame(FramePixelConsumer e) {
        forEachFramePixel(e);
    }

    /**
     * Accepts a consumer that iterates over all image pixels, ordered by frame.
     * The given coordinates are global texture coordinates while the index represents the currently viewed frame
     */
    @Deprecated(forRemoval = true)
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


    @Deprecated(forRemoval = true)
    @Nullable
    public AnimationMetadataSection getMetadata() {
        return metadata == null ? null : metadata.animation();
    }


    @Deprecated(forRemoval = true)
    public static TextureImage createNew(int width, int height, @Nullable AnimationMetadataSection animation) {
        return createNew(width, height, animation == null ? null : McMetaFile.of(animation));
    }

    @Deprecated(forRemoval = true)
    public static TextureImage of(NativeImage image, @Nullable AnimationMetadataSection animation) {
        return of(image, animation == null ? null : McMetaFile.of(animation));
    }

    //ind, x, y
    @Deprecated(forRemoval = true)
    @FunctionalInterface
    public interface FramePixelConsumer extends TriConsumer<Integer, Integer, Integer> {
        @Override
        void accept(Integer frameIndex, Integer globalX, Integer globalY);
    }

}
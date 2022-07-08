package net.mehvahdjukaar.moonlight.api.resources.textures;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mojang.blaze3d.platform.NativeImage;
import net.mehvahdjukaar.moonlight.api.resources.ResType;
import net.minecraft.client.resources.metadata.animation.AnimationFrame;
import net.minecraft.client.resources.metadata.animation.AnimationMetadataSection;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.AbstractPackResources;
import net.minecraft.server.packs.resources.ResourceManager;
import org.apache.logging.log4j.util.TriConsumer;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

//like a native image that also has its metadata
public class TextureImage implements AutoCloseable {

    @Nullable
    private final AnimationMetadataSection metadata;
    private final NativeImage image;
    //width of a frame
    private final int frameW;
    //height of a frame
    private final int frameH;
    //all frames. Includes unused ones
    private final int maxFrames;

    private final int frameScale;


    private TextureImage(NativeImage image, @Nullable AnimationMetadataSection metadata) {
        this.image = image;
        this.metadata = metadata;
        int imgWidth = this.imageWidth(); // 16
        int imgHeight = this.imageHeight(); // 48
        this.frameW = metadata == null ? imgWidth : metadata.getFrameWidth(imgWidth);
        this.frameH = metadata == null ? imgHeight : metadata.getFrameHeight(imgWidth);
        this.frameScale = imgWidth / frameW; // 1
        int frameScaleHeight = imgHeight / frameH; // 2
        this.maxFrames = frameScale * frameScaleHeight; // 2
    }

    /**
     * Accepts a consumer that iterates over all image pixels, ordered by frame.
     * The given coordinates are global texture coordinates while the index represents the currently viewed frame
     */
    public void forEachFrame(FramePixelConsumer framePixelConsumer) {
        for (int ind = 0; ind < maxFrames; ind++) {
            int xOff = getFrameX(ind);
            int yOff = getFrameY(ind);
            for (int x = 0; x < frameW; x++) {
                for (int y = 0; y < frameH; y++) {
                    framePixelConsumer.accept(ind, x + xOff, y + yOff);
                }
            }
        }
    }

    public void toGrayscale(){
        SpriteUtils.grayscaleImage(this.image);
    }

    public int frameWidth() {
        return frameW;
    }

    public int frameHeight() {
        return frameH;
    }

    @FunctionalInterface
    public interface FramePixelConsumer extends TriConsumer<Integer, Integer, Integer> {

        @Override
        void accept(Integer frameIndex, Integer globalX, Integer globalY);
    }


    public int getFrameX(int frameIndex) {
        return (frameIndex % frameScale) * frameW; //(2 % 1) * 16
    }

    public int getFrameY(int frameIndex) {
        return (frameIndex / frameScale) * frameH; // (2/1) * 32
    }

    public NativeImage getImage() {
        return image;
    }

    public int framesSize() {
        return maxFrames;
    }

    @Nullable
    public AnimationMetadataSection getMetadata() {
        return metadata;
    }

    public TextureImage makeCopy() {
        NativeImage im = new NativeImage(this.imageWidth(), this.imageHeight(), false);
        im.copyFrom(image);
        return new TextureImage(im, metadata);
    }

    public TextureImage createAnimationTemplate(int length, AnimationMetadataSection useDataFrom) {
        List<AnimationFrame> frameData = new ArrayList<>();
        useDataFrom.forEachFrame((i, t) -> frameData.add(new AnimationFrame(i, t)));

        return createAnimationTemplate(length, frameData, useDataFrom.getDefaultFrameTime(), useDataFrom.isInterpolatedFrames());
    }

    /**
     * Creates a new image using the first frame of this one. Its frame data and frame lenght will be the one provided
     */
    public TextureImage createAnimationTemplate(int length, List<AnimationFrame> frameData, int frameTime, boolean interpolate) {
        NativeImage im = new NativeImage(this.frameWidth(), this.frameHeight() * length, false);
        TextureImage t = new TextureImage(im, new AnimationMetadataSection(frameData, this.frameW, this.frameH, frameTime, interpolate));

        t.forEachFrame((i, x, y) -> {
            int xo = x - t.getFrameX(i);
            int yo = y - t.getFrameY(i);
            t.image.setPixelRGBA(x, y, this.image.getPixelRGBA(xo, yo));
        });
        return t;
    }

    /**
     * Opens a texture image from the given resource path. A texture image is composed of a NativeImage and its associated McMeta file
     *
     * @param manager      resource manager
     * @param relativePath relative texture path (does not include /textures)
     */
    public static TextureImage open(ResourceManager manager, ResourceLocation relativePath) throws IOException {
        ResourceLocation textureLoc = ResType.TEXTURES.getPath(relativePath);
        NativeImage i = SpriteUtils.readImage(manager, textureLoc);
        //try getting metadata for animated textures
        ResourceLocation metadataLoc = ResType.MCMETA.getPath(relativePath);
        AnimationMetadataSection metadata = null;

        var res = manager.getResource(metadataLoc);
        if(res.isPresent()){
            try (InputStream metadataStream = res.get().open()) {
                metadata = AbstractPackResources.getMetadataFromStream(AnimationMetadataSection.SERIALIZER, metadataStream);

            } catch (Exception ignored) {}
        }

        return new TextureImage(i, metadata);
    }

    public static TextureImage createNew(int width, int height, @Nullable AnimationMetadataSection animation) {
        return new TextureImage(new NativeImage(width, height, false), animation);
    }

    public static TextureImage of(NativeImage image, @Nullable AnimationMetadataSection animation) {
        return new TextureImage(image, animation);
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


    private ImmutableList<NativeImage> splitFrames() {
        var builder = ImmutableList.<NativeImage>builder();
        if (metadata == null) {
            builder.add(image);
            return builder.build();
        }
        int imgWidth = this.imageWidth(); // 16
        int imgHeight = this.imageHeight(); // 48
        int frameW = metadata.getFrameWidth(imgWidth);
        int frameH = metadata.getFrameHeight(imgWidth);

        int frameScaleWidth = imgWidth / frameW; // 1
        int frameScaleHeight = imgHeight / frameH; // 2
        int maxFrames = frameScaleWidth * frameScaleHeight; // 2

        List<Integer> indexList = Lists.newArrayList();

        metadata.forEachFrame((index, time) -> indexList.add(index));
        if (indexList.isEmpty()) {
            for (int l = 0; l < maxFrames; ++l) {
                indexList.add(l);
            }
        }

        if (indexList.size() <= 1) {
            builder.add(image);
        } else {
            for (int index : indexList) { // 2, 1

                int xOffset = (index % frameScaleWidth) * frameW; //(2 % 1) * 16
                int yOffset = (index / frameScaleWidth) * frameH; // (2/1) * 32 =

                if (index >= 0 && xOffset + frameW < imgWidth && yOffset + frameH < imgHeight) {
                    NativeImage f = new NativeImage(frameW, frameH, false);
                    for (int x = 0; x < frameW; x++) {
                        for (int y = 0; y < frameH; y++) {
                            f.setPixelRGBA(x, y, this.image.getPixelRGBA(x + xOffset, y + yOffset));
                        }
                    }
                    builder.add(f);
                }
            }
        }
        return builder.build();
    }


    @Nullable
    public JsonObject serializeMcMeta() {
        if (metadata == null) return null;
        JsonObject obj = new JsonObject();

        JsonObject animation = new JsonObject();

        animation.addProperty("frametime", metadata.getDefaultFrameTime());
        animation.addProperty("interpolate", metadata.isInterpolatedFrames());
        animation.addProperty("height", metadata.getFrameHeight(this.frameHeight()));
        animation.addProperty("width", metadata.getFrameWidth(this.frameWidth()));

        JsonArray frames = new JsonArray();

        metadata.forEachFrame((i, t) -> {
            if (t != -1) {
                JsonObject o = new JsonObject();
                o.addProperty("time", t);
                o.addProperty("index", i);
                frames.add(o);
            } else frames.add(i);
        });

        animation.add("frames", frames);

        obj.add("animation", animation);

        return obj;
    }

    /**
     * Creates an image by combining two others taking alpha into consideration. Overlays are applied first in first out
     * Closes all given overlays images
     */
    public void applyOverlay(TextureImage... overlays) throws IllegalStateException {
        int width = imageWidth();
        int height = imageHeight();
        if (Arrays.stream(overlays).anyMatch(n -> n.imageHeight() < height || n.imageWidth() < width)) {
            throw new IllegalStateException("Could not merge images because they had different dimensions");
        }

        for (var o : overlays) {
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    image.blendPixel(x, y, o.image.getPixelRGBA(x, y));
                }
            }
            o.close();
        }
    }

    /**
     * Same as before but only applies them on non transparent pixels
     * Overlays are applied first in first out
     * Closes all given overlays images
     */
    public void applyOverlayOnExisting(TextureImage... overlays) throws IllegalStateException {
        int width = imageWidth();
        int height = imageHeight();
        if (Arrays.stream(overlays).anyMatch(n -> n.imageHeight() < height || n.imageWidth() < width)) {
            throw new IllegalStateException("Could not merge images because they had different dimensions");
        }

        for (var o : overlays) {
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    if(NativeImage.getA(image.getPixelRGBA(x,y))!=0) {
                        image.blendPixel(x, y, o.image.getPixelRGBA(x, y));
                    }
                }
            }
            o.close();
        }
    }

    /**
     * Increases alpha of all pixels and sets the one that have alpha = 0 to background color
     */
    public void removeAlpha(int backgroundColor) {
        for (int x = 0; x < image.getWidth(); ++x) {
            for (int y = 0; y < image.getHeight(); ++y) {
                int oldValue = image.getPixelRGBA(x, y);
                int a = NativeImage.getA(oldValue);
                if (a == 0) {
                    image.setPixelRGBA(x, y, backgroundColor);
                } else {
                    image.setPixelRGBA(x, y, NativeImage.combine(255,
                            NativeImage.getB(oldValue),
                            NativeImage.getG(oldValue),
                            NativeImage.getR(oldValue)));
                }
            }
        }
    }

    public void crop(TextureImage mask){
        crop(mask, true);
    }

    /**
     * Crop the given image with the provided mask. All that isnt transparent will be erased
     * Closes the given mask
     * @param mask mask
     * @param inner if the operation should be reversed by keeping what is not transparent
     */
    public void crop(TextureImage mask, boolean inner){
        int width = imageWidth();
        int height = imageHeight();
        if (mask.imageHeight() < height || mask.imageWidth() < width) {
            throw new IllegalStateException("Could not merge images because they had different dimensions");
        }
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                if(NativeImage.getA(mask.image.getPixelRGBA(x,y))!=0 == inner) {
                    image.setPixelRGBA(x,y,0);
                }
            }
        }
        mask.close();
    }

}


package net.mehvahdjukaar.moonlight.api.resources.textures;

import net.mehvahdjukaar.moonlight.api.platform.PlatHelper;
import net.mehvahdjukaar.moonlight.api.util.math.colors.RGBColor;
import net.mehvahdjukaar.moonlight.core.Moonlight;
import net.mehvahdjukaar.moonlight.core.misc.McMetaFile;
import net.minecraft.util.FastColor;
import net.minecraft.world.level.block.Rotation;

public final class TextureOps {

    public static void grayscale(TextureImage img) {
        img.forEachPixel(pixel -> pixel.setValue(new RGBColor(pixel.getValue())
                .asHCL().withChroma(0).asRGB().toInt()));
    }

    /**
     * Applies one or more overlay images onto a base image.
     * Throws if any overlay is smaller than the base image in frame dimensions.
     *
     * @param img      the target TextureImage to apply overlays on
     * @param overlays one or more overlay TextureImages to apply
     * @throws IllegalStateException if an overlay is smaller than the base image
     */
    public static void applyOverlay(TextureImage img, TextureImage... overlays) {
        applyOverlay(img, false, overlays);
    }

    public static void applyOverlayOnExisting(TextureImage img, TextureImage... overlays) {
        applyOverlay(img, true, overlays);
    }

    private static void applyOverlay(TextureImage baseImage, boolean onlyOnExisting, TextureImage... overlays) {
        int baseFrameWidth = baseImage.frameWidth();
        int baseFrameHeight = baseImage.frameHeight();

        // Validate overlays size
        for (TextureImage overlay : overlays) {
            if (overlay.frameWidth() < baseFrameWidth) {
                throw new IllegalStateException(
                        "Overlay width too small (overlay W: " + overlay.frameWidth() + ", base W: " + baseFrameWidth + ")");
            }
            if (overlay.frameHeight() < baseFrameHeight) {
                throw new IllegalStateException(
                        "Overlay height too small (overlay H: " + overlay.frameHeight() + ", base H: " + baseFrameHeight + ")");
            }
        }

        // Apply overlays
        for (TextureImage overlay : overlays) {
            baseImage.forEachPixel(pixel -> {
                int frameX = pixel.frameX();
                int frameY = pixel.frameY();

                int overlayFrame = Math.min(pixel.frameIndex(), overlay.frameCount() - 1);
                int overlayPixel = overlay.getFramePixel(overlayFrame, frameX, frameY);

                if (onlyOnExisting && FastColor.ABGR32.alpha(overlayPixel) == 0) {
                    return;
                }

                pixel.blendValue(overlayPixel);
            });
        }
    }


    /**
     * Replaces fully transparent pixels with the given background color
     * and sets all other pixels' alpha to fully opaque (255).
     *
     * @param backgroundColor the color to set pixels with zero alpha
     */
    public static void makeOpaque(TextureImage img, int backgroundColor) {
        img.forEachPixel(pixel -> {
            int oldValue = pixel.getValue();
            int alpha = FastColor.ABGR32.alpha(oldValue);
            if (alpha == 0) {
                // Fully transparent — replace with background color
                pixel.setValue(backgroundColor);
            } else {
                // Keep color, but set alpha fully opaque (255)
                int newColor = FastColor.ABGR32.color(
                        255,
                        FastColor.ABGR32.red(oldValue),
                        FastColor.ABGR32.green(oldValue),
                        FastColor.ABGR32.blue(oldValue)
                );
                pixel.setValue(newColor);
            }
        });
    }


    private static void applyMask(TextureImage img, TextureImage mask, boolean discardOpaque) {
        if (mask.imageWidth() < img.imageWidth() || mask.imageHeight() < img.imageHeight()) {
            Moonlight.LOGGER.error("Palette mask {} needs to be at least as large as the target image {} and have the same frame count. You must alter the mask to match the texture size", img.path, mask.path);
            if (PlatHelper.isDev()) {
                throw new IllegalArgumentException("Palette mask " + mask.path + " has invalid size or frame count");
            }
            return;
        }

        img.forEachPixel(pixel -> {
            int maskPixel = mask.getPixel(pixel.x(), pixel.y());
            boolean maskOpaque = FastColor.ABGR32.alpha(maskPixel) != 0;
            if (maskOpaque == discardOpaque) {
                pixel.setValue(0);
            }
        });
    }

    /**
     * Crop the given image with the provided mask. All that isn't transparent will be erased
     *
     * @param mask mask
     */
    public static void applyMask(TextureImage img, TextureImage mask) {
        applyMask(img, mask, true);
    }

    public static void applyMaskInverted(TextureImage img, TextureImage mask) {
        applyMask(img, mask, false);
    }

    public static void tileTexture(TextureImage image, TextureImage toTileOn, int xOff, int yOff) {
        int tileW = toTileOn.imageWidth();
        int tileH = toTileOn.imageHeight();

        // For each pixel in the base image, pick from tiled toTileOn
        image.forEachPixel(pixel -> {
            int x = pixel.x();
            int y = pixel.y();

            // Calculate where this pixel maps to in the tiled texture
            int srcX = Math.floorMod(x - xOff, tileW);
            int srcY = Math.floorMod(y - yOff, tileH);

            int val = toTileOn.getPixel(srcX, srcY);
            pixel.setValue(val);
        });
    }


    public static void multiplyColor(TextureImage img, int tintColor) {
        int r = FastColor.ABGR32.red(tintColor);
        int g = FastColor.ABGR32.green(tintColor);
        int b = FastColor.ABGR32.blue(tintColor);
        img.forEachPixel(pixel -> {
            int oldValue = pixel.getValue();
            int newColor = FastColor.ABGR32.color(
                    FastColor.ABGR32.alpha(oldValue),
                    FastColor.ABGR32.red(oldValue) * r / 255,
                    FastColor.ABGR32.green(oldValue) * g / 255,
                    FastColor.ABGR32.blue(oldValue) * b / 255
            );
            pixel.setValue(newColor);
        });
    }


    //create

    /**
     * Creates a new image using the first frame of this one. Its frame data and frame length will be the one provided
     */
    public static TextureImage createSingleFrameAnimation(TextureImage img, McMetaFile animationData) {
        int length = animationData.getLogicalFrameCount();
        if (length <= 0) {
            throw new IllegalArgumentException("Length must be greater than 0");
        }
        McMetaFile newMetadata = animationData.cloneWithSize(img.frameWidth(), img.frameHeight());
        if (length == 1) {
            return img.makeCopyWithMetadata(newMetadata);
        }
        TextureImage newImage = TextureImage.createNew(img.frameWidth(), img.frameHeight() * length, newMetadata);

        newImage.forEachPixel(pixel -> {
            int xo = pixel.localX;
            int yo = pixel.localY;
            pixel.setValue(img.getFramePixel(0, xo, yo));
        });
        return newImage;
    }

    @Deprecated(forRemoval = true)
    public static TextureImage createSingleFrameAnimation(TextureImage img, int length, McMetaFile animationData) {
        return createSingleFrameAnimation(img, animationData);
    }

    public static TextureImage createScaled(TextureImage img, float widthScale, float heightScale) {
        int newW = (int) (img.imageWidth() * widthScale);
        int newH = (int) (img.imageHeight() * heightScale);
        McMetaFile meta = null;
        var metadata = img.getMcMeta();
        if (metadata != null) {
            int mW = (int) (metadata.animation().frameWidth * widthScale);
            int mH = (int) (metadata.animation().frameHeight * heightScale);
            meta = metadata.cloneWithSize(mW, mH);
        }
        TextureImage im = TextureImage.createNew(newW, newH, meta);
        TextureCollager transformer = TextureCollager.builder(img.frameWidth(), img.frameHeight(), im.frameWidth(), im.frameHeight())
                .copyFrom(0, 0, img.frameWidth(), img.frameHeight())
                .to(0, 0, im.frameWidth(), im.frameHeight())
                .build();
        transformer.apply(img, im);
        return im;
    }

    public static TextureImage createRotated(TextureImage img, Rotation rotation) {

        TextureImage flippedImage = TextureImage.createNew(img.frameHeight(),
                img.frameWidth() * img.frameCount(), img.getMcMeta());

        img.forEachPixel(context -> {

            int frameX = context.frameX();
            int frameY = context.frameY();
            int frameIndex = context.frameIndex();

            int newFrameX = frameX;
            int newFrameY = frameY;
            int frameWidth = img.frameWidth();
            int frameHeight = img.frameHeight();

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

            int pixel = context.getValue();
            flippedImage.setPixel(newGlobalX, newGlobalY, pixel);
        });

        return flippedImage;
    }

}

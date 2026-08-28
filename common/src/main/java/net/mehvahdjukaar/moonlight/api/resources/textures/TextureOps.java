package net.mehvahdjukaar.moonlight.api.resources.textures;

import net.mehvahdjukaar.moonlight.api.platform.PlatHelper;
import net.mehvahdjukaar.moonlight.api.util.math.colors.RGBColor;
import net.mehvahdjukaar.moonlight.core.Moonlight;
import net.mehvahdjukaar.moonlight.core.misc.McMetaFile;
import net.minecraft.util.ARGB;
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

                if (onlyOnExisting && ARGB.alpha(overlayPixel) == 0) {
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
            int alpha = ARGB.alpha(oldValue);
            if (alpha == 0) {
                pixel.setValue(backgroundColor);
            } else {
                // Keep color, but set alpha fully opaque (255)
                pixel.setValue(ARGB.opaque(oldValue));
            }
        });
    }


    private static void applyMask(TextureImage img, TextureImage mask, boolean discardOpaque) {
        if (!checkMaskCoversImage(img, mask, "applyMask")) return;

        int maskFrames = mask.frameCount();
        img.forEachPixel(pixel -> {
            int maskPixel = mask.getFramePixel(pixel.frameIndex() % maskFrames, pixel.frameX(), pixel.frameY());
            boolean maskOpaque = ARGB.alpha(maskPixel) != 0;
            if (maskOpaque == discardOpaque) {
                pixel.setValue(0);
            }
        });
    }

    private static boolean checkMaskCoversImage(TextureImage img, TextureImage mask, String opName) {
        if (mask.frameWidth() < img.frameWidth() || mask.frameHeight() < img.frameHeight()) {
            Moonlight.LOGGER.error("{} - Mask {} needs to be at least as large as the target image {}. You must alter the mask's frame size {}x{} to match the texture's frame size {}x{}",
                    opName, mask.debugPath, img.debugPath, mask.frameWidth(), mask.frameHeight(), img.frameWidth(), img.frameHeight());
            if (PlatHelper.isDev()) {
                throw new IllegalArgumentException("Mask " + mask.debugPath + " has invalid frame size");
            }
            return false;
        }
        return true;
    }

    /**
     * Multiplies each pixel's alpha by the alpha of the matching mask pixel. Unlike applyMask this keeps
     * soft edges, so a half transparent mask pixel leaves a half transparent image pixel.
     */
    public static void multiplyAlpha(TextureImage img, TextureImage mask) {
        if (!checkMaskCoversImage(img, mask, "multiplyAlpha")) return;

        int maskFrames = mask.frameCount();
        img.forEachPixel(pixel -> {
            int color = pixel.getValue();
            int maskPixel = mask.getFramePixel(pixel.frameIndex() % maskFrames, pixel.frameX(), pixel.frameY());
            int alpha = ARGB.alpha(color) * ARGB.alpha(maskPixel) / 255;
            pixel.setValue(ARGB.color(alpha, color));
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


    //create

    /**
     * Creates a new image made of length copies of this one's first frame, stacked vertically.
     * Its frame data will be the one provided
     */
    public static TextureImage createSingleFrameAnimation(TextureImage img, int length, McMetaFile animationData) {
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

    public static TextureImage createScaled(TextureImage img, float widthScale, float heightScale) {
        int newW = (int) (img.imageWidth() * widthScale);
        int newH = (int) (img.imageHeight() * heightScale);
        McMetaFile meta = null;
        var metadata = img.getMcMeta();
        if (metadata != null) {
            //nothing to rescale if there's no animation, but the modded data still carries over
            meta = metadata.hasAnimation()
                    ? metadata.cloneWithSize((int) (metadata.getAnimationFrameWidth() * widthScale),
                    (int) (metadata.getAnimationFrameHeight() * heightScale))
                    : metadata.copy();
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

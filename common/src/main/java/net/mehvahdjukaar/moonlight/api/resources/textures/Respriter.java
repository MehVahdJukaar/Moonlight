package net.mehvahdjukaar.moonlight.api.resources.textures;

import com.mojang.blaze3d.platform.NativeImage;
import net.mehvahdjukaar.moonlight.api.platform.PlatHelper;
import net.mehvahdjukaar.moonlight.core.Moonlight;
import net.mehvahdjukaar.moonlight.core.misc.McMetaFile;
import net.minecraft.client.resources.metadata.animation.AnimationMetadataSection;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Respriter {

    private final TextureImage imageToRecolor;
    //one palette for each frame. frame order will be the same
    private final List<Palette> originalPalettes;
    //if respriter is not provided a texture list for an animated image, it will use the first palette for all images,
    //keeping recolors consistent
    private final boolean useMergedPalette;

    /**
     * Base respriter. Automatically grabs a palette from this image and swaps it in recolorImage with the other one provided
     *
     * @param imageToRecolor base image that needs to be recolored
     */
    public static Respriter of(TextureImage imageToRecolor) {
        return new Respriter(imageToRecolor, Palette.fromAnimatedImage(imageToRecolor, null, 0));
    }

    /**
     * Only includes colors from the target image following the provided mask
     *
     * @param imageToRecolor base image that needs to be recolored
     */
    public static Respriter masked(TextureImage imageToRecolor, TextureImage colorMask) {
        return new Respriter(imageToRecolor, List.of(Palette.fromImage(imageToRecolor, colorMask, 0)));
    }

    public static Respriter ofPalette(TextureImage imageToRecolor, List<Palette> colorsToSwap) {
        return new Respriter(imageToRecolor, colorsToSwap);
    }

    /**
     * Creates a respriter object, used to change a target image colors a repeated number of times
     *
     * @param imageToRecolor template image that you wish to recolor
     * @param colorsToSwap   palette containing colors that need to be changed.
     *                       Does not care about animated texture and will not treat each frame individually
     */
    public static Respriter ofPalette(TextureImage imageToRecolor, Palette colorsToSwap) {
        return new Respriter(imageToRecolor, List.of(colorsToSwap));
    }

    /**
     * Creates a respriter object, used to change a target image colors a repeated number of times
     *
     * @param imageToRecolor template image that you wish to recolor
     * @param colorsToSwap   list fo colors that need to be changed. Each entry maps to the relative animated image frame.
     *                       If the provided list is less than the animation strip length,
     *                       only the first provided palette will be used on the whole image keeping colors consistent among different frames
     */
    private Respriter(TextureImage imageToRecolor, List<Palette> colorsToSwap) {
        if (colorsToSwap.isEmpty())
            throw new UnsupportedOperationException("Respriter must have a non empty target palette");
        // assures that frame size and palette size match
        if (imageToRecolor.frameCount() > colorsToSwap.size()) {
            //if it does not have enough colors just uses the first one
            Palette firstPalette = colorsToSwap.get(0);
            colorsToSwap = Collections.nCopies(imageToRecolor.frameCount(), firstPalette);
            this.useMergedPalette = true;
        } else this.useMergedPalette = false;
        this.imageToRecolor = imageToRecolor;
        this.originalPalettes = colorsToSwap;
    }


    /**
     * Move powerful method that recolors an image using the palette from the provided image,
     * and uses its animation data
     * Does not modify any of the given palettes
     */
    public TextureImage recolorWithAnimationOf(TextureImage textureImage) {
        return recolorWithAnimation(List.of(Palette.fromImage(textureImage)), textureImage.getMcMeta());
    }

    //TODO: generalize and merge these two

    @Deprecated(forRemoval = true)
    public TextureImage recolorWithAnimation(List<Palette> targetPalettes, @Nullable AnimationMetadataSection targetAnimationData) {
        return recolorWithAnimation(targetPalettes, targetAnimationData == null ? null : McMetaFile.of(targetAnimationData));
    }

    /**
     * Move powerful method that recolors an image using the palette provided and the animation data provided.
     * It will merge a new animation strip made of the first frame of the original image colored with the given colors
     * Does not modify any of the given palettes
     * In short turns a non-animated texture into an animated one
     */
    // this should only be used when you go from non-animated to animated
    public TextureImage recolorWithAnimation(List<Palette> targetPalettes, @Nullable McMetaFile targetAnimationData) {

        // in case the SOURCE texture itself has an animation we use it instead. this WILL create issues with animated planks textures but its acceptable as mcmeta of source could have more important stuff like ctm
        McMetaFile animationData = McMetaFile.merge(imageToRecolor.getMcMeta(), targetAnimationData);

        if (animationData == null) return recolor(targetPalettes);

        //is restricted to use only first original palette since it must merge a new animation following the given one
        Palette originalPalette = originalPalettes.getFirst();

        // in case the SOURCE texture itself has an animation we use it instead. this WILL create issues with animated planks textures but its acceptable as mcmeta of source could have more important stuff like ctm
        targetAnimationData = McMetaFile.merge(imageToRecolor.getMcMeta(), targetAnimationData);

        int newFrameAmount = animationData.getLogicalFrameCount();
        TextureImage texture = TextureOps.createSingleFrameAnimation(imageToRecolor,
                newFrameAmount, animationData);

        Map<Integer, ColorToColorMap> mapForFrameCache = new HashMap<>();

        boolean usesTargetAnimationColors = !useMergedPalette && (targetAnimationData == animationData);
        if (usesTargetAnimationColors && texture.frameCount() > targetPalettes.size()) {
            String s = "Target animation data has more frames than provided target palettes. " +
                    "This is not supported by the recolorWithAnimation method. Debug info: " + targetPalettes.size() +
                    " " + newFrameAmount + " " + targetAnimationData + " " + animationData;
            if (PlatHelper.isDev()) throw new IndexOutOfBoundsException(s);
            else {
                Moonlight.LOGGER.error(s);
                //TODO: fix properly
                usesTargetAnimationColors = false;
            }
        }
        boolean finalUsesTargetAnimationColors = usesTargetAnimationColors;
        texture.forEachPixel(pixel -> {
            int finalInd = finalUsesTargetAnimationColors ? pixel.frameIndex() : 0;

            //caches these for each palette
            ColorToColorMap oldToNewMap = mapForFrameCache.computeIfAbsent(finalInd, i -> {
                Palette toPalette = targetPalettes.get(finalInd);

                return ColorToColorMap.create(originalPalette, toPalette);
            });

            if (oldToNewMap != null) {

                Integer oldValue = pixel.getValue();
                Integer newValue = oldToNewMap.mapColor(oldValue);
                if (newValue != null) {
                    pixel.setValue(newValue);
                }
            }
        });
        return texture;
    }

    /**
     * @param targetPalette New palette that will be applied. Frame order will be the same
     * @return new recolored image. Copy of template if it fails
     * Does not modify any of the given palettes
     */
    public TextureImage recolor(Palette targetPalette) {
        return recolor(List.of(targetPalette));
    }

    /**
     * @param targetPalettes New palettes that will be applied. Frame order will be the same
     * @return new recolored image. Copy of template if it fails. Always remember to close the provided texture
     * Does not modify any of the given palettes
     */
    public TextureImage recolor(List<Palette> targetPalettes) {

        //if original palettes < provided palettes just use the first provided for all
        boolean onlyUseFirst = targetPalettes.size() < originalPalettes.size();

        TextureImage texture = imageToRecolor.makeCopy();
        Map<Integer, ColorToColorMap> mapForFrameCache = new HashMap<>();

        texture.forEachPixel(pixel -> {
            //caches these for each palette
            int ind = pixel.frameIndex();
            int finalInd = useMergedPalette ? 0 : ind;
            ColorToColorMap oldToNewMap = mapForFrameCache.computeIfAbsent(ind, i -> {
                Palette toPalette = onlyUseFirst ? targetPalettes.get(0) : targetPalettes.get(finalInd);
                Palette originalPalette = originalPalettes.get(finalInd);

                return ColorToColorMap.create(originalPalette, toPalette);
            });

            if (oldToNewMap != null) {

                Integer oldValue = pixel.getValue();
                Integer newValue = oldToNewMap.mapColor(oldValue);
                if (newValue != null) {
                    pixel.setValue(newValue);
                }
            }
        });
        return texture;
    }

    //boxed so it's cleaner

    /**
     * Does not modify any of the given palettes
     */
    public record ColorToColorMap(Map<Integer, Integer> map) {

        @Nullable
        public Integer mapColor(Integer color) {
            return map.get(color);
        }

        @Nullable
        public static ColorToColorMap create(Palette originalPalette, Palette toPalette) {
            //we don't want to modify the original palette for later use here, so we make a copy
            Palette copy = toPalette.copy();
            copy.matchSize(originalPalette.size(), originalPalette.getAverageLuminanceStep());
            if (copy.size() != originalPalette.size()) {
                //provided swap palette had too little colors
                return null;
            }
            //now they should be the same size
            return new ColorToColorMap(zipToMap(originalPalette.getValues(), copy.getValues()));
        }

        private static Map<Integer, Integer> zipToMap(List<PaletteColor> keys, List<PaletteColor> values) {
            return IntStream.range(0, keys.size()).boxed()
                    .collect(Collectors.toMap(i -> keys.get(i).value(), i -> values.get(i).value()));
        }

    }

}

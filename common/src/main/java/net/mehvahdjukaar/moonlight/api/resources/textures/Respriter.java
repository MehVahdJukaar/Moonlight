package net.mehvahdjukaar.moonlight.api.resources.textures;

import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import net.mehvahdjukaar.moonlight.api.platform.PlatHelper;
import net.mehvahdjukaar.moonlight.core.Moonlight;
import net.mehvahdjukaar.moonlight.core.misc.McMetaFile;
import net.minecraft.client.resources.metadata.animation.AnimationMetadataSection;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Respriter {

    private final TextureImage imageToRecolor;
    //one palette for each frame (this is guaranteed). frame order will be the same
    //these represent the colors that for each frames will be swapped
    private final List<Palette> originalPalettes;
    //if provided, only applies recoloring process to the given areas. Most of the time this isn't needed as it's coveted by the palettes
    @Nullable
    private final Sampler2D recoloringMask;

    /**
     * Base respriter. Automatically grabs a palette from this image and swaps it in recolorImage with the other one provided
     *
     * @param imageToRecolor base image that needs to be recolored
     */
    public static Respriter of(TextureImage imageToRecolor) {
        return new Respriter(imageToRecolor, Palette.fromAnimatedImage(imageToRecolor, null, 0), null);
    }

    /**
     * Only includes colors from the target image following the provided mask
     *
     * @param imageToRecolor base image that needs to be recolored
     */
    public static Respriter masked(TextureImage imageToRecolor, TextureImage colorMask) {
        return new Respriter(imageToRecolor, List.of(Palette.fromImage(imageToRecolor, colorMask, 0)), colorMask);
    }

    public static Respriter ofPalette(TextureImage imageToRecolor, List<Palette> colorsToSwap) {
        return new Respriter(imageToRecolor, colorsToSwap, null);
    }

    /**
     * Creates a respriter object, used to change a target image colors a repeated number of times
     *
     * @param imageToRecolor template image that you wish to recolor
     * @param colorsToSwap   palette containing colors that need to be changed.
     *                       Does not care about animated texture and will not treat each frame individually
     */
    public static Respriter ofPalette(TextureImage imageToRecolor, Palette colorsToSwap) {
        return new Respriter(imageToRecolor, List.of(colorsToSwap), null);
    }

    /**
     * Creates a respriter object, used to change a target image colors a repeated number of times
     *
     * @param imageToRecolor template image that you wish to recolor
     * @param colorsToSwap   list fo colors that need to be changed. Each entry maps to the relative animated image frame.
     *                       If the provided list is less than the animation strip length,
     *                       only the first provided palette will be used on the whole image keeping colors consistent among different frames
     */
    private Respriter(TextureImage imageToRecolor, List<Palette> colorsToSwap, @Nullable Sampler2D recoloringMask) {
        if (colorsToSwap.isEmpty())
            throw new UnsupportedOperationException("Respriter must have a non empty target palette");
        this.imageToRecolor = imageToRecolor;
        this.originalPalettes = colorsToSwap;
        this.recoloringMask = recoloringMask;
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

    /**
     * @deprecated use {@link #recolorWithAnimation(List, McMetaFile)}
     */
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
        McMetaFile mergedAnimationData = McMetaFile.merge(imageToRecolor.getMcMeta(), targetAnimationData);

        if (mergedAnimationData == null) return recolor(targetPalettes);

        //is restricted to use only first original palette since it must merge a new animation following the given one
        TextureImage texture = TextureOps.createSingleFrameAnimation(imageToRecolor, mergedAnimationData);

        //if it uses more than targetPalette first frame
        int newImageFrameCount = texture.frameCount();
        if (targetAnimationData != mergedAnimationData) {
            //just use first frame if we are not using our own animation data
            targetPalettes = Collections.nCopies(newImageFrameCount, targetPalettes.get(0));
        } else if (newImageFrameCount > targetPalettes.size()) {
            //if we are using ouw own target animation and the palette we were given didnt match then we have a problem.probably badly configured
            String s = "Target animation data has more frames than provided target palettes. " +
                    "This is not supported by the recolorWithAnimation method. Debug info: " + targetPalettes.size() +
                    " " + newImageFrameCount + " " + targetAnimationData + " " + mergedAnimationData;
            if (PlatHelper.isDev()) throw new IndexOutOfBoundsException(s);
            else {
                Moonlight.LOGGER.error(s);
                //still use first color only
                targetPalettes = Collections.nCopies(newImageFrameCount, targetPalettes.get(0));
            }
        }//else we use the provided animation & metadata together. They always have to match

        FrameColorRemapper colorRemapper = new FrameColorRemapper(originalPalettes, targetPalettes);

        texture.forEachPixel(pixel -> {
            int ind = pixel.frameIndex();
            Integer newColor = colorRemapper.remapColor(ind, pixel.getValue());
            if (newColor != null) {
                pixel.setValue(newColor);
            }
        });

        return texture;
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
        FrameColorRemapper colorRemapper = new FrameColorRemapper(originalPalettes, targetPalettes);

        texture.forEachPixel(pixel -> {
            int ind = pixel.frameIndex();
            Integer newColor = colorRemapper.remapColor(ind, pixel.getValue());
            if (newColor != null) {
                pixel.setValue(newColor);
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


    //boxed so it's cleaner

    /**
     * Does not modify any of the given palettes
     */
    private record ColorToColorMap(Int2ObjectArrayMap<Integer> map) {

        @Nullable
        public Integer mapColor(int color) {
            return map.get(color);
        }

        @Nullable
        public static ColorToColorMap create(Palette originalPalette, Palette toPalette) {
            //we don't want to modify the original palette for later use here, so we make a copy
            toPalette = toPalette.copy();
            toPalette.matchSize(originalPalette.size(), originalPalette.getAverageLuminanceStep());
            if (toPalette.size() != originalPalette.size()) {
                //provided swap palette had too little colors
                return null;
            }
            //now they should be the same size
            return new ColorToColorMap(zipToMap(originalPalette.getValues(), toPalette.getValues()));
        }

        private static Int2ObjectArrayMap<Integer> zipToMap(List<PaletteColor> keys, List<PaletteColor> values) {
            Int2ObjectArrayMap<Integer> map = new Int2ObjectArrayMap<>(keys.size());
            for (int i = 0; i < keys.size(); i++) {
                map.put(keys.get(i).value(), (Integer) values.get(i).value());
            }
            return map;
        }

    }

    private static class FrameColorRemapper {

        private final Int2ObjectArrayMap<ColorToColorMap> colorMappingsPerFrame = new Int2ObjectArrayMap<>();
        private final List<Palette> originalPalettes;
        private final List<Palette> targetPalettes;

        public FrameColorRemapper(List<Palette> originalPalettes, List<Palette> targetPalettes) {
            this.originalPalettes = originalPalettes;
            this.targetPalettes = targetPalettes;
        }

        @Nullable
        public Integer remapColor(int frameIndex, int color) {
            //caches these for each palette
            var map = colorMappingsPerFrame.computeIfAbsent(frameIndex, i -> {
                Palette toPalette = targetPalettes.get(frameIndex);
                Palette originalPalette = originalPalettes.get(frameIndex);

                return ColorToColorMap.create(originalPalette, toPalette);
            });
            if (map == null) return null;
            return map.mapColor(color);
        }
    }

}

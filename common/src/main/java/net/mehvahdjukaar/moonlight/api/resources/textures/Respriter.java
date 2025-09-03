package net.mehvahdjukaar.moonlight.api.resources.textures;

import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import net.mehvahdjukaar.moonlight.core.Moonlight;
import net.mehvahdjukaar.moonlight.core.misc.McMetaFile;
import net.minecraft.client.resources.metadata.animation.AnimationMetadataSection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class Respriter {

    private final TextureImage imageToRecolor;
    //single palette for whole image.Recoloring frame by frame does not make much sense
    private final Palette originalPalette;
    //if provided, only applies recoloring process to the given areas. Most of the time this isn't needed as it's coveted by the palettes
    @Nullable
    private final Sampler2D recoloringMask;

    /**
     * Base respriter. Automatically grabs a palette from this image and swaps it in recolorImage with the other one provided
     *
     * @param imageToRecolor base image that needs to be recolored
     */
    public static Respriter of(TextureImage imageToRecolor) {
        return new Respriter(imageToRecolor, Palette.fromImage(imageToRecolor, null, 0), null);
    }

    /**
     * Only includes colors from the target image following the provided mask
     *
     * @param imageToRecolor base image that needs to be recolored
     */
    public static Respriter masked(TextureImage imageToRecolor, TextureImage colorMask) {
        return new Respriter(imageToRecolor, Palette.fromImage(imageToRecolor, colorMask, 0), colorMask);
    }

    @Deprecated(forRemoval = true)
    public static Respriter ofPalette(TextureImage imageToRecolor, List<Palette> colorsToSwap) {
        return new Respriter(imageToRecolor, Palette.merge(colorsToSwap.toArray(Palette[]::new)), null);
    }

    /**
     * Creates a respriter object, used to change a target image colors a repeated number of times
     *
     * @param imageToRecolor template image that you wish to recolor
     * @param colorsToSwap   palette containing colors that need to be changed.
     *                       Does not care about animated texture and will not treat each frame individually
     */
    public static Respriter ofPalette(TextureImage imageToRecolor, Palette colorsToSwap) {
        return new Respriter(imageToRecolor, colorsToSwap, null);
    }

    /**
     * Creates a respriter object, used to change a target image colors a repeated number of times
     *
     * @param imageToRecolor template image that you wish to recolor
     * @param colorsToSwap   list fo colors that need to be changed. Each entry maps to the relative animated image frame.
     *                       If the provided list is less than the animation strip length,
     *                       only the first provided palette will be used on the whole image keeping colors consistent among different frames
     */
    private Respriter(TextureImage imageToRecolor, Palette colorsToSwap, @Nullable Sampler2D recoloringMask) {
        if (colorsToSwap.isEmpty())
            throw new UnsupportedOperationException("Respriter must have a non empty target palette");
        this.imageToRecolor = imageToRecolor;
        this.originalPalette = colorsToSwap;
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
        if (targetPalettes.isEmpty()) {
            Moonlight.crashIfInDev("Respriter was given no palettes!");
            return imageToRecolor.makeCopy();
        }

        // in case the SOURCE texture itself has an animation we use it instead. this WILL create issues with animated planks textures but its acceptable as mcmeta of source could have more important stuff like ctm
        @Nullable
        McMetaFile mergedAnimationData = McMetaFile.merge(imageToRecolor.getMcMeta(), targetAnimationData);

        //is restricted to use only first original palette since it must merge a new animation following the given one
        int originalFrameCount = imageToRecolor.frameCount();
        //if we have multiple frames we use the original image as a base and recolor with single palette, otherwise we clone it and recolor it with the new palettes
        TextureImage outputTexture = (originalFrameCount == 1 && mergedAnimationData != null) ?
                TextureOps.createSingleFrameAnimation(imageToRecolor, mergedAnimationData) :
                imageToRecolor.makeCopy();

        FrameColorRemapper colorRemapper = FrameColorRemapper.of(originalPalette, originalFrameCount,
                targetPalettes, outputTexture.frameCount());

        //TODO: add proper mask here. not just a color whitelist like its now
        outputTexture.forEachPixel(pixel -> {
            int ind = pixel.frameIndex();
            Integer newColor = colorRemapper.remapColor(ind, pixel.getValue());
            if (newColor != null) {
                pixel.setValue(newColor);
            }
        });

        return outputTexture;
    }

    /**
     * @param targetPalettes New palettes that will be applied. Frame order will be the same
     * @return new recolored image. Copy of template if it fails. Always remember to close the provided texture
     * Does not modify any of the given palettes
     */
    public TextureImage recolor(List<Palette> targetPalettes) {
        return recolorWithAnimation(targetPalettes, (McMetaFile) null);
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
    private record Color2ColorMap(Int2ObjectArrayMap<Integer> map) {
        static final Color2ColorMap EMPTY = new Color2ColorMap(new Int2ObjectArrayMap<>(0));

        @Nullable
        public Integer mapColor(int color) {
            return map.get(color);
        }

        @NotNull
        public static Respriter.Color2ColorMap create(Palette originalPalette, Palette toPalette) {
            //we don't want to modify the original palette for later use here, so we make a copy
            toPalette = toPalette.copy();
            toPalette.matchSize(originalPalette.size(), originalPalette.getAverageLuminanceStep());
            if (toPalette.size() != originalPalette.size()) {
                Moonlight.LOGGER.error("Failed to create Color2ColorMap. Too few colors in toPalette");
                //provided swap palette had too little colors
                return EMPTY;
            }
            //now they should be the same size
            return new Color2ColorMap(zipToMap(originalPalette.getValues(), toPalette.getValues()));
        }

        private static Int2ObjectArrayMap<Integer> zipToMap(List<PaletteColor> keys, List<PaletteColor> values) {
            Int2ObjectArrayMap<Integer> map = new Int2ObjectArrayMap<>(keys.size());
            for (int i = 0; i < keys.size(); i++) {
                map.put(keys.get(i).value(), (Integer) values.get(i).value());
            }
            return map;
        }

    }

    @FunctionalInterface
    private interface FrameColorRemapper {


        static FrameColorRemapper of(Palette originalPalette, int originalFrameCount,
                                     List<Palette> targetPalettes, int targetFrameCount) {

            boolean invalidSize = targetFrameCount > targetPalettes.size();
            if (originalFrameCount != 1 || invalidSize) {
                //it means original image is animated. Just use first palette given
                Color2ColorMap singleColorMap = Color2ColorMap.create(originalPalette, targetPalettes.getFirst());

                return (frameIndex, color) -> singleColorMap.mapColor(color);
            } else {
                List<Color2ColorMap> mappingPerFrame = new ArrayList<>();
                for (int i = 0; i < targetFrameCount; i++) {
                    Palette toPalette = targetPalettes.get(i);
                    mappingPerFrame.add(Color2ColorMap.create(originalPalette, toPalette));
                }

                return new FrameColorRemapper() {
                    @Override
                    public @Nullable Integer remapColor(int frameIndex, int color) {
                        Color2ColorMap colorMap = mappingPerFrame.get(frameIndex);
                        if (colorMap != null) return colorMap.mapColor(color);
                        return null;
                    }
                };
            }
        }

        @Nullable
        Integer remapColor(int frameIndex, int color);
    }

}

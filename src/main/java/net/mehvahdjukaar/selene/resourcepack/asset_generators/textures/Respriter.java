package net.mehvahdjukaar.selene.resourcepack.asset_generators.textures;

import com.mojang.blaze3d.platform.NativeImage;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Respriter {

    private final TextureImage imageToRecolor;
    //one palette for each frame. frame order will be the same
    private final List<Palette> originalPalettes;

    /**
     * Dynamic mode. Just automatically grabs a palette from this image and swaps it in recolorImage with the other one provided
     *
     * @param imageToRecolor base image that needs to be recolored
     */
    public Respriter(TextureImage imageToRecolor) {
        this(imageToRecolor, Palette.fromAnimatedImage(imageToRecolor, null, 0));
    }

    public Respriter(TextureImage imageToRecolor, TextureImage colorMask) {
        this(imageToRecolor, Palette.fromAnimatedImage(imageToRecolor, colorMask, 0));
    }

    /**
     * Creates a respriter object, used to change a target image colors a repeated number of times
     *
     * @param imageToRecolor template image that you wish to recolor
     * @param colorsToSwap   list fo colors that need to be changed. Each entry maps to the relative animated image frame.
     *                       If the provided list is less than the animation strip length only the first provided palette will be used on the whole image
     */
    public Respriter(TextureImage imageToRecolor, List<Palette> colorsToSwap) {
        if(colorsToSwap.size() == 0) throw new UnsupportedOperationException("Respriter must have a non empty target palette");
        //assures that frames size and palette size match
        if (imageToRecolor.framesSize() > colorsToSwap.size()) {
            //if it does not have enough colors just uses the first one
            var firstPalette = colorsToSwap.get(0);
            for (int i = 0; i < imageToRecolor.framesSize(); i++) {
                colorsToSwap.set(i, firstPalette);
            }
        }
        this.imageToRecolor = imageToRecolor;
        this.originalPalettes = colorsToSwap;
    }

    /**
     * Creates a respriter object, used to change a target image colors a repeated number of times
     *
     * @param imageToRecolor template image that you wish to recolor
     * @param colorsToSwap   palette containing colors that need to be changed.
     *                       Does not care about animated texture and will not treat each frame individually
     */
    public Respriter(TextureImage imageToRecolor, Palette colorsToSwap) {
        this(imageToRecolor, List.of(colorsToSwap));
    }

    /**
     * Move powerful method that recolors an image using the palette from the provided image and using its animation data
     *
     * @return
     */
    public TextureImage recolorImageAndMatchAnimation(TextureImage textureImage) {
        var list = Palette.fromAnimatedImage(textureImage);
        return null;
    }

    /**
     * @param targetPalette New palette that will be applied. Frame order will be the same
     * @return new recolored image. Copy of template if it fails
     */
    public TextureImage recolorImage(Palette targetPalette) {
        return recolorImage(List.of(targetPalette));
    }

    /**
     * @param targetPalettes New palettes that will be applied. Frame order will be the same
     * @return new recolored image. Copy of template if it fails. Always remember to close the provided texture
     */
    public TextureImage recolorImage(List<Palette> targetPalettes) {

        //if original palettes < provided palettes just use the first provided for all
        boolean onlyUseFirst = targetPalettes.size() < originalPalettes.size();

        TextureImage texture = imageToRecolor.makeCopy();
        NativeImage img = texture.getImage();

        Map<Integer, ColorToColorMap> mapForFrameCache = new HashMap<>();

        texture.forEachFrame((ind, x, y) -> {
            //caches these for each palette
            ColorToColorMap oldToNewMap = mapForFrameCache.computeIfAbsent(ind, i -> {
                Palette toPalette = onlyUseFirst ? targetPalettes.get(0) : targetPalettes.get(ind);
                Palette originalPalette = originalPalettes.get(ind);

                return ColorToColorMap.create(originalPalette, toPalette);
            });

            if (oldToNewMap != null) {

                Integer oldValue = img.getPixelRGBA(x, y);
                Integer newValue = oldToNewMap.mapColor(oldValue);
                if (newValue != null) {
                    img.setPixelRGBA(x, y, newValue);
                }
            }
        });
        return texture;
    }

    //boxed so it's cleaner
    private record ColorToColorMap(Map<Integer, Integer> map) {

        @Nullable
        public Integer mapColor(Integer color) {
            return map.get(color);
        }

        @Nullable
        public static ColorToColorMap create(Palette originalPalette, Palette toPalette) {
            toPalette.matchSize(originalPalette.size());
            if (toPalette.size() != originalPalette.size()) {
                //provided swap palette had too little colors
                return null;
            }
            //now they should be same size
            return new ColorToColorMap(zipToMap(originalPalette.getValues(), toPalette.getValues()));
        }

        private static Map<Integer, Integer> zipToMap(List<PaletteColor> keys, List<PaletteColor> values) {
            return IntStream.range(0, keys.size()).boxed()
                    .collect(Collectors.toMap(i -> keys.get(i).value(), i -> values.get(i).value()));
        }

    }

}

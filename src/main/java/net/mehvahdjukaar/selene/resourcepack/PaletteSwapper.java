package net.mehvahdjukaar.selene.resourcepack;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.datafixers.util.Pair;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class PaletteSwapper {

    private final NativeImage templateImage;
    private final List<PaletteColor> originalPalette;

    public final boolean autoDetectsPalette;

    /**
     * Dynamic mode. Just automatically grabs a palette from this image and swaps it in recolorImage with the other one provided
     * @param imageToRecolor base image that needs to be recolored
     */
    public PaletteSwapper(NativeImage imageToRecolor) {
        this.templateImage = imageToRecolor;
        this.originalPalette = grabImagePalette(imageToRecolor);
        this.autoDetectsPalette = true;
    }

    /**
     * Static mode
     * @param templateImage template image to be recolored
     * @param grabPaletteFrom base texture from which the palette will be grabbed. Palette should match the one in the template image
     *                        Ideally one should provide textures in recolorImage that are just recolored versions of this image(I.E: wood types)
     */
    public PaletteSwapper(NativeImage templateImage, NativeImage grabPaletteFrom) {
        this.templateImage = templateImage;
        this.originalPalette = grabImagePalette(grabPaletteFrom);
        this.autoDetectsPalette = false;
    }

    /**
     *
     * @param targetPaletteImage Image containing the new desired palette. If is non dynamic should be the same format as the one provided in the constructor.
     * @return recolored image. Template if it fails
     */
    @SuppressWarnings("UnnecessaryLocalVariable")
    public NativeImage recolorImage(NativeImage targetPaletteImage) {
        Map<Integer, Integer> paletteMap = getColorToColorMap(targetPaletteImage);
        NativeImage newImage = new NativeImage(templateImage.getWidth(),templateImage.getHeight(), false);
        if (paletteMap != null) {

            newImage.copyFrom(this.templateImage);

            for (int x = 0; x < newImage.getWidth(); x++) {
                for (int y = 0; y < newImage.getHeight(); y++) {
                    int oldValue = newImage.getPixelRGBA(x, y);
                    Integer newValue = paletteMap.get(oldValue);
                    if (newValue != null) {
                        newImage.setPixelRGBA(x, y, newValue);
                    }
                }
            }
        }
        return newImage;
    }

    @Nullable
    private Map<Integer, Integer> getColorToColorMap(NativeImage newPalette) {
        if (!this.autoDetectsPalette && false) {
            return this.originalPalette.stream().collect(Collectors.toMap(p -> p.color, p -> newPalette.getPixelRGBA(p.x, p.y)));
        } else {
            List<PaletteColor> toPalette = grabImagePalette(newPalette);
            maybeReducePalette(toPalette);
            maybeIncreasePalette(toPalette);
            if (toPalette.size() != this.originalPalette.size()) {
                //provided swap palette had too little colors
                return null;
            }
            //now they should be same size
            return zipToMap(this.originalPalette, toPalette);
        }
    }

    private void maybeIncreasePalette(List<PaletteColor> toPalette) {
        while (toPalette.size() < this.originalPalette.size()) {
            float lastLum = 0;
            int ind = 0;
            for (int i = 1; i<toPalette.size(); i++) {
                float d = toPalette.get(i).luminance-toPalette.get(i-1).luminance;
                if(d>lastLum){
                    lastLum = d;
                    ind = i;
                }
            }

            int color0 = toPalette.get(ind-1).color;
            int color1 = toPalette.get(ind).color;
            int r = (int) ((NativeImage.getR(color0)+NativeImage.getR(color1))/2f);
            int g = (int) ((NativeImage.getG(color0)+NativeImage.getG(color1))/2f);
            int b = (int) ((NativeImage.getB(color0)+NativeImage.getB(color1))/2f);
            int newColor = NativeImage.combine(255,b,g,r);

            toPalette.add(new PaletteColor(0,0,newColor));
            toPalette.sort((a, aa) -> Float.compare(a.luminance, aa.luminance));
        }
    }

    private void maybeReducePalette(List<PaletteColor> toPalette) {
        while (toPalette.size() > this.originalPalette.size()) {
            PaletteColor toRemove = toPalette.get(0);
            for (var p : toPalette) {
                if (p.occurrence < toRemove.occurrence) {
                    toRemove = p;
                }
            }
            toPalette.remove(toRemove);
        }
    }

    private Map<Integer, Integer> zipToMap(List<PaletteColor> keys, List<PaletteColor> values) {
        return IntStream.range(0, keys.size()).boxed()
                .collect(Collectors.toMap(i -> keys.get(i).color, i -> values.get(i).color));
    }

    private List<PaletteColor> grabImagePalette(NativeImage image) {
        Map<Integer, PaletteColor> map = new HashMap<>();
        for (int x = 0; x < image.getWidth(); x++) {
            for (int y = 0; y < image.getHeight(); y++) {
                int color = image.getPixelRGBA(x, y);

                int finalX = x;
                int finalY = y;
                var paletteColor = map.computeIfAbsent(color,
                        p -> new PaletteColor(finalX, finalY, color));
                paletteColor.occurrence++;
            }
        }
        return map.values().stream().sorted((a, b) -> Float.compare(a.luminance, b.luminance))
                .collect(Collectors.toList());
    }

    private static class PaletteColor {
        public final int color;
        public final float luminance ;
        public final int x;
        public final int y;
        public int occurrence = 0;


        public PaletteColor(int x, int y, int color) {
            this.x = x;
            this.y = y;
            this.color = color;
            int r = NativeImage.getR(color);
            int g = NativeImage.getG(color);
            int b = NativeImage.getB(color);
            this.luminance = (0.299f * r + 0.587f * g + 0.114f * b);
        }
    }

}

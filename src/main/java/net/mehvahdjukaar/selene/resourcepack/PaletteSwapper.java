package net.mehvahdjukaar.selene.resourcepack;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.datafixers.util.Pair;
import org.jetbrains.annotations.Nullable;

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
     * @param targetPaletteImage if is non dynamic should be the same format as the one provided in the constructor.
     * @return recolored image. Template if it fails
     */
    @SuppressWarnings("UnnecessaryLocalVariable")
    public NativeImage recolorImage(NativeImage targetPaletteImage) {
        Map<Integer, Integer> paletteMap = getColorToColorMap(targetPaletteImage);
        NativeImage newImage = targetPaletteImage;
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
        if (!this.autoDetectsPalette) {
            return this.originalPalette.stream().collect(Collectors.toMap(p -> p.color, p -> newPalette.getPixelRGBA(p.x, p.y)));
        } else {
            List<PaletteColor> toPalette = grabImagePalette(newPalette);
            while (toPalette.size() > this.originalPalette.size()) {
                PaletteColor toRemove = toPalette.get(0);
                for (var p : toPalette) {
                    if (p.occurrence < toRemove.occurrence) {
                        toRemove = p;
                    }
                }
                toPalette.remove(toRemove);
            }
            if (toPalette.size() != this.originalPalette.size()) {
                //provided swap palette had too little colors
                return null;
            }
            //now they should be same size
            return zipToMap(this.originalPalette, toPalette);
        }
    }

    private Map<Integer, Integer> zipToMap(List<PaletteColor> keys, List<PaletteColor> values) {
        return IntStream.range(0, keys.size()).boxed()
                .collect(Collectors.toMap(i -> keys.get(i).color, i -> values.get(i).color));
    }

    private List<PaletteColor> grabImagePalette(NativeImage image) {
        Map<Integer, Pair<PaletteColor, Float>> map = new HashMap<>();
        for (int x = 0; x < image.getWidth(); x++) {
            for (int y = 0; y < image.getHeight(); y++) {
                int color = image.getPixelRGBA(x, y);
                int r = NativeImage.getR(color);
                int g = NativeImage.getG(color);
                int b = NativeImage.getB(color);
                float luminance = (0.299f * r + 0.587f * g + 0.114f * b);
                int finalX = x;
                int finalY = y;
                var paletteColor = map.computeIfAbsent(color,
                        p -> Pair.of(new PaletteColor(finalX, finalY, color), luminance));
                paletteColor.getFirst().occurrence++;
            }
        }
        return map.values().stream().sorted((a, b) -> Float.compare(a.getSecond(), b.getSecond()))
                .map(Pair::getFirst).collect(Collectors.toList());
    }

    private static class PaletteColor {
        public final int color;
        public final int x;
        public final int y;
        public int occurrence = 0;

        public PaletteColor(int x, int y, int color) {
            this.x = x;
            this.y = y;
            this.color = color;
        }
    }

}

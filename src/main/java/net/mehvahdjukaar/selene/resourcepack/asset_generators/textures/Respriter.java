package net.mehvahdjukaar.selene.resourcepack.asset_generators.textures;

import com.mojang.blaze3d.platform.NativeImage;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Respriter {

    private final NativeImage templateImage;
    private final Palette originalPalette;

    public Respriter(NativeImage templateImage) {
        this(templateImage, templateImage);
    }


    public Respriter(NativeImage templateImage, Palette originalPalette) {
        this.templateImage = templateImage;
        this.originalPalette = originalPalette;
    }

    /**
     * Dynamic mode. Just automatically grabs a palette from this image and swaps it in recolorImage with the other one provided
     *
     * @param imageToRecolor base image that needs to be recolored
     */
    public Respriter(NativeImage imageToRecolor, NativeImage paletteFrom) {
        this(imageToRecolor,Palette.fromImage(paletteFrom, null));
    }

    /**
     * @param targetPalette Image containing the new desired palette. If is non dynamic should be the same format as the one provided in the constructor.
     * @return new recolored image. Template if it fails
     */
    public NativeImage recolorImage(Palette targetPalette) {
        Map<Integer, Integer> paletteMap = getColorToColorMap(targetPalette);
        NativeImage newImage = new NativeImage(templateImage.getWidth(), templateImage.getHeight(), false);
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

    public NativeImage recolorImage(NativeImage targetPaletteImage, @Nullable NativeImage mask) {
        return recolorImage(Palette.fromImage(targetPaletteImage, mask));
    }

    @Nullable
    private Map<Integer, Integer> getColorToColorMap(Palette toPalette) {
        toPalette.matchSize(this.originalPalette.size());
        if (toPalette.size() != this.originalPalette.size()) {
            //provided swap palette had too little colors
            return null;
        }
        //now they should be same size
        return zipToMap(this.originalPalette.getValues(), toPalette.getValues());
    }

    private Map<Integer, Integer> zipToMap(List<PaletteColor> keys, List<PaletteColor> values) {
        return IntStream.range(0, keys.size()).boxed()
                .collect(Collectors.toMap(i -> keys.get(i).color, i -> values.get(i).color));
    }


}

package net.mehvahdjukaar.selene.resourcepack.asset_generators.textures;

import com.mojang.blaze3d.platform.NativeImage;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class Palette {

    private static final Comparator<PaletteColor> COMPARATOR = (a, b) -> Float.compare(a.luminance, b.luminance);

    private final ArrayList<PaletteColor> internal = new ArrayList<>();

    //ordered from darkest to lightest
    public int size() {
        return internal.size();
    }

    public List<PaletteColor> getValues() {
        return internal;
    }

    private void sort() {
        internal.sort(COMPARATOR);
    }


    public Palette(Collection<PaletteColor> colors) {
        this.internal.addAll(colors);
        this.sort();
    }

    public void add(PaletteColor color) {
        internal.add(color);
        this.sort();
    }

    public void add(int index, PaletteColor color) {
        internal.add(index, color);
        this.sort();
    }

    public void set(int index, PaletteColor color) {
        internal.set(index, color);
    }

    public PaletteColor get(int index) {
        return internal.get(index);
    }

    public PaletteColor getDarkest() {
        return get(0);
    }

    public PaletteColor getLightest() {
        return get(internal.size() - 1);
    }

    public void remove(int index) {
        internal.remove(index);
        this.sort();
    }

    public void remove(PaletteColor color) {
        internal.remove(color);
        this.sort();
    }

    public int calculateAverage() {
        return SpriteUtils.averageColors(this.internal.stream().map(c -> c.color).toArray(Integer[]::new));
    }

    public static Palette fromImage(NativeImage image) {
        return fromImage(image, null);
    }

    public static Palette fromImage(NativeImage image, @Nullable NativeImage mask) {
        Map<Integer, PaletteColor> map = new HashMap<>();
        for (int x = 0; x < image.getWidth(); x++) {
            for (int y = 0; y < image.getHeight(); y++) {
                if (mask == null || NativeImage.getA(mask.getPixelRGBA(x, y)) == 0) {
                    int color = image.getPixelRGBA(x, y);
                    if (NativeImage.getA(color) != 0) {
                        int finalX = x;
                        int finalY = y;
                        var paletteColor = map.computeIfAbsent(color,
                                p -> new PaletteColor(finalX, finalY, color));
                        paletteColor.occurrence++;
                    }
                }
            }
        }
        if (map.size() == 0) throw new UnsupportedOperationException("Palette mask must not cover the whole image");
        return new Palette(map.values());
    }

    public void matchSize(int targetSize) {
        if (this.size() <= 0 || targetSize <= 0) {
            throw new UnsupportedOperationException("Palette size can't be 0");
        }
        this.maybeReducePalette(targetSize);
        this.maybeIncreasePalette(targetSize);
    }

    private void maybeReducePalette(int targetSize) {
        //remove the one with least occurrence
        while (this.internal.size() > targetSize) {
            PaletteColor toRemove = internal.get(0);
            for (var p : internal) {
                if (p.occurrence < toRemove.occurrence) {
                    toRemove = p;
                }
            }
            internal.remove(toRemove);
        }
        this.sort();
    }

    private void maybeIncreasePalette(int targetSize) {
        //adds a color in the space between the two colors that differ the most
        while (internal.size() < targetSize) {
            float lastLum = 0;
            int ind = 0;
            for (int i = 1; i < internal.size(); i++) {
                float d = internal.get(i).luminance - internal.get(i - 1).luminance;
                if (d > lastLum) {
                    lastLum = d;
                    ind = i;
                }
            }

            int newColor = SpriteUtils.averageColors(internal.get(ind - 1).color, internal.get(ind).color);

            internal.add(new PaletteColor(0, 0, newColor));
            this.sort();
        }
    }

    //add a highlight color
    public void increaseUp() {
        //float averageDeltaLum = (this.getLightest().luminance - this.getDarkest().luminance)/this.size()-1;
        var lightest = this.getLightest();
        var secondLightest = this.get(this.size() - 2);
        //float newLum = lightest.luminance+averageDeltaLum;
        var h1 = SpriteUtils.RGBtoHSV(lightest.color);
        var h2 = SpriteUtils.RGBtoHSV(secondLightest.color);
        //float lum1 = lightest.luminance;
        // float lum2 = secondLightest.luminance;
        float v1 = h1[2];
        float v2 = h2[2];
        float dv = v2 - v1;
        float hue1 = h1[0];
        float hue2 = h2[0];
        float dh = hue2 - hue1;
        float sat1 = h1[1];
        float sat2 = h2[1];
        float ds = sat2 - sat1;
        float newHue = hue1 + (dh / dv) * 2 * dv;
        float newSat = sat1 + (ds / dv) * 2 * dv;
        float newVal = v1 + dv;
        this.add(new PaletteColor(SpriteUtils.HSVtoRGB(newHue, newSat, newVal)));
    }

    //add a dark color
    public void increaseDown() {
        //float averageDeltaLum = (this.getLightest().luminance - this.getDarkest().luminance)/this.size()-1;
        var darkest = this.getDarkest();
        var secondDarkest = this.get(1);
        //float newLum = lightest.luminance+averageDeltaLum;
        var h2 = SpriteUtils.RGBtoHSV(darkest.color);
        var h1 = SpriteUtils.RGBtoHSV(secondDarkest.color);
        //float lum1 = lightest.luminance;
        // float lum2 = secondLightest.luminance;
        float v1 = h1[2];
        float v2 = h2[2];
        float dv = v2 - v1;
        float hue1 = h1[0];
        float hue2 = h2[0];
        float dh = hue2 - hue1;
        float sat1 = h1[1];
        float sat2 = h2[1];
        float ds = sat2 - sat1;
        float newHue = hue2 - (dh * dv);
        float newSat = sat2 - (ds * dv);
        float newVal = v2 + dv;
        this.add(new PaletteColor(SpriteUtils.HSVtoRGB(newHue, newSat, newVal)));
    }


}
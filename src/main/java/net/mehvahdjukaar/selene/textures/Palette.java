package net.mehvahdjukaar.selene.textures;

import com.mojang.blaze3d.platform.NativeImage;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class Palette {
    private static final Comparator<PaletteColor> COMPARATOR = (a, b) -> Float.compare(a.luminance, b.luminance);

    private final ArrayList<PaletteColor> internal = new ArrayList<>();

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


    public void remove(int index) {
        internal.remove(index);
        this.sort();
    }

    public void remove(PaletteColor color) {
        internal.remove(color);
        this.sort();
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
                    if(NativeImage.getA(color)!=0) {
                        int finalX = x;
                        int finalY = y;
                        var paletteColor = map.computeIfAbsent(color,
                                p -> new PaletteColor(finalX, finalY, color));
                        paletteColor.occurrence++;
                    }
                }
            }
        }
        return new Palette(map.values());
    }

    public void matchSize(int targetSize) {
        this.maybeReducePalette(targetSize);
        this.maybeIncreasePalette(targetSize);
    }

    private void maybeReducePalette(int targetSize) {
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

            int color0 = internal.get(ind - 1).color;
            int color1 = internal.get(ind).color;

            int r = (int) ((NativeImage.getR(color0) + NativeImage.getR(color1)) / 2f);
            int g = (int) ((NativeImage.getG(color0) + NativeImage.getG(color1)) / 2f);
            int b = (int) ((NativeImage.getB(color0) + NativeImage.getB(color1)) / 2f);
            int newColor = NativeImage.combine(255, b, g, r);

            internal.add(new PaletteColor(0, 0, newColor));
            this.sort();
        }
    }
}
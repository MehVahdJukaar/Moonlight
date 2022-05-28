package net.mehvahdjukaar.selene.client.asset_generators.textures;

import com.mojang.blaze3d.platform.NativeImage;
import net.mehvahdjukaar.selene.math.MthUtils;
import net.mehvahdjukaar.selene.math.colors.BaseColor;
import net.mehvahdjukaar.selene.math.colors.HCLColor;
import net.mehvahdjukaar.selene.math.colors.LABColor;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

public class Palette {

    public static final float BASE_TOLERANCE = 1 / 200f;
    private static final Palette EMPTY = new Palette(List.of());

    private float tolerance = 0;
    //ordered from darkest to lightest (luminance)
    private final ArrayList<PaletteColor> internal = new ArrayList<>();

    protected Palette(Collection<PaletteColor> colors) {
        this.internal.addAll(colors);
        this.sort();
    }

    protected Palette(Collection<PaletteColor> colors, float tolerance) {
        this.internal.addAll(colors);
        this.sort();
        this.updateTolerance(tolerance);
    }

    public boolean isEmpty() {
        return this == EMPTY;
    }

    public Palette copy(){
        return new Palette(this.internal,tolerance);
    }

    /**
     * Changes tolerance settings and merge all colors that are close enough. Default value is always 0 which will accept any colors
     *
     * @param tolerance at what distance colors will be merged and consolidated into one
     */
    public void updateTolerance(float tolerance) {
        if (this.tolerance == tolerance) return;
        this.tolerance = tolerance;
        if (tolerance == 0) return;
        boolean recalculate;
        do {
            recalculate = false;
            for (int i = 1; i < this.size(); i++) {
                PaletteColor c0 = this.get(i - 1); //first
                PaletteColor c1 = this.get(i); //second
                if (c0.distanceTo(c1) <= tolerance) {
                    Palette tempPal = new Palette(List.of(c0, c1));
                    int after = i + 1;
                    while (after < this.size() && tempPal.calculateAverage().distanceTo(this.get(after)) <= tolerance) {
                        tempPal.add(this.get(after));
                        after++;
                    }
                    tempPal.getValues().forEach(this::remove);
                    this.add(tempPal.calculateAverage());
                    recalculate = true;
                }
            }
        } while (recalculate);
    }


    public int size() {
        return internal.size();
    }

    public List<PaletteColor> getValues() {
        return internal;
    }

    private void sort() {
        Collections.sort(internal);
    }

    private void addUnchecked(PaletteColor color) {
        if (color.rgb().alpha() == 0) return;
        internal.add(color);
        this.sort();
    }

    public void add(PaletteColor color) {
        if (color.rgb().alpha() == 0) return;
        if (!hasColor(color)) {
            internal.add(color);
            this.sort();
        }
    }

    public void set(int index, PaletteColor color) {
        if (color.rgb().alpha() == 0) return;
        if (!hasColor(color)) internal.set(index, color);
    }

    public PaletteColor get(int index) {
        return internal.get(index);
    }

    public boolean hasColor(PaletteColor color) {
        return this.hasColor(color, this.tolerance);
    }

    public boolean hasColor(PaletteColor color, float tolerance) {
        if (color.rgb().alpha() != 0) {
            for (PaletteColor c : this.getValues()) {
                if (c.distanceTo(color) <= tolerance) {
                    return true;
                }
            }
        }
        return false;
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
        if (internal.remove(color)) {
            this.sort();
        }
    }

    public PaletteColor calculateAverage() {
        return new PaletteColor(LABColor.averageColors(this.getValues().stream().map(PaletteColor::lab).toArray(LABColor[]::new)));
    }

    /**
     * Gets the color within this palette that most closely matches the average center color
     */
    public PaletteColor getCenterColor() {
        PaletteColor center = calculateAverage();
        return getColorClosestTo(center);
    }

    /**
     * Gets the color within this palette that most closely matches the given color
     */
    private PaletteColor getColorClosestTo(PaletteColor target) {
        PaletteColor bestMatch = target;
        float lastDist = 10000;
        for (var c : this.getValues()) {
            float dist = target.distanceTo(c);
            if (dist < lastDist) {
                lastDist = dist;
                bestMatch = c;
            }
        }
        return bestMatch;
    }

    /**
     * Adds or remove colors to match the target size
     */
    public void matchSize(int targetSize) {
        if (this.size() == 0 || targetSize <= 0) {
            throw new UnsupportedOperationException("Palette size can't be 0");
        }
        if (this.size() == 2) {
            var lightest = this.getLightest();
            var darkest = this.getDarkest();
            Palette other = Palette.fromArc(lightest.hcl(), darkest.hcl(), targetSize);
            this.internal.clear();
            this.internal.addAll(other.getValues());
        }
        while (this.size() > targetSize) {
            removeLeastUsed();
        }
        while (this.size() < targetSize) {
            increaseInner();
        }
    }

    /**
     * Removes one color, the one that is least used
     */
    public void removeLeastUsed() {
        //remove the one with least occurrence
        PaletteColor toRemove = internal.get(0);
        for (var p : internal) {
            if (p.occurrence < toRemove.occurrence) {
                toRemove = p;
            }
        }
        this.remove(toRemove);
    }

    /**
     * Removes one color, the one that is closest to other colors
     */
    public void reduce() {
        int index = 0;
        float minDelta = 10000;
        float lastLum = this.get(0).luminance();
        for (int i = 1; i < this.size(); i++) {
            float l = this.get(i).luminance();
            float dl = l - lastLum;
            if (dl < minDelta) {
                index = i;
                minDelta = dl;
            }
            lastLum = l;
        }
        this.remove(this.get(index));
    }

    /**
     * Calculates the average luminance different between each color. Ideally it should be somewhat constant
     */
    public float calculateAverageDeltaLuminance() {
        List<Float> list = new ArrayList<>();
        float lastLum = this.get(0).luminance();
        for (int i = 1; i < this.size(); i++) {
            float l = this.get(i).luminance();
            list.add(l - lastLum);
            lastLum = l;
        }
        float total = 0;
        for (var v : list) total += v;
        return total / (float) list.size();
    }

    /**
     * Adds a color to the palette by interpolating existing colors
     * Only works if it has at least 2 colors
     */
    public PaletteColor increaseInner() {
        assert (this.size() < 2);
        int index = 1;
        float maxDelta = 0;
        float lastLum = this.get(0).luminance();
        for (int i = 1; i < this.size(); i++) {
            float l = this.get(i).luminance();
            float dl = l - lastLum;
            if (dl > maxDelta) {
                index = i;
                maxDelta = dl;
            }
            lastLum = l;
        }
        var c1 = this.get(index).hcl();
        var c2 = this.get(index - 1).hcl();
        var newC = new PaletteColor(c1.mixWith(c2));
        //always adds, ignoring tolerance since we do want to add something
        this.addUnchecked(newC);
        return newC;
    }

    /**
     * Adds a highlight color, lighter than the lightest color present
     * Only works if it has at least 2 colors
     */
    public PaletteColor increaseUp() {
        assert (this.size() < 2);
        float averageDeltaLum = this.calculateAverageDeltaLuminance();
        HCLColor lightest = this.getLightest().hcl();
        HCLColor secondLightest = this.get(this.size() - 2).hcl();
        var cc = getNextColor(averageDeltaLum, lightest, secondLightest);
        PaletteColor pl = new PaletteColor(cc);
        this.add(pl);
        return pl;
    }

    /**
     * Adds an outline color, darker than the darkest color present
     * Only works if it has at least 2 colors
     */
    public PaletteColor increaseDown() {
        assert (this.size() < 2);
        float averageDeltaLum = this.calculateAverageDeltaLuminance();
        HCLColor darkest = this.getDarkest().hcl();
        HCLColor secondDarkest = this.get(1).hcl();
        var cc = getNextColor(-averageDeltaLum, darkest, secondDarkest);
        PaletteColor pl = new PaletteColor(cc);
        this.add(pl);
        return pl;
    }

    private HCLColor getNextColor(float lumIncrease, HCLColor source, HCLColor previous) {
        float newLum = source.luminance() + lumIncrease;
        float h1 = source.hue();
        float c1 = source.chroma();
        float a1 = source.alpha();
        float h2 = previous.hue();
        float c2 = previous.chroma();
        float a2 = previous.alpha();
        float hueIncrease = (float) (-MthUtils.signedAngleDiff(h1 * Math.PI * 2, h2 * Math.PI * 2) / (Math.PI * 2.0));
        //better be conservative here. some hue increase might look bad even if they are the same as the last hue diff
        float newH = h1 + hueIncrease * 0.5f;
        while (newH < 0) ++newH;

        float newC = c1 + (c1 - c2);
        float newA = a1 + (a1 - a2);
        return new HCLColor(newH, newC, newLum, newA);
    }

    /**
     * Combines multiple palettes into one, preserving their occurrence values
     */
    public static Palette merge(Palette... palettes) {
        if(palettes.length == 1)return new Palette(palettes[0].getValues());
        Map<Integer, PaletteColor> map = new HashMap<>();
        for (Palette p : palettes) {
            for (PaletteColor c : p.getValues()) {
                int color = c.value();
                if (map.containsKey(color)) {
                    map.get(color).occurrence += c.occurrence;
                } else map.put(color, c);
            }
        }
        if (map.values().size() == 0) return EMPTY;
        return new Palette(map.values());
    }

    public static Palette ofColors(Collection<BaseColor<?>> colors) {
        return new Palette(colors.stream().map(PaletteColor::new).collect(Collectors.toSet()));
    }

    /**
     * Creates a palette by interpolating a start and end point. Interpolation mode depends on the color space of the color provided
     *
     * @param light start color
     * @param dark  end color
     * @param size  number of colors to have
     * @param <T>   type of color. Best if you use HCL or HCLV
     * @return new Palette
     */
    public static <T extends BaseColor<T>> Palette fromArc(T light, T dark, int size) {
        List<BaseColor<T>> colors = new ArrayList<>();
        if (size <= 1) throw new IllegalArgumentException("Size must be greater than one");
        for (int i = 0; i < size; i++) {
            colors.add(dark.mixWith(light, i / (size - 1f)));
        }
        return new Palette(colors.stream().map(PaletteColor::new).collect(Collectors.toSet()));
    }

    public static Palette fromImage(TextureImage image) {
        return fromImage(image, null);
    }

    public static Palette fromImage(TextureImage image, @Nullable TextureImage mask) {
        return fromImage(image, mask, BASE_TOLERANCE);
    }

    /**
     * Grabs a palette from an image.
     * Differs from fromAnimatedImage since it will grab a palette that represents the entire image without concern over its animation frames
     * For non-animated textures these two are the same
     * If a mask is supplied it will only look at its transparent pixels
     *
     * @param textureImage target image
     * @param textureMask  mask to select which part of the image to grab colors from
     * @param tolerance    tolerance parameter which determines how close similar colors can be without being merged
     * @return new palette
     */
    public static Palette fromImage(TextureImage textureImage, @Nullable TextureImage textureMask, float tolerance) {

        //grabs separate palettes & then merges them
        List<Palette> palettes = fromAnimatedImage(textureImage, textureMask, 0);

        Palette palette = merge(palettes.toArray(new Palette[0]));
        if(tolerance != 0) palette.updateTolerance(tolerance);
        return palette;
    }

    public static List<Palette> fromAnimatedImage(TextureImage image) {
        return fromAnimatedImage(image, null);
    }

    public static List<Palette> fromAnimatedImage(TextureImage image, @Nullable TextureImage mask) {
        return fromAnimatedImage(image, mask, BASE_TOLERANCE);
    }

    /**
     * Grabs a palette list from an image. Each palette represents the colors of the given image frames.
     * If a mask is supplied it will only look at its transparent pixels
     *
     * @param textureImage target image
     * @param textureMask  mask to select which part of the image to grab colors from
     * @param tolerance    tolerance parameter which determines how close similar colors can be without being merged
     * @return new palette
     */
    public static List<Palette> fromAnimatedImage(TextureImage textureImage, @Nullable TextureImage textureMask, float tolerance) {
        if (textureMask != null &&
                (textureImage.framesSize() != textureMask.framesSize() ||
                        textureMask.frameWidth() < textureImage.frameWidth() ||
                        textureMask.frameHeight() < textureImage.frameHeight())) {
            throw new UnsupportedOperationException("Palette mask needs to be at least as large as the target image and have the same format");
        }

        List<Palette> palettes = new ArrayList<>();

        NativeImage mask = textureMask == null ? null : textureMask.getImage();
        NativeImage image = textureImage.getImage();

        List<Map<Integer, PaletteColor>> paletteBuilders = new ArrayList<>();

        textureImage.forEachFrame((index, x, y) -> {
            //when index changes we add a completed palette
            if (paletteBuilders.size() <= index) {
                paletteBuilders.add(new HashMap<>());
            }
            var builder = paletteBuilders.get(index);

            if (mask == null || NativeImage.getA(mask.getPixelRGBA(x, y)) == 0) {
                int color = image.getPixelRGBA(x, y);
                if (NativeImage.getA(color) != 0) {
                    var paletteColor = builder.computeIfAbsent(color,
                            p -> new PaletteColor(color));
                    paletteColor.occurrence++;
                }
            }
        });

        for (var p : paletteBuilders) {
            Palette pal;
            if (p.size() == 0) {
                pal = EMPTY;
            } else {
                pal = new Palette(p.values(), tolerance);
            }
            palettes.add(pal);
        }

        return palettes;
    }
}
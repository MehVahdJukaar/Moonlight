package net.mehvahdjukaar.moonlight.api.resources.textures;

import com.mojang.blaze3d.platform.NativeImage;
import net.mehvahdjukaar.moonlight.api.util.math.MthUtils;
import net.mehvahdjukaar.moonlight.api.util.math.colors.BaseColor;
import net.mehvahdjukaar.moonlight.api.util.math.colors.HCLColor;
import net.mehvahdjukaar.moonlight.api.util.math.colors.LABColor;
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

public class Palette implements Set<PaletteColor> {

    public static final float BASE_TOLERANCE = 1 / 170f;
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

    @Override
    public boolean isEmpty() {
        return internal.isEmpty();
    }

    /**
     * Makes a copy of this palette
     */
    public Palette copy() {
        return new Palette(new ArrayList<>(this.internal), tolerance);
    }

    public static Palette empty() {
        return new Palette(new ArrayList<>());
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
                        tempPal.addUnchecked(this.get(after));
                        after++;
                    }
                    tempPal.getValues().forEach(this::remove);
                    this.addUnchecked(tempPal.calculateAverage());
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

    @Override
    public boolean add(PaletteColor color) {
        if (color.rgb().alpha() == 0) return false;
        if (!hasColor(color)) {
            internal.add(color);
            this.sort();
            return true;
        }
        return false;
    }

    @Override
    public boolean addAll(@NotNull Collection<? extends PaletteColor> colors) {
        boolean added = false;
        for (var c : colors) {
            if (!hasColor(c)) {
                internal.add(c);
                added = true;
            }
        }
        if (added) {
            sort();
            return true;
        }
        return false;
    }

    public void set(int index, PaletteColor color) {
        if (color.rgb().alpha() == 0) return;
        if (!hasColor(color)) internal.set(index, color);
    }

    public PaletteColor get(int index) {
        return internal.get(index);
    }

    public int indexOf(PaletteColor color) {
        return this.internal.indexOf(color);
    }

    public boolean hasColor(int rgba) {
        return this.hasColor(new PaletteColor(rgba), 0);
    }

    public boolean hasColor(PaletteColor color) {
        return this.hasColor(color, this.tolerance);
    }

    public boolean hasColor(PaletteColor color, float tolerance) {
        if (color.rgb().alpha() != 0) {
            for (PaletteColor c : this.getValues()) {
                if (tolerance == 0) {
                    if (c.value() == color.value()) return true;
                } else {
                    if (c.distanceTo(color) <= tolerance) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public PaletteColor getDarkest() {
        return getDarkest(0);
    }

    public PaletteColor getDarkest(int offset) {
        return get(offset);
    }

    public PaletteColor getLightest() {
        return getLightest(0);
    }

    public PaletteColor getLightest(int offset) {
        return get(internal.size() - 1 - offset);
    }

    /**
     * @param index of the color to remove
     * @return removed color
     */
    public PaletteColor remove(int index) {
        return internal.remove(index);
    }

    public boolean remove(PaletteColor color) {
        return this.internal.remove(color);
    }

    @Override
    public boolean remove(Object o) {
        return this.internal.remove(o);
    }

    public boolean removeAll(Collection<?> colors) {
        if (this.internal.removeAll(colors)) {
            this.sort();
            return true;
        }
        return false;
    }

    public PaletteColor calculateAverage() {
        return new PaletteColor(LABColor.averageColors(this.getValues().stream().map(PaletteColor::lab).toArray(LABColor[]::new)));
    }

    /**
     * Grabs a color closest to the given value with 0 being the first color, 1 being the last
     *
     * @param slope percentage of palette. from 0 to 1
     */
    public PaletteColor getColorAtSlope(float slope) {
        int index = Math.round((internal.size() - 1) * slope);
        return internal.get(index);
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
    public PaletteColor getColorClosestTo(PaletteColor target) {
        PaletteColor bestMatch = target;
        float lastDist = Float.MAX_VALUE;
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
        matchSize(targetSize, null);
    }

    //TODO: make this depend on interger palette luminance step too
    public void matchSize(int targetSize, @Nullable Float targetLumStep) {
        if (targetLumStep != null && (targetSize - 1) * targetLumStep > 1)
            throw new UnsupportedOperationException("Palette (size-1) * luminance step must be less than 1");
        if (targetLumStep != null && targetLumStep < 0)
            throw new UnsupportedOperationException("Luminance step must be positive");
        if (this.size() == 0 || targetSize <= 0) {
            throw new UnsupportedOperationException("Palette size can't be 0");
        }
        if (targetLumStep != null) targetLumStep = targetLumStep - 0.00001f; //to avoid rounding errors
        if (this.size() == 1) {
            PaletteColor first = this.get(0);
            this.add(first.getDarkened());
            this.add(first.getLightened());
        }
        if (this.size() == 2 && targetLumStep == null) {
            var lightest = this.getLightest();
            var darkest = this.getDarkest();
            Palette other = Palette.fromArc(lightest.hcl(), darkest.hcl(), targetSize);
            this.internal.clear();
            this.internal.addAll(other.getValues());
        }
        while (this.size() > targetSize) {
            if (this.size() > 14) {
                //too many color, we remove the least used
                removeLeastUsed();
            } else {
                //we remove and merge the one close to eachother. we could do some smarter check here...
                reduceAndAverage();
            } //TODO: add this.shouldChangeRange(targetSize, targetLuminanceStep) and decrease outer. maybe not that needed since reduce does merge and remove outer colors too
        }
        boolean down = true;
        boolean canIncreaseDown = true;
        boolean canIncreaseUp = true;
        int currentSize;
        while ((currentSize = this.size()) < targetSize) {
            //safety check if palette is full
            //increase inner if it shouldn't increase outer or if it can't increase outer
            if ((!canIncreaseDown && !canIncreaseUp) ||
                    (!this.shouldExpandRange(targetSize, targetLumStep))) { //&& this.hasLuminanceGap()
                increaseInner();
            } else {
                //increase up and down every cycle
                if (down) increaseDown();
                else increaseUp();
                //if it didn't increase means we are at max luminance, probably white
                if (currentSize == this.size()) {
                    if (down) canIncreaseDown = false;
                    else canIncreaseUp = false;

                    down = !down;
                }
                if (canIncreaseDown && canIncreaseUp)
                    down = !down;
            }

        }
    }

    /**
     * If this should cover more of the luminance spectrum by increasing max or min rather than increasing/decreasing inner
     */
    private boolean shouldExpandRange(int targetSize, @Nullable Float targetStep) {
        if (targetStep == null) return false;
        float targetRange = targetSize * targetStep;
        float currentRange = this.getLuminanceSpan();
        return currentRange < targetRange;
    }

    /**
     * Removes one color, the one that is least used
     *
     * @return removed color
     */
    public PaletteColor removeLeastUsed() {
        //remove the one with the least occurrence
        PaletteColor toRemove = internal.get(0);
        for (var p : internal) {
            if (p.getOccurrence() < toRemove.getOccurrence()) {
                toRemove = p;
            }
        }
        this.remove(toRemove);
        return toRemove;
    }

    /**
     * Removes one color, the one that is closest to other colors
     */
    public PaletteColor reduce() {
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
        return this.remove(index);
    }

    /**
     * Same as before but merges these two colors
     *
     * @return newly added color
     */
    public PaletteColor reduceAndAverage() {
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
        PaletteColor toRemove = this.get(index);
        PaletteColor toRemove2 = this.get(index - 1);
        this.remove(toRemove);
        this.remove(toRemove2);
        var newColor = new PaletteColor(toRemove.lab().mixWith(toRemove2.lab()));
        newColor.setOccurrence(toRemove.getOccurrence() * toRemove2.getOccurrence());
        this.add(newColor);
        return newColor;
    }

    /**
     * Change palette size to match the target luminance span
     * You can think of this as "set contrast"
     * Works by adding or removing colors.
     * Does not change all colors as a whole
     *
     * @param targetLuminanceSpan target luminance span (max luminance - min luminance)
     */
    public void changeSizeMatchingLuminanceSpan(float targetLuminanceSpan) {
        if (targetLuminanceSpan > 1 || targetLuminanceSpan < 0)
            throw new UnsupportedOperationException("Luminance span must be between 0 and 1");
        float currentSpan = this.getLuminanceSpan();
        while (Mth.abs(currentSpan - targetLuminanceSpan) > 0.5 * this.getAverageLuminanceStep()) {
            if (currentSpan < targetLuminanceSpan) {
                if (this.getLightest().luminance() < 1 - this.getDarkest().luminance()) {
                    this.increaseUp();
                } else {
                    this.increaseDown();
                }
            } else if (currentSpan > targetLuminanceSpan) {
                if (this.getLightest().luminance() > 1 - this.getDarkest().luminance()) {
                    this.reduceUp();
                } else {
                    this.reduceDown();
                }
            } else {
                break;
            }
            currentSpan = this.getLuminanceSpan();
        }
    }

    /**
     * Changes the size of a palette to match the luminance range of the target
     * You can think of this as "set contrast" by means of setting the lightest and darkest colors (luminance)
     * Works by adding or removing colors.
     * Does not change all colors as a whole
     *
     * @param minLuminance target min luminance
     * @param maxLuminance target max luminance
     */
    public void expandMatchingLuminanceRange(float minLuminance, float maxLuminance) {
        float currentMin = this.getDarkest().luminance();
        float currentMax = this.getLightest().luminance();
        while (Mth.abs(currentMin - minLuminance) > 0.5 * this.getAverageLuminanceStep()) {
            if (currentMin < minLuminance) {
                this.reduceDown();
            } else {
                this.increaseDown();
            }
            currentMin = this.getDarkest().luminance();
        }

        while (Mth.abs(currentMax - maxLuminance) > 0.5 * this.getAverageLuminanceStep()) {
            if (currentMax > maxLuminance) {
                this.reduceUp();
            } else {
                this.increaseUp();
            }
            currentMax = this.getLightest().luminance();
        }
    }

    /**
     * Alters luminance step by NOT changing its size but instead creating a new palette of sae size as before where each color is a certain luminance step apart from eachother
     * All colors will be evenly spaced by this, centered on the old luminance
     * In other words transforms the old palette such that all its colos are spaced by this luminance step
     *
     * @param newLuminanceStep you can see this as contrast between 2 colors.
     */
    public void matchLuminanceStep(float newLuminanceStep) {
        float centerLuminance = getCenterLuminance();
        int size = this.size();
        float lowerLuminance = centerLuminance - newLuminanceStep * size / 2;
        var copy = this.copy();
        for (int i = 0; i < size; i++) {
            PaletteColor color = copy.get(i);
            float newLum = lowerLuminance + i * newLuminanceStep;
            this.remove(color);
            this.addUnchecked(new PaletteColor(color.hcl().withLuminance(newLum)));
        }
    }

    /**
     * @return true if there is a significant gap between two neighboring colors
     */
    private boolean hasLuminanceGap() {
        return hasLuminanceGap(1.7f);
    }

    private boolean hasLuminanceGap(float cutoff) {
        List<Float> list = getLuminanceSteps();
        float mean = getAverageLuminanceStep();

        for (var s : list) {
            //if it has one step that is greater than 1.5 times the mean
            if (s > cutoff * mean) return true;
        }
        return false;
    }

    /**
     * This is just Normalized Standard Deviation (SD/Mean)
     *
     * @return How much luminance steps differ from eachother
     */
    public float getLuminanceStepVariationCoeff() {
        List<Float> list = getLuminanceSteps();
        float mean = getAverageLuminanceStep();
        float sum = 0;
        for (var s : list) {
            sum += ((s - mean) * (s - mean));
        }
        return Mth.sqrt(sum / (list.size() - 1)) / mean;
    }

    /**
     * @return A list containing the luminance increase between each color
     */
    public List<Float> getLuminanceSteps() {
        List<Float> list = new ArrayList<>();
        float lastLum = this.get(0).luminance();
        for (int i = 1; i < this.size(); i++) {
            float l = this.get(i).luminance();
            list.add(l - lastLum);
            lastLum = l;
        }
        return list;
    }

    /**
     * Calculates the average luminance different between each color. Ideally it should be somewhat constant
     */
    public float getAverageLuminanceStep() {
        return this.getLuminanceSpan() / (this.size() - 1);
    }

    public float getLuminanceSpan() {
        return this.getLightest().luminance() - this.getDarkest().luminance();
    }

    public float getCenterLuminance() {
        return (this.getLightest().luminance() + this.getDarkest().luminance()) / 2;
    }

    /**
     * Removes lightest color
     *
     * @return removed
     */
    public PaletteColor reduceUp() {
        var c = this.getLightest();
        this.remove(c);
        return c;
    }

    /**
     * Removes darkest color
     *
     * @return removed
     */
    public PaletteColor reduceDown() {
        var c = this.getDarkest();
        this.remove(c);
        return c;
    }

    /**
     * Adds a color to the palette by interpolating existing colors
     * Only works if it has at least 2 colors
     */
    public PaletteColor increaseInner() {
        assert (this.size() < 2);
        int index = 1;
        //finds max delta lum and adds a color there
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
        float averageDeltaLum = this.getAverageLuminanceStep();
        HCLColor lightest = this.getLightest().hcl();
        HCLColor secondLightest = this.get(this.size() - 2).hcl();
        var cc = getNextColor(averageDeltaLum, lightest, secondLightest);
        PaletteColor pl = new PaletteColor(cc);
        this.addUnchecked(pl);
        return pl;
    }

    /**
     * Adds an outline color, darker than the darkest color present
     * Only works if it has at least 2 colors
     */
    public PaletteColor increaseDown() {
        assert (this.size() < 2);
        float averageDeltaLum = this.getAverageLuminanceStep();
        HCLColor darkest = this.getDarkest().hcl();
        HCLColor secondDarkest = this.get(1).hcl();
        var cc = getNextColor(-averageDeltaLum, darkest, secondDarkest);
        PaletteColor pl = new PaletteColor(cc);
        this.addUnchecked(pl);
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
     * Combines multiple palettes into one, preserving their occurrence getValues
     */
    public static Palette merge(Palette... palettes) {
        if (palettes.length == 1) return new Palette(palettes[0].getValues());
        Map<Integer, PaletteColor> map = new HashMap<>();
        for (Palette p : palettes) {
            for (PaletteColor c : p.getValues()) {
                int color = c.value();
                if (map.containsKey(color)) {
                    map.get(color).setOccurrence(map.get(color).getOccurrence() + c.getOccurrence());
                } else map.put(color, c);
            }
        }
        if (map.values().size() == 0) return new Palette(new ArrayList<>());
        return new Palette(map.values());
    }

    public static <C extends BaseColor<C>> Palette ofColors(Collection<C> colors) {
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
        if (tolerance != 0) palette.updateTolerance(tolerance);

        if (palette.isEmpty()) {
            throw new RuntimeException("Palette from image " + textureImage + " ended ub being empty");
        }
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
    public static List<Palette> fromAnimatedImage(TextureImage textureImage, @Nullable TextureImage textureMask,
                                                  float tolerance) {
        if (textureMask != null &&
                (textureImage.frameCount() != textureMask.frameCount() ||
                        textureMask.frameWidth() < textureImage.frameWidth() ||
                        textureMask.frameHeight() < textureImage.frameHeight())) {
            throw new UnsupportedOperationException("Palette mask needs to be at least as large as the target image and have the same format");
        }

        List<Palette> palettes = new ArrayList<>();

        NativeImage mask = textureMask == null ? null : textureMask.getImage();
        NativeImage image = textureImage.getImage();

        List<Map<Integer, PaletteColor>> paletteBuilders = new ArrayList<>();

        textureImage.forEachFramePixel((index, x, y) -> {
            //when index changes, we add a completed palette
            if (paletteBuilders.size() <= index) {
                paletteBuilders.add(new HashMap<>());
            }
            var builder = paletteBuilders.get(index);

            if (mask == null || FastColor.ABGR32.alpha(mask.getPixelRGBA(x, y)) == 0) {
                int color = image.getPixelRGBA(x, y);
                if (FastColor.ABGR32.alpha(color) != 0) {
                    var paletteColor = builder.computeIfAbsent(color,
                            p -> new PaletteColor(color));
                    paletteColor.setOccurrence(paletteColor.getOccurrence() + 1);
                }
            }
        });

        for (var p : paletteBuilders) {
            Palette pal;
            if (p.size() == 0) {
                pal = new Palette(new ArrayList<>());
            } else {
                pal = new Palette(p.values(), tolerance);
            }
            palettes.add(pal);
        }

        return palettes;
    }


    //set stuff

    @NotNull
    @Override
    public Iterator<PaletteColor> iterator() {
        return new ItrWrapper();
    }

    private class ItrWrapper implements Iterator<PaletteColor> {

        private final Iterator<PaletteColor> itr;

        private ItrWrapper() {
            this.itr = internal.iterator();
        }

        @Override
        public boolean hasNext() {
            return itr.hasNext();
        }

        @Override
        public PaletteColor next() {
            return itr.next();
        }

        @Override
        public void remove() {
            itr.remove();
            Palette.this.sort();
        }
    }

    @NotNull
    @Override
    public Object[] toArray() {
        return internal.toArray();
    }

    @NotNull
    @Override
    public <T> T[] toArray(@NotNull T[] a) {
        return internal.toArray(a);
    }

    @Override
    public boolean containsAll(@NotNull Collection<?> c) {
        return internal.containsAll(c);
    }

    @Deprecated
    @Override
    public boolean contains(Object o) {
        return this.internal.contains(o);
    }

    @Override
    public boolean retainAll(@NotNull Collection<?> c) {
        if (this.internal.retainAll(c)) {
            this.sort();
            return true;
        }
        return false;
    }

    @Override
    public void clear() {
        this.internal.clear();
    }
}
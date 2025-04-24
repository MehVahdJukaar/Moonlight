package net.mehvahdjukaar.moonlight.api.resources.textures;

import com.mojang.blaze3d.platform.NativeImage;
import net.mehvahdjukaar.moonlight.api.util.math.colors.HSVColor;
import net.mehvahdjukaar.moonlight.api.util.math.colors.RGBColor;
import net.mehvahdjukaar.moonlight.api.util.math.kmeans.DataSet;
import net.mehvahdjukaar.moonlight.api.util.math.kmeans.KMeans;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.IntUnaryOperator;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;

public final class SpriteUtils {

    /**
     * Shorthand method to read a NativeImage
     */
    public static NativeImage readImage(ResourceManager manager, ResourceLocation resourceLocation) throws IOException, NoSuchElementException {
        try (var res = manager.getResource(resourceLocation).get().open()) {
            return NativeImage.read(res);
        } catch (Exception e) {
            throw new IOException(e);
        }
    }

    public static void forEachPixel(NativeImage image, BiConsumer<Integer, Integer> function) {
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                function.accept(x, y);
            }
        }
    }

    public static void grayscaleImage(NativeImage image) {
        forEachPixel(image, (x, y) -> image.setPixelRGBA(x, y,
                new RGBColor(image.getPixelRGBA(x, y)).asHCL().withChroma(0).asRGB().toInt()));
    }

    public static RGBColor averageColor(NativeImage image) {
        //could be faster. whatever
        // read data
        Palette p = Palette.fromImage(TextureImage.of(image), null, 0);

        if (p.size() == 0) return new RGBColor(-1);
        DataSet<DataSet.ColorPoint> data = DataSet.fromPalette(p);

        // cluster
        KMeans.kMeans(data, 1);

        return data.getLastCentroids().get(0).cast().getColor().rgb();
    }

    //TODO: maybe use HCL here

    /**
     * Algorithm that approximates and generates a texture to be used on signs based off its corresponding planks texture.
     * It basically removes last 2 colors and adds another highlight
     * Returns a list of Palettes to work with possible animated (plank) textures
     *
     * @param planksTexture plank texture of the desired wood type
     */
    public static List<Palette> extrapolateSignBlockPalette(TextureImage planksTexture) {
        List<Palette> newPalettes = new ArrayList<>();
        List<Palette> oakPalettes = Palette.fromAnimatedImage(planksTexture, null, 1 / 300f);
        for (Palette palette : oakPalettes) {
            extrapolateSignBlockPalette(palette);
            newPalettes.add(palette);
        }
        return newPalettes;
    }

    public static void extrapolateSignBlockPalette(Palette palette) {
        int size = palette.size();
        if (size == 7) {
            PaletteColor color = palette.get(size - 3);
            HSVColor hsv = color.rgb().asHSV();
            //just saturates last color
            float satIncrease = 1 / 0.94f;
            float brightnessIncrease = 1 / 0.94f;
            HSVColor newCol = new HSVColor(hsv.hue(),
                    Mth.clamp(hsv.saturation() * satIncrease, 0, 1),
                    Mth.clamp(hsv.value() * brightnessIncrease, 0, 1),
                    hsv.alpha());
            PaletteColor newP = new PaletteColor(newCol);
            newP.setOccurrence(color.getOccurrence());
            palette.set(size - 1, newP);
            palette.remove(size - 2);
        }
    }

    //

    /**
     * Algorithm that approximates and generates a texture to be used on wooden item.
     * It basically just darkens the first color
     * Returns just one Palette since items should not have animated textures
     *
     * @param planksTexture plank texture of the desired wood type
     */
    public static Palette extrapolateWoodItemPalette(TextureImage planksTexture) {
        Palette palette = Palette.fromAnimatedImage(planksTexture, null).get(0);
        extrapolateWoodItemPalette(palette);
        return palette;
    }

    public static void extrapolateWoodItemPalette(Palette palette) {
        PaletteColor color = palette.get(0);
        HSVColor hsv = color.rgb().asHSV();
        //just saturates last color
        float satMult = 1.11f;
        float brightnessMult = 0.94f;
        HSVColor newCol = new HSVColor(hsv.hue(),
                Mth.clamp(hsv.saturation() * satMult, 0, 1),
                Mth.clamp(hsv.value() * brightnessMult, 0, 1),
                hsv.alpha());
        PaletteColor newP = new PaletteColor(newCol);
        newP.setOccurrence(color.getOccurrence());
        palette.set(0, newP);
    }


    //Better use LAB color
    @Deprecated
    public static float getLuminance(int r, int g, int b) {
        return (0.299f * r + 0.587f * g + 0.114f * b);
    }


    /**
     * Given an image, reduce its color palette using k-means algorithm
     * Note that this also accounts for color occurrence
     *
     * @param image  original image
     * @param sizeFn target size function. Goes from original size to target size
     */
    public static void reduceColors(NativeImage image, IntUnaryOperator sizeFn) {

        // read data
        Palette p = Palette.fromImage(TextureImage.of(image), null, 0);

        if (p.size() == 0) return;
        DataSet<DataSet.ColorPoint> data = DataSet.fromPalette(p);

        int size = sizeFn.applyAsInt(p.size());

        if (size >= p.size()) return;

        // cluster
        KMeans.kMeans(data, size);

        Map<Integer, Integer> colorToColorMap = new HashMap<>();

        for (var c : data.getColorPoints()) {
            var centroid = data.getLastCentroids().get(c.getClusterNo());
            colorToColorMap.put(c.cast().getColor().value(), centroid.cast().getColor().value());
        }

        SpriteUtils.forEachPixel(image, (x, y) -> {
            int i = image.getPixelRGBA(x, y);
            if (colorToColorMap.containsKey(i)) {
                image.setPixelRGBA(x, y, colorToColorMap.get(i));
            }
        });

    }


    /**
     * Similar to reduceColors, this takes an image and tries to reduce its colors by grouping together similar ones
     * In other words gets rid of colors very close to each other. Useful to clean up textures before recoloring as having many similar colors could skew the retexturing process
     *
     * @param image     original image
     * @param tolerance tolerance for two colors to be merged
     */
    public static void mergeSimilarColors(NativeImage image, float tolerance) {
        TextureImage texture = TextureImage.of(image);
        Palette originalPalette = Palette.fromImage(texture, null, 0);
        Palette targetPalette = originalPalette.copy();
        targetPalette.updateTolerance(tolerance);
        //gets removed colors
        originalPalette.removeAll(targetPalette);

        //colors to replace
        Map<Integer, Integer> removedColors = new HashMap<>();

        for (var i : originalPalette) {
            var replacement = targetPalette.getColorClosestTo(i);
            removedColors.put(i.value(), replacement.value());
        }

        SpriteUtils.forEachPixel(image, (x, y) -> {
            int i = image.getPixelRGBA(x, y);
            Integer replacement = removedColors.get(i);
            if (replacement != null)
                image.setPixelRGBA(x, y, replacement);
        });
    }

    /**
     * @param manager         resource manager
     * @param fullTexturePath texture location
     * @param expectColors    expected amount of colors. Will stop reading once the amount is reached
     * @return an ordered color list obtained by reading the provided image pixels one by one from left to right then up to bottom (like a book)
     */
    public static List<Integer> parsePaletteStrip(ResourceManager manager, ResourceLocation fullTexturePath, int expectColors) {
        try (NativeImage image = readImage(manager, fullTexturePath)) {
            List<Integer> list = new ArrayList<>();
            forEachPixel(image, (x, y) -> {
                int i = image.getPixelRGBA(x, y);
                if (i == 0 || list.size() >= expectColors) return;
                list.add(i);
            });
            if (list.size() < expectColors) {
                throw new RuntimeException("Image at " + fullTexturePath + " has too few colors! Expected at least " + expectColors + " and got " + list.size());
            }
            return list;
        } catch (IOException | NoSuchElementException e) {
            throw new RuntimeException("Failed to find image at location " + fullTexturePath, e);
        }
    }


    public static TextureImage savePaletteStrip(ResourceManager manager, List<Integer> colors) {

        try (var image = TextureImage.createNew(16, 16)) {
            var it = colors.iterator();
            image.forEachFramePixel((x, y, f) -> {
                if (it.hasNext()) {
                    image.getImage().setPixelRGBA(x, y, it.next());
                }
            });
            return image;
        } catch (Exception e) {
            throw new RuntimeException("Failed to create palette strip");
        }
    }

}

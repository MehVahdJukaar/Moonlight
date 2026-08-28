package net.mehvahdjukaar.moonlight.api.resources.textures;

import com.mojang.blaze3d.platform.NativeImage;
import net.mehvahdjukaar.moonlight.api.util.math.colors.HSVColor;
import net.mehvahdjukaar.moonlight.api.util.math.colors.RGBColor;
import net.mehvahdjukaar.moonlight.api.util.math.kmeans.DataSet;
import net.mehvahdjukaar.moonlight.api.util.math.kmeans.KMeans;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.Mth;
import org.lwjgl.stb.STBImage;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.IntBuffer;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.IntUnaryOperator;

public final class SpriteUtils {

    private static final byte[] PNG_SIGNATURE = {(byte) 0x89, 'P', 'N', 'G', '\r', '\n', 0x1A, '\n'};

    public static byte[] toPngBytes(NativeImage image) throws IOException {
        var out = new ByteArrayOutputStream();
        try (var channel = Channels.newChannel(out)) {
            if (!image.writeToChannel(channel)) {
                throw new IOException("Failed to encode image as png");
            }
        }
        return out.toByteArray();
    }

    /**
     * Shorthand method to read a NativeImage
     */
    public static NativeImage readImage(ResourceManager manager, Identifier resourceLocation) throws IOException, NoSuchElementException {
        try (var res = manager.getResource(resourceLocation).get().open()) {
            return NativeImage.read(res);
        } catch (Exception e) {
            throw new IOException(e);
        }
    }

    /**
     * Decodes raw image bytes. Unlike NativeImage.read, which rejects anything that isn't a PNG, this accepts
     * every format stb can read (gif, jpeg, bmp, tga, ...). Animated gifs decode to their first frame.
     */
    public static NativeImage readImage(byte[] imageBytes) throws IOException {
        if (isPng(imageBytes)) {
            try (InputStream in = new ByteArrayInputStream(imageBytes)) {
                return NativeImage.read(in);
            }
        }
        return readImageWithStb(imageBytes);
    }

    private static boolean isPng(byte[] bytes) {
        if (bytes.length < PNG_SIGNATURE.length) return false;
        for (int i = 0; i < PNG_SIGNATURE.length; i++) {
            if (bytes[i] != PNG_SIGNATURE[i]) return false;
        }
        return true;
    }

    // stb hands us its own rgba buffer, so we copy it into a NativeImage-owned one and free it right away
    private static NativeImage readImageWithStb(byte[] imageBytes) throws IOException {
        ByteBuffer encoded = MemoryUtil.memAlloc(imageBytes.length);
        try (MemoryStack stack = MemoryStack.stackPush()) {
            encoded.put(imageBytes).flip();
            IntBuffer width = stack.mallocInt(1);
            IntBuffer height = stack.mallocInt(1);
            IntBuffer channels = stack.mallocInt(1);
            ByteBuffer pixels = STBImage.stbi_load_from_memory(encoded, width, height, channels, 4);
            if (pixels == null) {
                throw new IOException("Could not load image: " + STBImage.stbi_failure_reason());
            }
            try {
                NativeImage image = new NativeImage(NativeImage.Format.RGBA, width.get(0), height.get(0), false);
                MemoryUtil.memCopy(MemoryUtil.memAddress(pixels), image.pixels,
                        (long) width.get(0) * height.get(0) * 4);
                return image;
            } finally {
                STBImage.stbi_image_free(pixels);
            }
        } finally {
            MemoryUtil.memFree(encoded);
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
        forEachPixel(image, (x, y) -> image.setPixelABGR(x, y,
                new RGBColor(image.getPixelABGR(x, y)).asHCL().withChroma(0).asRGB().toInt()));
    }

    public static RGBColor averageColor(NativeImage image) {
        //could be faster. whatever
        // read data
        Palette p = Palette.fromImage(TextureImage.of(image), null, 0);

        if (p.isEmpty()) return new RGBColor(-1);
        DataSet<DataSet.ColorPoint> data = DataSet.fromPalette(p);

        // cluster
        KMeans.kMeans(data, 1);

        return data.getLastCentroids().getFirst().cast().getColor().rgb();
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
        Palette palette = Palette.fromAnimatedImage(planksTexture, null).getFirst();
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

        if (p.isEmpty()) return;
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
            int i = image.getPixelABGR(x, y);
            if (colorToColorMap.containsKey(i)) {
                image.setPixelABGR(x, y, colorToColorMap.get(i));
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
            int i = image.getPixelABGR(x, y);
            Integer replacement = removedColors.get(i);
            if (replacement != null)
                image.setPixelABGR(x, y, replacement);
        });
    }

    /**
     * @param manager         resource manager
     * @param fullTexturePath texture location
     * @param expectColors    expected amount of colors. Will stop reading once the amount is reached
     * @return an ordered color list obtained by reading the provided image pixels one by one from left to right then up to bottom (like a book)
     */
    public static List<Integer> parsePaletteStrip(ResourceManager manager, Identifier fullTexturePath, int expectColors) {
        try (NativeImage image = readImage(manager, fullTexturePath)) {
            List<Integer> list = new ArrayList<>();
            forEachPixel(image, (x, y) -> {
                int i = image.getPixelABGR(x, y);
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

        try (TextureImage image = TextureImage.createNew(16, 16)) {
            var it = colors.iterator();
            image.forEachPixel(pixel -> {
                if (it.hasNext()) {
                    pixel.setValue(it.next());
                }
            });
            return image;
        } catch (Exception e) {
            throw new RuntimeException("Failed to create palette strip");
        }
    }

}

package net.mehvahdjukaar.selene.textures;

import com.mojang.blaze3d.platform.NativeImage;
import net.mehvahdjukaar.selene.Selene;
import net.minecraft.util.Mth;

import java.awt.*;
import java.util.Arrays;

public class SpriteUtils {

    /**
     * Creates an image by combining two others taking alpha into consideration. Overlays are applied first in first out
     * The base image passed is modified
     */
    public static void mergeImages(NativeImage baseImage, NativeImage... overlays) throws IllegalStateException{
        int width = baseImage.getWidth();
        int height = baseImage.getHeight();
        if (Arrays.stream(overlays).anyMatch(n -> n.getHeight() != height || n.getWidth() != width)) {
            throw new IllegalStateException("Could not create images because they had different dimensions");
        }
        
        for (var o : overlays) {
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    baseImage.blendPixel(x, y, o.getPixelRGBA(x, y));
                }
            }
        }
    }

    //removes last 2 colors and adds another hilight
    public static Palette extrapolateSignBlockPalette(NativeImage planksTexture) {
        Palette palette = Palette.fromImage(planksTexture, null);
        int size = palette.size();
        if(size == 7) {
            PaletteColor color = palette.get(size - 3);
            float[] hsv = RGBtoHSV(color.color);
            //just saturates last color
            float satIncrease = 1/0.94f;
            float brightnessIncrease = 1/0.94f;
            int newCol = HSVtoRGB(hsv[0], hsv[1] * satIncrease, hsv[2] * brightnessIncrease);
            PaletteColor newP = new PaletteColor(color.x, color.y, newCol);
            newP.occurrence = color.occurrence;
            palette.set(size - 1, newP);
            palette.remove(size - 2);
        }
        return palette;
    }

    //darkens first color
    public static Palette extrapolateWoodItemPalette(NativeImage planksTexture) {
        Palette palette = Palette.fromImage(planksTexture, null);
        PaletteColor color = palette.get(0);
        float[] hsv = RGBtoHSV(color.color);
        //just saturates last color
        float satIncrease = 1.11f;
        float brightnessIncrease = 0.945f;
        int newCol = HSVtoRGB(hsv[0], hsv[1] * satIncrease, hsv[2] * brightnessIncrease);
        PaletteColor newP = new PaletteColor(color.x, color.y, newCol);
        newP.occurrence = color.occurrence;
        palette.set(0, newP);
        return palette;
    }

    public static float getLuminance(int r, int g, int b) {
        return (0.299f * r + 0.587f * g + 0.114f * b);
    }

    public static int HSVtoRGB(float hue, float saturation, float brightness) {
        hue = Mth.clamp(hue, 0,1);
        saturation = Mth.clamp(saturation, 0,1);
        brightness = Mth.clamp(brightness, 0,1);
        int r = 0, g = 0, b = 0;
        if (saturation == 0) {
            r = g = b = (int) (brightness * 255.0f + 0.5f);
        } else {
            float h = (hue - (float)Math.floor(hue)) * 6.0f;
            float f = h - (float)java.lang.Math.floor(h);
            float p = brightness * (1.0f - saturation);
            float q = brightness * (1.0f - saturation * f);
            float t = brightness * (1.0f - (saturation * (1.0f - f)));
            switch ((int) h) {
                case 0 -> {
                    r = (int) (brightness * 255.0f + 0.5f);
                    g = (int) (t * 255.0f + 0.5f);
                    b = (int) (p * 255.0f + 0.5f);
                }
                case 1 -> {
                    r = (int) (q * 255.0f + 0.5f);
                    g = (int) (brightness * 255.0f + 0.5f);
                    b = (int) (p * 255.0f + 0.5f);
                }
                case 2 -> {
                    r = (int) (p * 255.0f + 0.5f);
                    g = (int) (brightness * 255.0f + 0.5f);
                    b = (int) (t * 255.0f + 0.5f);
                }
                case 3 -> {
                    r = (int) (p * 255.0f + 0.5f);
                    g = (int) (q * 255.0f + 0.5f);
                    b = (int) (brightness * 255.0f + 0.5f);
                }
                case 4 -> {
                    r = (int) (t * 255.0f + 0.5f);
                    g = (int) (p * 255.0f + 0.5f);
                    b = (int) (brightness * 255.0f + 0.5f);
                }
                case 5 -> {
                    r = (int) (brightness * 255.0f + 0.5f);
                    g = (int) (p * 255.0f + 0.5f);
                    b = (int) (q * 255.0f + 0.5f);
                }
            }
        }
        r = Mth.clamp(r, 0,255);
        g = Mth.clamp(g, 0,255);
        b = Mth.clamp(b, 0,255);
        return NativeImage.combine(255, b, g, r);
    }

    public static float[] RGBtoHSV(int rgb) {
        int r = NativeImage.getR(rgb);
        int g = NativeImage.getG(rgb);
        int b = NativeImage.getB(rgb);

        float hue, saturation, brightness;

        float[] hsbvals = new float[3];

        int cmax = Math.max(r, g);
        if (b > cmax) cmax = b;
        int cmin = Math.min(r, g);
        if (b < cmin) cmin = b;

        brightness = ((float) cmax) / 255.0f;
        if (cmax != 0)
            saturation = ((float) (cmax - cmin)) / ((float) cmax);
        else
            saturation = 0;
        if (saturation == 0)
            hue = 0;
        else {
            float redc = ((float) (cmax - r)) / ((float) (cmax - cmin));
            float greenc = ((float) (cmax - g)) / ((float) (cmax - cmin));
            float bluec = ((float) (cmax - b)) / ((float) (cmax - cmin));
            if (r == cmax)
                hue = bluec - greenc;
            else if (g == cmax)
                hue = 2.0f + redc - bluec;
            else
                hue = 4.0f + greenc - redc;
            hue = hue / 6.0f;
            if (hue < 0)
                hue = hue + 1.0f;
        }
        hsbvals[0] = hue;
        hsbvals[1] = saturation;
        hsbvals[2] = brightness;
        return hsbvals;
    }

        /*
    public List<Pair<ResourceLocation, byte[]>> generateImage(
           PaletteSwapper paletteSwapper

            ResourceLocation originalTexture, ResourceLocation targetPath, ResourceManager manager) {
        List<Pair<ResourceLocation, byte[]>> list = new ArrayList<>();
        try {

            NativeImage transformedImage = swapper.recolorImage(oak);

            list.add(Pair.of(targetPath, transformedImage.asByteArray()));

            //try getting metadata for animated textures
            ResourceLocation metadataLocation = RPUtils.resPath(originalTexture, ResType.BLOCK_MCMETA);

            if (manager.hasResource(metadataLocation)) {
                BufferedReader bufferedReader = null;

                try (InputStream metadataStream = manager.getResource(metadataLocation).getInputStream()) {
                    bufferedReader = new BufferedReader(new InputStreamReader(metadataStream, StandardCharsets.UTF_8));
                    JsonObject metadataJson = GsonHelper.parse(bufferedReader);

                    list.add(Pair.of(RPUtils.resPath(newTexture, ResType.BLOCK_MCMETA), metadataJson.toString().getBytes()));
                } finally {
                    IOUtils.closeQuietly(bufferedReader);
                }
            }
        }catch (Exception exception){

        }

        return list;
    }*/
}

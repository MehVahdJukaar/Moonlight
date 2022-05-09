package net.mehvahdjukaar.selene.math.colors;

import javax.annotation.concurrent.Immutable;
import java.util.ArrayList;
import java.util.Arrays;

//Polar LUV
@Immutable
public class HCLVColor extends BaseColor<HCLVColor> {

    public HCLVColor(float h, float c, float l, float a) {
        super(h, c, l, a);
    }

    @Override
    public String toString() {
        return String.format("H: %s, C: %s, L %s", (int) (255 * hue()), (int) (255 * chroma()), (int) (255 * luminance()));
    }

    public float hue() {
        return v0;
    }

    public float chroma() {
        return v1;
    }

    public float luminance() {
        return v2;
    }

    public float alpha() {
        return v3;
    }

    public HCLVColor withHue(float hue) {
        return new HCLVColor(hue, chroma(), luminance(), alpha());
    }

    public HCLVColor withChroma(float chroma) {
        return new HCLVColor(hue(), chroma, luminance(), alpha());
    }

    public HCLVColor withLuminance(float luminance) {
        return new HCLVColor(hue(), chroma(), luminance, alpha());
    }

    public HCLVColor withAlpha(float alpha) {
        return new HCLVColor(hue(), chroma(), luminance(), alpha);
    }

    @Override
    public RGBColor asRGB() {
        return ColorSpaces.HCLVtoLUV(this).asRGB();
    }

    @Override
    public HCLVColor asHCLV() {
        return this;
    }

    public static HCLVColor averageColors(HCLVColor... colors) {
        float size = colors.length;
        var list = Arrays.stream(colors).map(HCLVColor::hue);
        Float[] hues = list.toArray(Float[]::new);
        float s = 0, v = 0, a = 0;
        for (HCLVColor c : colors) {
            s += c.chroma();
            v += c.luminance();
            a += c.alpha();
        }
        return new HCLVColor(averageAngles(hues), s / size, v / size, a / size);
    }

    @Override
    public HCLVColor mixWith(HCLVColor color, float bias) {
        float i = 1 - bias;
        float h = weightedAverageAngles(this.hue(), color.hue(),  bias);
        while (h < 0) ++h;
        float c = this.chroma() * i + color.chroma() * bias;
        float b = this.luminance() * i + color.luminance() * bias;
        float a = this.alpha() * i + color.alpha() * bias;

        return new HCLVColor(h, c, b, a);
    }

    @Override
    public HCLVColor fromRGB(RGBColor rgb) {
        return rgb.asHCLV();
    }

    @Override
    public float distTo(HCLVColor other) {
        float h = this.hue();
        float h2 = other.hue();
        float c = this.chroma();
        float c2 = other.chroma();
        double x = c * Math.cos(h * Math.PI * 2) - c2 * Math.cos(h2 * Math.PI * 2);
        double y = c * Math.sin(h * Math.PI * 2) - c2 * Math.sin(h2 * Math.PI * 2);

        return (float) Math.sqrt(x * x + y * y +
                (this.luminance() - other.luminance()) * (this.luminance() - other.luminance()));
    }
}

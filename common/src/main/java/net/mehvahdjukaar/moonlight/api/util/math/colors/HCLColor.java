package net.mehvahdjukaar.moonlight.api.util.math.colors;

import net.mehvahdjukaar.moonlight.api.set.BlockTypeRegistry;
import net.mehvahdjukaar.moonlight.core.set.CompatTypes;
import net.minecraft.util.Mth;

import javax.annotation.concurrent.Immutable;
import java.util.Arrays;

//Polar LAB
@Immutable
public class HCLColor extends BaseColor<HCLColor> {

    public HCLColor(float h, float c, float l, float a) {
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

    public HCLColor withHue(float hue) {
        return new HCLColor(hue, chroma(), luminance(), alpha());
    }

    public HCLColor withChroma(float chroma) {
        return new HCLColor(hue(), chroma, luminance(), alpha());
    }

    public HCLColor withLuminance(float luminance) {
        return new HCLColor(hue(), chroma(), luminance, alpha());
    }

    public HCLColor withAlpha(float alpha) {
        return new HCLColor(hue(), chroma(), luminance(), alpha);
    }

    @Override
    public RGBColor asRGB() {
        return ColorSpaces.HCLtoLAB(this).asRGB();
    }

    @Override
    public HCLColor asHCL() {
        return this;
    }

    public static HCLColor averageColors(HCLColor... colors) {
        float size = colors.length;
        var list = Arrays.stream(colors).map(HCLColor::hue);
        Float[] hues = list.toArray(Float[]::new);
        float cr = 0, l = 0, a = 0;
        for (HCLColor c : colors) {
            cr += c.chroma();
            l += c.luminance();
            a += c.alpha();
        }
        return new HCLColor(averageAngles(hues), cr / size, l / size, a / size);
    }


    @Override
    public HCLColor mixWith(HCLColor color, float bias) {
        float i = 1 - bias;
        float h = weightedAverageAngles(this.hue(), color.hue(),  bias);
        while (h < 0) ++h;
        float c = this.chroma() * i + color.chroma() * bias;
        float b = this.luminance() * i + color.luminance() * bias;
        float a = this.alpha() * i + color.alpha() * bias;

        return new HCLColor(h, c, b, a);
    }

    @Override
    public HCLColor multiply(HCLColor color, float hue, float chroma, float luminance, float alpha) {
        return new HCLColor(Mth.clamp(hue*this.hue(), 0,1),
                Mth.clamp(chroma*this.chroma(), 0,1),
                Mth.clamp(luminance*this.luminance(), 0,1),
                Mth.clamp(alpha*this.alpha(), 0,1));
    }

    @Override
    public HCLColor fromRGB(RGBColor rgb) {
        return rgb.asHCL();
    }

    @Override
    public float distTo(HCLColor other) {
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

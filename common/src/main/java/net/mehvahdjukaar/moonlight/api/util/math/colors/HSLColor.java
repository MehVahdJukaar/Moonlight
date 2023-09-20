package net.mehvahdjukaar.moonlight.api.util.math.colors;

import net.minecraft.util.Mth;
import oshi.annotation.concurrent.Immutable;

import java.util.Arrays;


@Immutable
public class HSLColor extends BaseColor<HSLColor> {

    public HSLColor(float h, float s, float l, float a) {
        super(h, s, l, a);
    }

    @Override
    public String toString() {
        return String.format("H: %s, S: %s, L %s", (int)(255*hue()), (int)(255*saturation()), (int)(255*lightness()));
    }

    public float hue() {
        return v0;
    }

    public float saturation() {
        return v1;
    }

    public float lightness() {
        return v2;
    }

    public float alpha() {
        return v3;
    }

    public HSLColor withHue(float hue) {
        return new HSLColor(hue, saturation(), lightness(), alpha());
    }

    public HSLColor withSaturation(float saturation) {
        return new HSLColor(hue(), saturation, lightness(), alpha());
    }

    public HSLColor withLightness(float lightness) {
        return new HSLColor(hue(), saturation(), lightness, alpha());
    }

    public HSLColor withAlpha(float alpha) {
        return new HSLColor(hue(), saturation(), lightness(), alpha);
    }


    @Override
    public RGBColor asRGB() {
        return ColorSpaces.HSLtoRGB(this);
    }

    @Override
    public HSLColor asHSL() {
        return this;
    }

    public static HSLColor averageColors(HSLColor... colors) {
        float size = colors.length;
        var list = Arrays.stream(colors).map(HSLColor::hue);
        Float[] hues = list.toArray(Float[]::new);
        float s = 0, l = 0, a = 0;
        for (HSLColor c : colors) {
            s += c.saturation();
            l += c.lightness();
            a += c.alpha();
        }
        return new HSLColor(averageAngles(hues), s / size, l / size, a / size);
    }
    @Override
    public HSLColor multiply(float hue, float saturation, float lightness, float alpha) {
        return new HSLColor(Mth.clamp(hue*this.hue(), 0,1),
                Mth.clamp(saturation*this.saturation(), 0,1),
                Mth.clamp(lightness*this.lightness(), 0,1),
                Mth.clamp(alpha*this.alpha(), 0,1));
    }

    @Deprecated(forRemoval = true)
    public HSLColor multiply(HSLColor color, float hue, float chroma, float luminance, float alpha) {
        return multiply(hue, chroma, luminance, alpha);
    }
    @Override
    public HSLColor mixWith(HSLColor color, float bias) {
        float i = 1 - bias;
        if(!(bias>=0 && bias<=1)){
            throw new IllegalArgumentException("bias must be between 0 and one");
        }
        float h = weightedAverageAngles(this.hue(), color.hue(), bias);
        while(h<0)++h;
        float s = this.saturation() * i + color.saturation() * bias;
        float l = this.lightness() * i + color.lightness() * bias;
        float a = this.alpha() * i + color.alpha() * bias;

        return new HSLColor(h, s, l, a);
    }

    @Override
    public HSLColor fromRGB(RGBColor rgb) {
        return rgb.asHSL();
    }

    @Override
    public float distTo(HSLColor other) {
        float h = this.hue();
        float h2 = other.hue();
        float c = this.saturation();
        float c2 = other.saturation();
        double x = c * Math.cos(h * Math.PI * 2) - c2 * Math.cos(h2 * Math.PI * 2);
        double y = c * Math.sin(h * Math.PI * 2) - c2 * Math.sin(h2 * Math.PI * 2);

        return (float) Math.sqrt(x * x + y * y +
                (this.lightness() - other.lightness()) * (this.lightness() - other.lightness()));
    }
}

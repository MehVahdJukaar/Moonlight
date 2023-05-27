package net.mehvahdjukaar.moonlight.api.util.math.colors;

import net.minecraft.util.Mth;

import java.util.Arrays;

public class HSVColor extends BaseColor<HSVColor> {

    public HSVColor(float h, float s, float b, float a) {
        super(h, s, b, a);
    }

    @Override
    public String toString() {
        return String.format("H: %s, S: %s, V %s", (int) (255 * hue()), (int) (255 * saturation()), (int) (255 * value()));
    }

    //color
    public float hue() {
        return v0;
    }

    public float saturation() {
        return v1;
    }

    //how intense this color is
    public float value() {
        return v2;
    }

    public float alpha() {
        return v3;
    }

    public HSVColor withHue(float hue) {
        return new HSVColor(hue, saturation(), value(), alpha());
    }

    public HSVColor withSaturation(float saturation) {
        return new HSVColor(hue(), saturation, value(), alpha());
    }

    public HSVColor withValue(float value) {
        return new HSVColor(hue(), saturation(), value, alpha());
    }

    public HSVColor withAlpha(float alpha) {
        return new HSVColor(hue(), saturation(), value(), alpha);
    }

    @Override
    public HSVColor asHSV() {
        return this;
    }

    @Override
    public RGBColor asRGB() {
        return ColorSpaces.HSVtoRGB(this);
    }

    public static HSVColor averageColors(HSVColor... colors) {
        float size = colors.length;
        var list = Arrays.stream(colors).map(HSVColor::hue);
        Float[] hues = list.toArray(Float[]::new);
        float s = 0, v = 0, a = 0;
        for (HSVColor c : colors) {
            s += c.saturation();
            v += c.value();
            a += c.alpha();
        }
        return new HSVColor(averageAngles(hues), s / size, v / size, a / size);
    }

    @Override
    public HSVColor multiply(HSVColor color, float hue, float saturation, float value, float alpha) {
        return new HSVColor(Mth.clamp(hue * this.hue(), 0, 1),
                Mth.clamp(saturation * this.saturation(), 0, 1),
                Mth.clamp(value * this.value(), 0, 1),
                Mth.clamp(alpha * this.alpha(), 0, 1));
    }

    @Override
    public HSVColor mixWith(HSVColor color, float bias) {
        float i = 1 - bias;
        float h = weightedAverageAngles(this.hue(), color.hue(), bias);
        while (h < 0) ++h;
        float s = this.saturation() * i + color.saturation() * bias;
        float v = this.value() * i + color.value() * bias;
        float a = this.alpha() * i + color.alpha() * bias;

        return new HSVColor(h, s, v, a);
    }

    @Override
    public HSVColor fromRGB(RGBColor rgb) {
        return rgb.asHSV();
    }

    @Override
    public float distTo(HSVColor other) {
        float h = this.hue();
        float h2 = other.hue();
        float c = this.saturation();
        float c2 = other.saturation();
        double x = c * Math.cos(h * Math.PI * 2) - c2 * Math.cos(h2 * Math.PI * 2);
        double y = c * Math.sin(h * Math.PI * 2) - c2 * Math.sin(h2 * Math.PI * 2);

        return (float) Math.sqrt(x * x + y * y +
                (this.value() - other.value()) * (this.value() - other.value()));
    }
}

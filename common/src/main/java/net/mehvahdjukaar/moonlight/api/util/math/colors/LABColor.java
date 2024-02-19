package net.mehvahdjukaar.moonlight.api.util.math.colors;

import net.minecraft.util.Mth;

public class LABColor extends BaseColor<LABColor> {

    public LABColor(float l, float a, float b, float alpha) {
        super(l, a, b, alpha);
        //LAB can have a,b negative getValues
    }

    @Override
    public String toString() {
        return String.format("L: %s, A: %s, B %s", (int) (255 * luminance()), (int) (255 * a()), (int) (255 * b()));
    }

    //same as HCL chroma
    public float luminance() {
        return v0;
    }

    public float a() {
        return v1;
    }

    public float b() {
        return v2;
    }

    public float alpha() {
        return v3;
    }

    public LABColor withLuminance(float luminance) {
        return new LABColor(luminance, a(), b(), alpha());
    }

    public LABColor withA(float a) {
        return new LABColor(luminance(), a, b(), alpha());
    }

    public LABColor withB(float b) {
        return new LABColor(luminance(), a(), b, alpha());
    }

    public LABColor withAlpha(float alpha) {
        return new LABColor(luminance(), a(), b(), alpha);
    }

    public static LABColor averageColors(LABColor... colors) {
        float size = colors.length;
        float r = 0, g = 0, b = 0, a = 0;
        for (LABColor c : colors) {
            r += c.luminance();
            g += c.a();
            b += c.b();
            a += c.alpha();
        }
        return new LABColor(r / size, g / size, b / size, a / size);
    }

    @Override
    public LABColor asLAB() {
        return this;
    }

    @Override
    public RGBColor asRGB() {
        return ColorSpaces.XYZtoRGB(ColorSpaces.LABtoXYZ(this));
    }

    @Override
    public LABColor multiply( float luminance, float a, float b, float alpha) {
        return new LABColor(
                luminance * this.luminance(),
                a * this.a(),
                b * this.b(),
                alpha * this.alpha());
    }
    @Deprecated(forRemoval = true)
    public LABColor multiply(LABColor color, float hue, float chroma, float luminance, float alpha) {
        return multiply(hue, chroma, luminance, alpha);
    }

    @Override
    public LABColor mixWith(LABColor color, float bias) {
        float i = 1 - bias;
        float r = this.luminance() * i + color.luminance() * bias;
        float g = this.a() * i + color.a() * bias;
        float b = this.b() * i + color.b() * bias;
        float a = this.alpha() * i + color.alpha() * bias;

        return new LABColor(r, g, b, a);
    }

    @Override
    public LABColor fromRGB(RGBColor rgb) {
        return rgb.asLAB();
    }
}

package net.mehvahdjukaar.selene.math.colors;

import javax.annotation.concurrent.Immutable;

@Immutable
public class LUVColor extends BaseColor<LUVColor> {

    public LUVColor(float l, float u, float v, float alpha) {
        super(l, u, v, alpha);
        //LAB can have a,b negative values
    }

    @Override
    public String toString() {
        return String.format("L: %s, U: %s, V %s", (int) (255 * luminance()), (int) (255 * u()), (int) (255 * v()));
    }

    //same as HCL chroma
    public float luminance() {
        return v0;
    }

    public float u() {
        return v1;
    }

    public float v() {
        return v2;
    }

    public float alpha() {
        return v3;
    }

    public LUVColor withLuminance(float luminance) {
        return new LUVColor(luminance, u(), v(), alpha());
    }

    public LUVColor withU(float u) {
        return new LUVColor(luminance(), u, v(), alpha());
    }

    public LUVColor withV(float v) {
        return new LUVColor(luminance(), u(), v, alpha());
    }

    public LUVColor withAlpha(float alpha) {
        return new LUVColor(luminance(), u(), v(), alpha);
    }

    public static LUVColor averageColors(LUVColor... colors) {
        float size = colors.length;
        float r = 0, g = 0, b = 0, a = 0;
        for (LUVColor c : colors) {
            r += c.luminance();
            g += c.u();
            b += c.v();
            a += c.alpha();
        }
        return new LUVColor(r / size, g / size, b / size, a / size);
    }

    @Override
    public LUVColor asLUV() {
        return this;
    }

    @Override
    public RGBColor asRGB() {
        return ColorSpaces.XYZtoRGB(ColorSpaces.LUVtoXYZ(this));
    }


    @Override
    public LUVColor mixWith(LUVColor color, float bias) {
        float i = 1 - bias;
        float r = this.luminance() * i + color.luminance() * bias;
        float g = this.u() * i + color.u() * bias;
        float b = this.v() * i + color.v() * bias;
        float a = this.alpha() * i + color.alpha() * bias;

        return new LUVColor(r, g, b, a);
    }

    @Override
    public LUVColor fromRGB(RGBColor rgb) {
        return rgb.asLUV();
    }
}

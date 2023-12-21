package net.mehvahdjukaar.moonlight.api.util.math.colors;

import net.minecraft.util.Mth;

public class RGBColor extends BaseColor<RGBColor> {

    //color from packed int
    public RGBColor(int value) {
        this(getR(value) / 255f, getG(value) / 255f,
                getB(value) / 255f, getA(value) / 255f);
    }

    public RGBColor(float r, float g, float b, float a) {
        super(Mth.clamp(r, 0, 1), Mth.clamp(g, 0, 1),
                Mth.clamp(b, 0, 1), Mth.clamp(a, 0, 1));
    }

    public static int getA(int abgr) {
        return abgr >> 24 & 0xFF;
    }

    public static int getR(int abgr) {
        return abgr & 0xFF;
    }

    public static int getG(int agbgr) {
        return agbgr >> 8 & 0xFF;
    }

    public static int getB(int agbgr) {
        return agbgr >> 16 & 0xFF;
    }

    public static int combine(int alpha, int blue, int green, int red) {
        return (alpha & 255) << 24 | (blue & 255) << 16 | (green & 255) << 8 | (red & 255);
    }

    @Override
    public String toString() {
        return String.format("R: %s, G: %s, B %s", (int) (255 * red()), (int) (255 * green()), (int) (255 * blue()));
    }

    public float red() {
        return v0;
    }

    public float green() {
        return v1;
    }

    public float blue() {
        return v2;
    }

    public float alpha() {
        return v3;
    }

    public RGBColor withRed(float red) {
        return new RGBColor(red, green(), blue(), alpha());
    }

    public RGBColor withGreen(float green) {
        return new RGBColor(red(), green, blue(), alpha());
    }

    public RGBColor withBlue(float blue) {
        return new RGBColor(red(), green(), blue, alpha());
    }

    public RGBColor withAlpha(float alpha) {
        return new RGBColor(red(), green(), blue(), alpha);
    }


    @Override
    public RGBColor asRGB() {
        return this;
    }

    public static RGBColor averageColors(RGBColor... colors) {
        float size = colors.length;
        float r = 0, g = 0, b = 0, a = 0;
        for (RGBColor c : colors) {
            r += c.red();
            g += c.green();
            b += c.blue();
            a += c.alpha();
        }
        return new RGBColor(r / size, g / size, b / size, a / size);
    }

    @Override
    public RGBColor multiply( float red, float green, float blue, float alpha) {
        return new RGBColor(Mth.clamp(red * this.red(), 0, 1),
                Mth.clamp(green * this.green(), 0, 1),
                Mth.clamp(blue * this.blue(), 0, 1),
                Mth.clamp(alpha * this.alpha(), 0, 1));
    }

    @Override
    public RGBColor mixWith(RGBColor color, float bias) {
        float i = 1 - bias;
        float r = this.red() * i + color.red() * bias;
        float g = this.green() * i + color.green() * bias;
        float b = this.blue() * i + color.blue() * bias;
        float a = this.alpha() * i + color.alpha() * bias;

        return new RGBColor(r, g, b, a);
    }

    @Override
    public RGBColor fromRGB(RGBColor rgb) {
        return this;
    }

    public int toInt() {
        return combine((int) (this.alpha() * 255), (int) (this.blue() * 255),
                (int) (this.green() * 255), (int) (this.red() * 255));
    }
}

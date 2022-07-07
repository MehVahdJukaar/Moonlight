package net.mehvahdjukaar.moonlight.api.util.math.colors;

import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.util.Mth;

import javax.annotation.concurrent.Immutable;

@Immutable
public class RGBColor extends BaseColor<RGBColor> {

    //color from packed int
    public RGBColor(int value) {
        this(NativeImage.getR(value) / 255f, NativeImage.getG(value) / 255f,
                NativeImage.getB(value) / 255f, NativeImage.getA(value) / 255f);

    }

    public RGBColor(float r, float g, float b, float a) {
        super(Mth.clamp(r, 0, 1), Mth.clamp(g, 0, 1),
                Mth.clamp(b, 0, 1), Mth.clamp(a, 0, 1));
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
        return NativeImage.combine((int) (this.alpha() * 255), (int) (this.blue() * 255),
                (int) (this.green() * 255), (int) (this.red() * 255));
    }
}

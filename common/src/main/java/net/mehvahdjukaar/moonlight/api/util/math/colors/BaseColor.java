package net.mehvahdjukaar.moonlight.api.util.math.colors;

import com.mojang.serialization.Codec;
import net.mehvahdjukaar.moonlight.api.util.math.ColorUtils;
import net.minecraft.util.Mth;

import java.util.List;

public abstract class BaseColor<T extends BaseColor<T>> {

    protected final float v0;
    protected final float v1;
    protected final float v2;
    protected final float v3;

    protected BaseColor(float v0, float v1, float v2, float v3) {
        this.v0 = v0;
        this.v1 = v1;
        this.v2 = v2;
        this.v3 = v3;
    }

    public float distTo(T other) {
        return (float) Math.sqrt((this.v0 - other.v0) * (this.v0 - other.v0) +
                (this.v1 - other.v1) * (this.v1 - other.v1) +
                (this.v2 - other.v2) * (this.v2 - other.v2));
    }

    public T mixWith(T color) {
        return mixWith(color, 0.5f);
    }

    /**
     * Mix two color together. Result varies with respect to their colorspace
     *
     * @param color second color
     * @param bias  how much of the second color should appear in the result
     * @return mixed color
     */
    public T mixWith(T color, float bias) {
        return color;
    }

    public abstract T multiply(float v0, float v1, float v2, float v3);

    /**
     * Utility to mixe multiple colors at once in equal parts
     */
    public static <C extends BaseColor<C>> C mixColors(List<C> colors) {
        int size = colors.size();
        C mixed = colors.get(0);
        for (int i = 1; i < size; i++) {
            mixed = mixed.mixWith(colors.get(i), 1 / (i + 1f));
        }
        return mixed;
    }

    public static <C extends BaseColor<C>> C mixColors(C... colors) {
        return mixColors(List.of(colors));
    }

    public abstract RGBColor asRGB();

    public HSLColor asHSL() {
        return this instanceof HSLColor c ? c : ColorSpaces.RGBtoHSL(this.asRGB());
    }

    public HSVColor asHSV() {
        return this instanceof HSVColor c ? c : ColorSpaces.RGBtoHSV(this.asRGB());
    }

    public XYZColor asXYZ() {
        return this instanceof XYZColor c ? c : ColorSpaces.RGBtoXYZ(this.asRGB());
    }

    public LABColor asLAB() {
        return this instanceof LABColor c ? c : ColorSpaces.XYZtoLAB(this.asXYZ());
    }

    public HCLColor asHCL() {
        return this instanceof HCLColor c ? c : ColorSpaces.LABtoHCL(this.asLAB());
    }

    public LUVColor asLUV() {
        return this instanceof LUVColor c ? c : ColorSpaces.XYZtoLUV(this.asXYZ());
    }

    public HCLVColor asHCLV() {
        return this instanceof HCLVColor c ? c : ColorSpaces.LUVtoHCLV(this.asLUV());
    }


    public static float weightedAverageAngles(float a, float b, float bias) {
        return Mth.rotLerp(bias, a * 360, b * 360) / 360f;
    }

    protected static float averageAngles(Float... angles) {
        float x = 0;
        float y = 0;
        for (float a : angles) {
            assert a >= 0 && a <= 1;
            x += Math.cos((float) (a * Math.PI * 2));
            y += Math.sin((float) (a * Math.PI * 2));
        }
        double a = (Math.atan2(y, x) / (Math.PI * 2));
        return (float) a;
    }

    public abstract T fromRGB(RGBColor rgb);

}

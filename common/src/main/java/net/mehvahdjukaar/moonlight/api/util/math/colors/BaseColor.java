package net.mehvahdjukaar.moonlight.api.util.math.colors;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;

import oshi.annotation.concurrent.Immutable;
import java.util.List;

@Immutable
public abstract class BaseColor<T extends BaseColor<T>> {

    //utility codec that serializes either a string or an integer
    public static final Codec<Integer> CODEC = Codec.either(Codec.INT,
            Codec.STRING.flatXmap(BaseColor::isValidString, BaseColor::isValidString)).xmap(either ->
                    either.map(i -> i, s -> Integer.parseUnsignedInt(s, 16)),
            i -> Either.right("#" + Integer.toHexString(i))
    );

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

    public abstract T multiply(T color, float v0, float v1, float v2, float v3);

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


    @NotNull
    private static DataResult<String> isValidString(String s) {
        String st = s;
        if (s.startsWith("0x")) st = s.substring(2);
        else if (s.startsWith("#")) st = s.substring(1);
        try {
            Integer.parseUnsignedInt(st, 16);
            return DataResult.success(st);
        } catch (NumberFormatException e) {
            return DataResult.error("Invalid color format. Must be in hex format (0xff00ff, #ff00ff, ff00ff) or integer value");
        }
    }
}

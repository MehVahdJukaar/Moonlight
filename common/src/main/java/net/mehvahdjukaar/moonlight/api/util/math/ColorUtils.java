package net.mehvahdjukaar.moonlight.api.util.math;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import net.mehvahdjukaar.codecui.SchemaCodecs;
import net.mehvahdjukaar.moonlight.api.util.math.colors.HSVColor;
import net.mehvahdjukaar.moonlight.api.util.math.colors.RGBColor;
import net.minecraft.core.Direction;
import net.minecraft.util.ARGB;
import org.joml.Vector3f;

import java.util.Locale;

public final class ColorUtils {

    //utility codec that serializes either a string or an integer.
    //wrapped as an ARGB color SchemaCodec so codecui-driven editors render a color picker (serialization is unchanged)
    public static final Codec<Integer> CODEC = SchemaCodecs.colorArgb(hexOrIntCodec(true));

    public static final Codec<Integer> RGB_CODEC = SchemaCodecs.colorRgb(
            hexOrIntCodec(false).xmap(i -> i & 0xFFFFFF, i -> i & 0xFFFFFF));

    public static Codec<Integer> codec(boolean hasAlpha) {
        return hasAlpha ? CODEC : RGB_CODEC;
    }

    private static Codec<Integer> hexOrIntCodec(boolean hasAlpha) {
        return Codec.either(Codec.INT,
                Codec.STRING.flatXmap(ColorUtils::isValidStringOrError, s -> isValidStringOrError(s)
                        .map(ColorUtils::formatString))).xmap(
                either -> either.map(integer -> integer, s -> Integer.parseUnsignedInt(s, 16)),
                integer -> Either.right(toHexString(integer, hasAlpha))
        );
    }

    private static String formatString(String s) {
        return "#" + s.toUpperCase(Locale.ROOT);
    }

    public static DataResult<String> isValidStringOrError(String s) {
        String st = s;
        if (s.startsWith("0x")) {
            st = s.substring(2);
        } else if (s.startsWith("#")) {
            st = s.substring(1);
        }

        // Enforce the maximum length of eight characters (including prefix)
        if (st.length() > 8) {
            return DataResult.error(() -> "Invalid color format. Hex value must have up to 8 characters.");
        }

        try {
            int parsedValue = Integer.parseUnsignedInt(st, 16);
            return DataResult.success(st);
        } catch (NumberFormatException e) {
            return DataResult.error(() -> "Invalid color format. Must be in ARGB hex format ('0xff00ff00', '#ff00ff00', 'ff00ff00') or its Integer value");
        }
    }

    public static boolean isValidString(String s) {
        return isValidStringOrError(s).result().isPresent();
    }

    /**
     * Parses an ARGB color from a hex string (accepts 0x, # or no prefix). Throws if invalid.
     */
    public static int parseHex(String s) {
        return Integer.parseUnsignedInt(isValidStringOrError(s).getOrThrow(), 16);
    }

    public static String toHexString(int argb) {
        return "#" + String.format("%08X", argb);
    }

    public static String toHexString(int color, boolean hasAlpha) {
        return hasAlpha
                ? "#" + String.format("%08X", color)
                : "#" + String.format("%06X", color & 0xFFFFFF);
    }

    public static int hsvToArgb(float hue, float saturation, float value, int alpha) {
        // HSVColor/RGBColor pack ABGR; our config colors are ARGB, so swap on the way out
        return swapFormat(new HSVColor(hue, saturation, value, alpha / 255f).asRGB().toInt());
    }

    /**
     * {hue, saturation, value}, all 0..1. Alpha is ignored.
     */
    public static float[] argbToHsv(int argb) {
        HSVColor hsv = new RGBColor(swapFormat(argb)).asHSV();
        return new float[]{hsv.hue(), hsv.saturation(), hsv.value()};
    }

    private static final Vector3f DIFFUSE_LIGHT_0 = (new Vector3f(0.2F, 1.0F, -0.7F)).normalize();
    private static final Vector3f DIFFUSE_LIGHT_1 = (new Vector3f(-0.2F, 1.0F, 0.7F)).normalize();
    public static final float MINECRAFT_LIGHT_POWER = 0.6f;
    public static final float MINECRAFT_AMBIENT_LIGHT = 0.4f;

    //equivalent of function with same name used by the game to calculate shading
    public static int shadeColor(Vector3f normal, int color) {
        return multiply(color, getShading(normal));
    }

    public static float getShading(Vector3f normal) {
        if (normal.equals(Direction.UP.step())) return 1;
        Vector3f lightDir0 = DIFFUSE_LIGHT_0;//RenderSystem.shaderLightDirections[0];
        Vector3f lightDir1 = DIFFUSE_LIGHT_1;//RenderSystem.shaderLightDirections[1];
        lightDir0.normalize();
        lightDir1.normalize();
        float light0 = Math.max(0.0f, lightDir0.dot(normal));
        float light1 = Math.max(0.0f, lightDir1.dot(normal));
        return Math.min(1.0f, (light0 + light1) * MINECRAFT_LIGHT_POWER + MINECRAFT_AMBIENT_LIGHT);
    }


    //component wise multiplication
    public static int multiply(int color, float amount) {
        if (amount == 1) return color;
        int r = Math.min(255, (int) (ARGB.red(color) * amount));
        int g = Math.min(255, (int) (ARGB.green(color) * amount));
        int b = Math.min(255, (int) (ARGB.blue(color) * amount));
        return ARGB.color(0, r, g, b);
    }

    public static int lerp(int c0, int c1, float t) {
        if (t == 0) return c0;
        if (t == 1) return c1;
        RGBColor col = new RGBColor(c0);
        return col.mixWith(new RGBColor(c1), t).toInt();
    }

    //ARGB to ABGR and vice versa
    public static int swapFormat(int argb) {
        return (argb & 0xFF00FF00)
                | ((argb >> 16) & 0x000000FF)
                | ((argb << 16) & 0x00FF0000);
    }

    public static int pack(float[] rgb) {
        return ARGB.color(255, (int) (rgb[0] * 255), (int) (rgb[1] * 255), (int) (rgb[2] * 255));
    }

    public static float[] unpack(int color) {
        int argb = ARGB.fromABGR(color);
        return new float[]{ARGB.red(argb) / 255f, ARGB.green(argb) / 255f, ARGB.blue(argb) / 255f};
    }
}

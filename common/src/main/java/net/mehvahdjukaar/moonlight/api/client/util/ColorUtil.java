package net.mehvahdjukaar.moonlight.api.client.util;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import net.mehvahdjukaar.moonlight.api.util.math.ColorUtils;
import net.minecraft.util.FastColor;
import org.joml.Vector3f;

@Deprecated(forRemoval = true)
public class ColorUtil {

    //this should  not be in a client only class. Move this in common
    //utility codec that serializes either a string or an integer
    public static final Codec<Integer> CODEC = ColorUtils.CODEC;

    public static DataResult<String> isValidStringOrError(String s) {
   return ColorUtils.isValidStringOrError(s);
    }

    public static boolean isValidString(String s) {
        return isValidStringOrError(s).result().isPresent();
    }

    public static final float MINECRAFT_LIGHT_POWER = 0.6f;
    public static final float MINECRAFT_AMBIENT_LIGHT = 0.4f;

    //equivalent of function with same name used by the game to calculate shading
    public static int shadeColor(Vector3f normal, int color) {
        return multiply(color, getShading(normal));
    }

    public static float getShading(Vector3f normal) {
return ColorUtils.getShading(normal);
    }


    //component wise multiplication
    public static int multiply(int color, float amount) {
       return ColorUtils.multiply(color,amount);
    }

    //ARGB to ABGR and vice versa
    public static int swapFormat(int argb) {
        return (argb & 0xFF00FF00)
                | ((argb >> 16) & 0x000000FF)
                | ((argb << 16) & 0x00FF0000);
    }

    public static int pack(float[] rgb) {
        return FastColor.ARGB32.color(255, (int) (rgb[0] * 255), (int) (rgb[1] * 255), (int) (rgb[2] * 255));
    }
}

package net.mehvahdjukaar.moonlight.api.client.util;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.core.Direction;
import net.minecraft.util.FastColor;
import org.joml.Vector3f;

public class ColorUtil {

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
        Vector3f lightDir0 = RenderSystem.shaderLightDirections[0];
        Vector3f lightDir1 = RenderSystem.shaderLightDirections[1];
        lightDir0.normalize();
        lightDir1.normalize();
        float light0 = Math.max(0.0f, lightDir0.dot(normal));
        float light1 = Math.max(0.0f, lightDir1.dot(normal));
        return Math.min(1.0f, (light0 + light1) * MINECRAFT_LIGHT_POWER + MINECRAFT_AMBIENT_LIGHT);
    }


    //component wise multiplication
    public static int multiply(int color, float amount) {
        if (amount == 1) return color;
        int j = Math.min(255, (int) (FastColor.ABGR32.red(color) * amount));
        int k = Math.min(255, (int) (FastColor.ABGR32.green(color) * amount));
        int l = Math.min(255, (int) (FastColor.ABGR32.blue(color) * amount));
        return FastColor.ABGR32.color(0, l, k, j);
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

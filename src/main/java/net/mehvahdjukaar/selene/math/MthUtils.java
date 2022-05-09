package net.mehvahdjukaar.selene.math;

import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

public class MthUtils {

    public static float[] polarToCartesian(float a, float r) {
        float x = r * Mth.cos(a);
        float y = r * Mth.sin(a);
        return new float[]{x, y};
    }

    public static float signedAngleDiff(double to, double from) {
        float x1 = Mth.cos((float) to);
        float y1 = Mth.sin((float) to);
        float x2 = Mth.cos((float) from);
        float y2 = Mth.sin((float) from);
        return (float) Mth.atan2(x1 * y1 - y1 * x2, x1 * x2 + y1 * y2);
    }


    //vector relative to a new basis
    public static Vec3 changeBasisN(Vec3 newBasisYVector, Vec3 rot) {
        Vec3 y = newBasisYVector.normalize();
        Vec3 x = new Vec3(y.y, y.z, y.x).normalize();
        Vec3 z = y.cross(x).normalize();
        return changeBasis(x, y, z, rot);
    }

    public static Vec3 changeBasis(Vec3 newX, Vec3 newY, Vec3 newZ, Vec3 rot) {
        return newX.scale(rot.x).add(newY.scale(rot.y)).add(newZ.scale(rot.z));
    }

    public static Vec3 getNormalFrom3DData(int direction) {
        return V3toV3i(Direction.from3DDataValue(direction).getNormal());
    }

    public static Vec3 V3toV3i(Vec3i v) {
        return new Vec3(v.getX(), v.getY(), v.getZ());
    }


}

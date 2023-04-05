package net.mehvahdjukaar.moonlight.api.util.math;

import net.mehvahdjukaar.moonlight.api.util.math.colors.BaseColor;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.UnaryOperator;

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
        return V3itoV3(Direction.from3DDataValue(direction).getNormal());
    }

    public static Vec3 V3itoV3(Vec3i v) {
        return new Vec3(v.getX(), v.getY(), v.getZ());
    }

    private static double isClockWise(UnaryOperator<Vec3> rot, Direction dir) {
        Vec3 v = MthUtils.V3itoV3(dir.getNormal());
        Vec3 v2 = rot.apply(v);
        return v2.dot(new Vec3(0, 1, 0));
    }

    /**
     * Gives a vector that is equal to the one given rotated on the Y axis by a given direction
     *
     * @param dir horizontal direction
     */
    public static Vec3 rotateVec3(Vec3 vec, Direction dir) {
        double cos = 1;
        double sin = 0;
        switch (dir) {
            case SOUTH -> {
                cos = -1;
                sin = 0;
            }
            case WEST -> {
                cos = 0;
                sin = 1;
            }
            case EAST -> {
                cos = 0;
                sin = -1;
            }
            case UP -> {
                return new Vec3(vec.x, -vec.z, vec.y);
            }
            case DOWN -> {
                return new Vec3(vec.x, vec.z, vec.y);
            }
        }
        double dx = vec.x * cos + vec.z * sin;
        double dy = vec.y;
        double dz = vec.z * cos - vec.x * sin;
        return new Vec3(dx, dy, dz);
    }

    /**
     * Takes angles from 0 to 1
     *
     * @return mean angle
     */
    public static float averageAngles(Float... angles) {
        float x = 0, y = 0;
        for (float a : angles) {
            x += Mth.cos((float) (a * Math.PI * 2));
            y += Mth.sin((float) (a * Math.PI * 2));
        }
        return (float) (Mth.atan2(y, x) / (Math.PI * 2));
    }

    public static double wrapRad(double pValue) {
        double p = Math.PI * 2;
        double d0 = pValue % p;
        if (d0 >= Math.PI) {
            d0 -= p;
        }

        if (d0 < -Math.PI) {
            d0 += p;
        }

        return d0;
    }

    public static float wrapRad(float pValue) {
        float p = (float) (Math.PI * 2);
        float d0 = pValue % p;
        if (d0 >= Math.PI) {
            d0 -= p;
        }

        if (d0 < -Math.PI) {
            d0 += p;
        }

        return d0;
    }

    /**
     * @param rand a rng
     * @param max  maximum value. Has to be >0
     * @param bias positive getValues skew the average towards 0 (has to be from 0 to infinity).
     *             negative toward max (has to be from 0 to negative infinity). Values <= -1 are invalid.
     *             Setting it to 0 is equivalent to rand.nextFloat()*max.
     *             bias = 1 is slightly skewed towards 0 with average 0.38*max
     * @return a number between 0 and max
     * The bias parameters control how much the average is skewed toward 0 or max
     */
    public static float nextWeighted(RandomSource rand, float max, float bias) {
        float r = rand.nextFloat();
        if (bias <= 0) {
            if (bias == 0) return r * max;
            //mapping 0 -1 to 0 -inf
            bias = -bias / (bias - 1);
        }
        return (max * (1 - r)) / ((bias * max * r) + 1);
    }

    /**
     * Same as above but value is included between max and min
     */
    public static float nextWeighted(RandomSource rand, float max, float bias, float min) {
        return nextWeighted(rand, max - min, bias) + min;
    }

    public static float nextWeighted(RandomSource rand, float max) {
        return nextWeighted(rand, max, 1);
    }

    /**
     * Golden ratio
     */
    public static final float PHI = (float) (1 + (Math.sqrt(5d) - 1) / 2f);

    public static <T extends BaseColor<T>> T lerpColorScale(List<T> palette, float phase) {
        if (phase >= 1) phase = phase % 1;

        int n = palette.size();
        float g = n * phase;
        int ind = (int) Math.floor(g);

        float delta = g % 1;
        T start = palette.get(ind);
        T end = palette.get((ind + 1) % n);

        return start.mixWith(end, delta);
    }

    public static boolean isWithinRectangle(int x, int y, int width, int height, int mouseX, int mouseY) {
        return mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
    }

    public static VoxelShape rotateVoxelShape(VoxelShape source, Direction direction) {
        if (direction == Direction.NORTH) return source;
        AtomicReference<VoxelShape> newShape = new AtomicReference<>(Shapes.empty());
        source.forAllBoxes((minX, minY, minZ, maxX, maxY, maxZ) -> {
            Vec3 min = new Vec3(minX - 0.5, minY - 0.5, minZ - 0.5);
            Vec3 max = new Vec3(maxX - 0.5, maxY - 0.5, maxZ - 0.5);
            Vec3 v1 = MthUtils.rotateVec3(min, direction);
            Vec3 v2 = MthUtils.rotateVec3(max, direction);
            VoxelShape s = Shapes.create(0.5 + Math.min(v1.x, v2.x), 0.5 + Math.min(v1.y, v2.y), 0.5 + Math.min(v1.z, v2.z),
                    0.5 + Math.max(v1.x, v2.x), 0.5 + Math.max(v1.y, v2.y), 0.5 + Math.max(v1.z, v2.z));
            newShape.set(Shapes.or(newShape.get(), s));
        });
        return newShape.get();
    }

    public static VoxelShape moveVoxelShape(VoxelShape source, Vec3 v) {
        AtomicReference<VoxelShape> newShape = new AtomicReference<>(Shapes.empty());
        source.forAllBoxes((minX, minY, minZ, maxX, maxY, maxZ) -> {
            VoxelShape s = Shapes.create(minX + v.x, minY + v.y, minZ + v.z,
                    maxX + v.x, maxY + v.y, maxZ + v.z);
            newShape.set(Shapes.or(newShape.get(), s));
        });
        return newShape.get();
    }

}

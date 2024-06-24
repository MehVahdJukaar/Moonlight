package net.mehvahdjukaar.moonlight.api.util.math;

import net.mehvahdjukaar.moonlight.api.util.math.colors.BaseColor;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

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

    // in degrees. Opposite of Vec3.fromRotation
    public static double getPitch(Vec3 vec3) {
        return -Math.toDegrees(Math.asin(vec3.y));
    }

    // in degrees
    public static double getYaw(Vec3 vec3) {
        return Math.toDegrees(Math.atan2(-vec3.x, vec3.z));
    }

    // not sure about this one tbh
    public static double getRoll(Vec3 vec3) {
        return Math.toDegrees(Math.atan2(vec3.y, vec3.x));
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
     * @param bias when a positive number, skew the average towards 0 (has to be from 0 to infinity).
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


    public static double lambertW0(double x) {
        double maxError = 1e-6;
        if (x == -1 / Math.E) {
            return -1;
        } else if (x >= -1 / Math.E) {
            double nLog = Math.log(x);
            double nLog0 = 1;
            while (Math.abs(nLog0 - nLog) > maxError) {
                nLog0 = (x * Math.exp(-nLog)) / (1 + nLog);
                nLog = (x * Math.exp(-nLog0)) / (1 + nLog0);
            }
            // precision of the return value
            return (Math.round(1000000 * nLog) / 1000000);
        } else {
            throw new IllegalArgumentException("Not in valid range for lambertW function. x has to be greater than or equal to -1/e.");
        }
    }

    // just brute forces it with newton approximation method
    // this only uses the secondary branch of the W function
    public static double lambertW1(double x) {
        double maxError = 1e-6;
        if (x == -1 / Math.E) {
            return -1;
        } else if (x < 0 && x > -1 / Math.E) {
            double nLog = Math.log(-x);
            double nLog0 = 1;
            while (Math.abs(nLog0 - nLog) > maxError) {
                nLog0 = (nLog * nLog + x / Math.exp(nLog)) / (nLog + 1);
                nLog = (nLog0 * nLog0 + x / Math.exp(nLog0)) / (nLog0 + 1);
            }
            // precision of the return value
            return (Math.round(1000000 * nLog) / 1000000);
        } else if (x == 0) {
            return 0;
        } else {
            throw new IllegalArgumentException("Not in valid range for lambertW function. x has to be in [-1/e,0]");
        }
    }

    /**
     * Exponent function that passed by 0,0 and 1,1
     */
    private static float exp01(float t, float base) {
        return (float) (base * Math.pow(1 / base + 1, t) - base);
    }

    /**
     * An exponent function that passes by 0,0 and 1,1
     *
     * @param t     time
     * @param curve determines the "curve" of the exponent graph.
     *              0 will be a line
     *              from 0 to 1 will curve with increasing severity (edge cases with vertical line at 1, which is not a valid input)
     *              from 0 to -1 will curve downwards in the same manner
     *              This parameter essentially controls the base of the exponent
     *              0.55 happens to map to a base close to Euler's number
     */
    public static float normalizedExponent(float t, float curve) {
        if (curve == 0) return t;
        float base;
        if (curve > 0) {
            base = (float) -Math.log(curve);
        } else {
            base = (float) (Math.log(-curve) - 1);
        }
        return exp01(t, base);
    }



    // collision code


    public static BlockHitResult collideWithSweptAABB(Entity entity, Vec3 movement, double maxStep) {
        AABB aabb = entity.getBoundingBox();
        return collideWithSweptAABB(entity.position(), aabb, movement, entity.level(), maxStep);
    }

    /**
     * Unlike vanilla .collide method this will have no tunnelling whatsoever and will stop the entity exactly when the first collision happens
     * It's somehow also more efficient than the vanilla method, around 2 times.
     */
    public static BlockHitResult collideWithSweptAABB(Vec3 myPos, AABB myBox, Vec3 movement, Level level, double maxStep) {
        double len = movement.length();
        if (maxStep >= len) return collideWithSweptAABB(myPos, myBox, movement, level);
        double step = 0;
        while (step < len) {
            Vec3 stepMovement = movement.scale(step / len);
            BlockHitResult result = collideWithSweptAABB(myPos, myBox, stepMovement, level);
            if (result.getType() != HitResult.Type.MISS) {
                return result;
            }
            step += maxStep;
            step = Math.min(step, len);
        }
        Vec3 missPos = myPos.add(movement);
        return BlockHitResult.miss(missPos, Direction.UP, BlockPos.containing(missPos));
    }

    public static BlockHitResult collideWithSweptAABB(Vec3 myPos, AABB myBox, Vec3 movement, Level level) {
        AABB encompassing = myBox.expandTowards(movement);
        Set<BlockPos> positions = BlockPos.betweenClosedStream(encompassing)
                .map(BlockPos::immutable).collect(Collectors.toSet());

        CollisionResult earliestCollision = null;
        BlockPos hitPos = null;

        for (BlockPos pos : positions) {
            BlockState state = level.getBlockState(pos);
            if (state.isAir()) continue;
            List<AABB> boxes = state.getCollisionShape(level, pos).toAabbs();
            for (AABB box : boxes) {
                box = box.move(pos);
                CollisionResult result = sweptAABB(myBox, box, movement);
                if (result == null || result.entryTime < 0) continue;
                if (earliestCollision == null) {
                    earliestCollision = result;
                    hitPos = pos;
                } else if (result.entryTime == earliestCollision.entryTime) {
                    Vec3 collidedPos = myPos.add(movement.scale(result.entryTime));
                    if (pos.distToCenterSqr(collidedPos) < hitPos.distToCenterSqr(collidedPos)) {
                        earliestCollision = result;
                        hitPos = pos;
                    }
                } else if (result.entryTime < earliestCollision.entryTime) {
                    earliestCollision = result;
                    hitPos = pos;
                }
            }
        }


        if (earliestCollision != null && earliestCollision.entryTime < 1.0) {
            movement = movement.scale(earliestCollision.entryTime);
            Vec3 finalPos = myPos.add(movement);

            return new BlockHitResult(finalPos, earliestCollision.direction, hitPos, false);
        }

        Vec3 missPos = myPos.add(movement);
        return BlockHitResult.miss(missPos, Direction.UP, BlockPos.containing(missPos));
    }

    private static CollisionResult sweptAABB(AABB movingBox, AABB staticBox, Vec3 movement) {
        double entryX, entryY, entryZ;
        double exitX, exitY, exitZ;
        Direction collisionDirection;

        if (movement.x > 0.0) {
            entryX = (staticBox.minX - movingBox.maxX) / movement.x;
            exitX = (staticBox.maxX - movingBox.minX) / movement.x;
        } else if (movement.x < 0.0) {
            entryX = (staticBox.maxX - movingBox.minX) / movement.x;
            exitX = (staticBox.minX - movingBox.maxX) / movement.x;
        } else {
            entryX = Double.NEGATIVE_INFINITY;
            exitX = Double.POSITIVE_INFINITY;
        }

        if (movement.y > 0.0) {
            entryY = (staticBox.minY - movingBox.maxY) / movement.y;
            exitY = (staticBox.maxY - movingBox.minY) / movement.y;
        } else if (movement.y < 0.0) {
            entryY = (staticBox.maxY - movingBox.minY) / movement.y;
            exitY = (staticBox.minY - movingBox.maxY) / movement.y;
        } else {
            entryY = Double.NEGATIVE_INFINITY;
            exitY = Double.POSITIVE_INFINITY;
        }

        if (movement.z > 0.0) {
            entryZ = (staticBox.minZ - movingBox.maxZ) / movement.z;
            exitZ = (staticBox.maxZ - movingBox.minZ) / movement.z;
        } else if (movement.z < 0.0) {
            entryZ = (staticBox.maxZ - movingBox.minZ) / movement.z;
            exitZ = (staticBox.minZ - movingBox.maxZ) / movement.z;
        } else {
            entryZ = Double.NEGATIVE_INFINITY;
            exitZ = Double.POSITIVE_INFINITY;
        }

        double entryTime = Math.max(Math.max(entryX, entryY), entryZ);
        double exitTime = Math.min(Math.min(exitX, exitY), exitZ);

        if (entryTime > exitTime || (entryX < 0.0 && entryY < 0.0 && entryZ < 0.0) || entryX > 1.0 || entryY > 1.0 || entryZ > 1.0) {
            return null;
        }

        if (entryX > entryY && entryX > entryZ) {
            if (movement.x > 0.0) {
                collisionDirection = Direction.EAST;
            } else {
                collisionDirection = Direction.WEST;
            }
        } else if (entryY > entryZ) {
            if (movement.y > 0.0) {
                collisionDirection = Direction.UP;
            } else {
                collisionDirection = Direction.DOWN;
            }
        } else {
            if (movement.z > 0.0) {
                collisionDirection = Direction.SOUTH;
            } else {
                collisionDirection = Direction.NORTH;
            }
        }

        return new CollisionResult(entryTime, collisionDirection);
    }

    private record CollisionResult(double entryTime, Direction direction) {
    }

}

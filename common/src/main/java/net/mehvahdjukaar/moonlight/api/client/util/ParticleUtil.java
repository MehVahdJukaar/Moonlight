package net.mehvahdjukaar.moonlight.api.client.util;

import net.mehvahdjukaar.moonlight.core.client.MLRenderTypes;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.TerrainParticle;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.function.Supplier;

public class ParticleUtil {

    public static void spawnParticleInASphere(Level level, double x, double y, double z, Supplier<ParticleOptions> type,
                                              int amount, float speed,
                                              float angleVariation, float speedVariation) {
        double azimuthIncrement = Math.PI * (3 - Math.sqrt(5)); // Golden angle

        for (int i = 0; i < amount; i++) {
            double inclination = Math.acos(1 - (2 * (i + 0.5) / amount)); // Angle from the pole
            double azimuth = azimuthIncrement * i; // Rotation around the axis

            if (angleVariation != 0) {
                inclination += level.random.nextFloat() * angleVariation - angleVariation / 2;
                azimuth += level.random.nextFloat() * angleVariation - angleVariation / 2;
            }

            float s = speed;
            if (speedVariation != 0) {
                s += level.random.nextFloat() * speedVariation - speedVariation / 2;
            }

            double vx = s * Math.sin(inclination) * Math.cos(azimuth);
            double vy = s * Math.sin(inclination) * Math.sin(azimuth);
            double vz = s * Math.cos(inclination);

            level.addParticle(type.get(), x, y, z, vx, vy, vz);
        }
    }

    //call with packet

    public static void spawnParticleOnBlockShape(Level level, BlockPos pos, ParticleOptions particleOptions,
                                                 UniformInt uniformInt, float maxSpeed) {
        spawnParticleOnBoundingBox(level.getBlockState(pos).getShape(level, pos).bounds().move(pos), level,
                particleOptions, uniformInt, maxSpeed);
    }

    public static void spawnParticleOnBoundingBox(AABB bb, Level level, ParticleOptions particleOptions,
                                                  UniformInt uniformInt, float maxSpeed) {

        RandomSource random = level.random;
        float offset = 0.1f;
        Vec3 blockCenter = new Vec3(bb.minX - 0.5 + (bb.maxX - bb.minX) / 2f, bb.minY - 0.5 + (bb.maxY - bb.minY) / 2f, bb.minZ - 0.5 + (bb.maxZ - bb.minZ) / 2f);
        bb = bb.move(-blockCenter.x, -blockCenter.y, -blockCenter.z);
        //north
        int i = uniformInt.sample(random);
        for (int j = 0; j < i; ++j) {
            double x = random.nextDouble();
            double y = random.nextDouble();
            if (x > bb.minX && x < bb.maxX && y > bb.minY && y < bb.maxY) {
                double dx = maxSpeed * level.random.nextDouble();
                double dy = maxSpeed * level.random.nextDouble();
                double dz = 0;
                level.addParticle(particleOptions, blockCenter.x + x, blockCenter.y + y, blockCenter.z + bb.minZ - offset, dx, dy, dz);
            }
        }
        //south
        i = uniformInt.sample(random);
        for (int j = 0; j < i; ++j) {
            double x = random.nextDouble();
            double y = random.nextDouble();
            if (x > bb.minX && x < bb.maxX && y > bb.minY && y < bb.maxY) {
                double dx = maxSpeed * level.random.nextDouble();
                double dy = maxSpeed * level.random.nextDouble();
                double dz = 0;
                level.addParticle(particleOptions, blockCenter.x() + x, blockCenter.y() + y, blockCenter.z() + bb.maxZ + offset, dx, dy, dz);
            }
        }
        //west
        i = uniformInt.sample(random);
        for (int j = 0; j < i; ++j) {
            double z = random.nextDouble();
            double y = random.nextDouble();
            if (z > bb.minZ && z < bb.maxZ && y > bb.minY && y < bb.maxY) {
                double dx = 0;
                double dy = maxSpeed * level.random.nextDouble();
                double dz = maxSpeed * level.random.nextDouble();
                level.addParticle(particleOptions, blockCenter.x() + bb.minX - offset, blockCenter.y() + y, blockCenter.z() + z, dx, dy, dz);
            }
        }
        //east
        i = uniformInt.sample(random);
        for (int j = 0; j < i; ++j) {
            double z = random.nextDouble();
            double y = random.nextDouble();
            if (z > bb.minZ && z < bb.maxZ && y > bb.minY && y < bb.maxY) {
                double dx = 0;
                double dy = maxSpeed * level.random.nextDouble();
                double dz = maxSpeed * level.random.nextDouble();
                level.addParticle(particleOptions, blockCenter.x() + bb.maxX + offset, blockCenter.y() + y, blockCenter.z() + z, dx, dy, dz);
            }
        }
        //down
        i = uniformInt.sample(random);
        for (int j = 0; j < i; ++j) {
            double x = random.nextDouble();
            double z = random.nextDouble();
            if (x > bb.minX && x < bb.maxX && z > bb.minZ && z < bb.maxZ) {
                double dx = maxSpeed * level.random.nextDouble();
                double dy = 0;
                double dz = maxSpeed * level.random.nextDouble();
                level.addParticle(particleOptions, blockCenter.x() + x, blockCenter.y() + bb.minY - offset, blockCenter.z() + z, dx, dy, dz);
            }
        }
        //up
        i = uniformInt.sample(random);
        for (int j = 0; j < i; ++j) {
            double x = random.nextDouble();
            double z = random.nextDouble();
            if (x > bb.minX && x < bb.maxX && z > bb.minZ && z < bb.maxZ) {
                double dx = maxSpeed * level.random.nextDouble();
                double dy = 0;
                double dz = maxSpeed * level.random.nextDouble();
                level.addParticle(particleOptions, blockCenter.x() + x, blockCenter.y() + bb.maxY + offset, blockCenter.z() + z, dx, dy, dz);
            }
        }
    }


    public static void spawnParticlesOnBlockFaces(Level level, BlockPos pos, ParticleOptions particleOptions,
                                                  UniformInt uniformInt, float minSpeed, float maxSpeed, boolean perpendicular) {
        for (Direction direction : Direction.values()) {
            int i = uniformInt.sample(level.random);

            for (int j = 0; j < i; ++j) {
                spawnParticleOnFace(level, pos, direction, particleOptions, minSpeed, maxSpeed, perpendicular);
            }
        }
    }

    public static void spawnParticleOnFace(Level level, BlockPos pos, Direction direction, ParticleOptions particleOptions,
                                           float minSpeed, float maxSpeed, boolean perpendicular) {
        Vec3 vec3 = Vec3.atCenterOf(pos);
        int i = direction.getStepX();
        int j = direction.getStepY();
        int k = direction.getStepZ();
        double d0 = vec3.x + (i == 0 ? Mth.nextDouble(level.random, -0.5D, 0.5D) : i * 0.6D);
        double d1 = vec3.y + (j == 0 ? Mth.nextDouble(level.random, -0.5D, 0.5D) : j * 0.6D);
        double d2 = vec3.z + (k == 0 ? Mth.nextDouble(level.random, -0.5D, 0.5D) : k * 0.6D);
        double dx;
        double dy;
        double dz;
        if (perpendicular) {
            dx = i * Mth.randomBetween(level.random, minSpeed, maxSpeed);
            dy = j * Mth.randomBetween(level.random, minSpeed, maxSpeed);
            dz = k * Mth.randomBetween(level.random, minSpeed, maxSpeed);
        } else {
            float d = maxSpeed - minSpeed;

            dx = (i == 0) ? (minSpeed + d * level.random.nextDouble()) : 0.0D;
            dy = (j == 0) ? (minSpeed + d * level.random.nextDouble()) : 0.0D;
            dz = (k == 0) ? (minSpeed + d * level.random.nextDouble()) : 0.0D;
        }
        level.addParticle(particleOptions, d0, d1, d2, dx, dy, dz);
    }


    public static void spawnBreakParticles(VoxelShape shape, BlockPos pPos, BlockState pState, Level level) {

        var particleEngine = Minecraft.getInstance().particleEngine;

        shape.forAllBoxes((x0, y0, z0, x1, y1, z1) -> {
            double d1 = Math.min(1.0D, x1 - x0);
            double d2 = Math.min(1.0D, y1 - y0);
            double d3 = Math.min(1.0D, z1 - z0);
            int i = Math.max(2, Mth.ceil(d1 / 0.25D));
            int j = Math.max(2, Mth.ceil(d2 / 0.25D));
            int k = Math.max(2, Mth.ceil(d3 / 0.25D));

            for (int l = 0; l < i; ++l) {
                for (int i1 = 0; i1 < j; ++i1) {
                    for (int j1 = 0; j1 < k; ++j1) {
                        double d4 = (l + 0.5D) / i;
                        double d5 = (i1 + 0.5D) / j;
                        double d6 = (j1 + 0.5D) / k;
                        double d7 = d4 * d1 + x0;
                        double d8 = d5 * d2 + y0;
                        double d9 = d6 * d3 + z0;
                        particleEngine.add(new TerrainParticle((ClientLevel) level, pPos.getX() + d7, pPos.getY() + d8,
                                pPos.getZ() + d9, d4 - 0.5D, d5 - 0.5D, d6 - 0.5D, pState, pPos));
                    }
                }
            }
        });
    }

    @Deprecated(forRemoval = true)
    public static final ParticleRenderType ADDITIVE_TRANSLUCENCY_RENDER_TYPE = MLRenderTypes.PARTICLE_ADDITIVE_TRANSLUCENCY_RENDER_TYPE;
}

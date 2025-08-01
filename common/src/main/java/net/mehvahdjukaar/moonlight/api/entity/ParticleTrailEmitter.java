package net.mehvahdjukaar.moonlight.api.entity;

import net.mehvahdjukaar.moonlight.api.misc.RollingBuffer;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

/**
 * A utility class for emitting particles along the trail at regular intervals no matter the speed of the entity.
 */

public class ParticleTrailEmitter {
    private final double wantedSpacing;
    private final int maxParticlesPerTick;
    private final double minSpeed;
    private Vec3 lastEmittedPos = null; // Track last emitted particle position

    private final RollingBuffer<Vec3> previousVelocities = new RollingBuffer<>(3);
    private final RollingBuffer<Vec3> previousPositions = new RollingBuffer<>(3);

    private ParticleTrailEmitter(Builder builder) {
        this.wantedSpacing = builder.idealSpacing;
        this.maxParticlesPerTick = builder.maxParticlesPerTick;
        this.minSpeed = builder.minSpeed;
    }

    public void tick(Entity obj, ParticleOptions particleOptions) {
        tick(obj, particleOptions, true);
    }

    public void tick(Entity obj, ParticleOptions particleOptions, boolean followSpeed) {
        tick(obj, (position, velocity) -> {
            var level = obj.level();
            if (followSpeed) {
                level.addParticle(particleOptions, position.x, position.y, position.z, velocity.x, velocity.y, velocity.z);
            } else {
                level.addParticle(particleOptions, position.x, position.y, position.z, 0, 0, 0);
            }
        });
    }

    public void tick(Entity obj, Emitter emitter) {
        Vec3 movement = obj.getDeltaMovement();
        previousVelocities.push(movement);
        previousPositions.push(obj.position());

        if (previousPositions.size() < 2) return;

        if (movement.lengthSqr() < (minSpeed * minSpeed)) return;

        Vec3 startPos = previousPositions.get(0);
        Vec3 endPos = previousPositions.get(1);
        Vec3 startVel = previousVelocities.get(0);
        Vec3 endVel = previousVelocities.get(1);

        if (lastEmittedPos == null) {
            lastEmittedPos = startPos;
            return;
        }

        double segmentLength = startPos.distanceTo(endPos);
        Double startT = intersectSphereSegment(lastEmittedPos, wantedSpacing, startPos, endPos);
        if (startT == null) {
            return;
        }

        double remainingLength = segmentLength * (1 - startT);
        int particlesToEmit = 1 + (int) (remainingLength / wantedSpacing); // +1 to include the first particle
        float spacing = (float) wantedSpacing;


        if (particlesToEmit > maxParticlesPerTick) {
            // If we have too many particles, adjust spacing to fit max particles per tick, equally spaced
            particlesToEmit = maxParticlesPerTick;
            spacing = (float) (remainingLength / particlesToEmit);
        }


        float h = obj.getBbHeight() / 2;
        for (int i = 0; i < particlesToEmit; i++) {
            double t = startT + (i * spacing / (float) segmentLength);
            if (t > 1.0f) {
                break; // Avoid going beyond the end of the segment
            }
            Vec3 position = startPos.lerp(endPos, t);
            Vec3 velocity = startVel.lerp(endVel, t);
            emitter.emitParticle(position.add(0, h, 0), velocity);
            lastEmittedPos = position;
        }
    }


    /**
     * Returns the segment percentage (t in [0, 1]) along the segment p1→p2 where the first intersection
     * with the sphere occurs. Returns null if no intersection on the segment.
     */
    private static Double intersectSphereSegment(Vec3 center, double radius, Vec3 start, Vec3 end) {
        Vec3 direction = end.subtract(start);        // Direction vector of the segment
        Vec3 oldDirection = start.subtract(center);    // Vector from center to p1

        double a = direction.dot(direction);
        double b = 2 * oldDirection.dot(direction);
        double c = oldDirection.dot(oldDirection) - radius * radius;

        double discriminant = b * b - 4 * a * c;

        if (discriminant < 0) {
            return null; // No intersection
        }

        double sqrtDiscriminant = (float) Math.sqrt(discriminant);
        double t1 = (-b - sqrtDiscriminant) / (2 * a);
        double t2 = (-b + sqrtDiscriminant) / (2 * a);

        // Return the first intersection that lies on the segment
        if (t1 >= 0 && t1 <= 1) {
            return Mth.clamp(t2, 0, 1); // Ensure t2 is clamped to [0, 1]
        }
        if (t2 >= 0 && t2 <= 1) {
            return Mth.clamp(t2, 0, 1); // Ensure t2 is clamped to [0, 1]
        }

        return null; // No intersection on the segment
    }


    public static Builder builder() {
        return new Builder();
    }

    // === Builder Class ===
    public static class Builder {
        private double idealSpacing = 0.5;
        private int maxParticlesPerTick = 5;
        private double minSpeed = 0.0;

        public Builder spacing(double spacing) {
            this.idealSpacing = spacing;
            return this;
        }

        public Builder maxParticlesPerTick(int max) {
            this.maxParticlesPerTick = max;
            return this;
        }

        public Builder minSpeed(double speed) {
            this.minSpeed = speed;
            return this;
        }

        public ParticleTrailEmitter build() {
            return new ParticleTrailEmitter(this);
        }
    }

    public interface Emitter {
        void emitParticle(Vec3 position, Vec3 velocity);
    }
}

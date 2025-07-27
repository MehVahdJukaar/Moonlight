package net.mehvahdjukaar.moonlight.api.entity;

import net.mehvahdjukaar.moonlight.api.misc.RollingBuffer;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.phys.Vec3;

public class ParticleTrailEmitter {

    private final double idealSpacing;
    private final int maxParticlesPerTick;
    private final double minSpeed;
    private Vec3 lastEmittedPos = null; // Track last emitted particle position

    private double accumulatedDistanceSinceLastParticle;

    private final RollingBuffer<Vec3> previousVelocities = new RollingBuffer<>(3);
    private final RollingBuffer<Vec3> previousPositions = new RollingBuffer<>(3);

    private ParticleTrailEmitter(Builder builder) {
        this.idealSpacing = builder.idealSpacing;
        this.maxParticlesPerTick = builder.maxParticlesPerTick;
        this.minSpeed = builder.minSpeed;
        this.accumulatedDistanceSinceLastParticle = -idealSpacing; // delay first particle emission
    }

    public void tick(Projectile obj, ParticleOptions particleOptions) {
        tick(obj, particleOptions, true);
    }

    public void tick(Projectile obj, ParticleOptions particleOptions, boolean followSpeed) {
        tick(obj, (position, velocity) -> {
            var level =  obj.level();
            if (followSpeed) {
                level.addParticle(particleOptions, position.x, position.y, position.z, velocity.x, velocity.y, velocity.z);
            } else {
                level.addParticle(particleOptions, position.x, position.y, position.z, 0, 0, 0);
            }
        });
    }

    public void tick(Projectile obj, Emitter emitter) {
        Vec3 currentVel = obj.getDeltaMovement();
        Vec3 currentPos = obj.position();

        previousVelocities.push(currentVel);
        previousPositions.push(currentPos);

        if (previousPositions.size() < 2) return;

        Vec3 prevPos = previousPositions.get(0);
        Vec3 currentPosBuf = previousPositions.get(1);
        Vec3 prevVel = previousVelocities.get(0);

        double segmentLength = prevPos.distanceTo(currentPosBuf);
        if (segmentLength < minSpeed) return;

        // Calculate how many particles we can emit
        double totalAvailable = accumulatedDistanceSinceLastParticle + segmentLength;
        int particlesToEmit = (int)(totalAvailable / idealSpacing);
        particlesToEmit = Math.min(particlesToEmit, maxParticlesPerTick);

        if (particlesToEmit == 0) {
            accumulatedDistanceSinceLastParticle += segmentLength;
            return;
        }

        // Calculate exact emission points
        Vec3 lastPos = (lastEmittedPos != null) ? lastEmittedPos : prevPos;
        double spacingSum = 0;

        for (int i = 1; i <= particlesToEmit; i++) {
            double targetDist = i * idealSpacing - accumulatedDistanceSinceLastParticle;
            double t = targetDist / segmentLength;
            t = Math.max(0, Math.min(1, t)); // Clamp to segment bounds

            Vec3 emitPos = prevPos.lerp(currentPosBuf, t);
            Vec3 emitVel = prevVel.lerp(previousVelocities.get(1), t);

            // Ensure perfect spacing
            Vec3 direction = emitPos.subtract(lastPos).normalize();
            Vec3 perfectPos = lastPos.add(direction.scale(idealSpacing));

            emitter.emitParticle(  perfectPos, emitVel);

            // Debug output
            double actualDist = perfectPos.distanceTo(lastPos);
            System.out.printf("Particle %d | Dist: %.6f | Ideal: %.6f%n",
                    i, actualDist, idealSpacing);

            lastPos = perfectPos;
            spacingSum += idealSpacing;
        }

        // Update state
        lastEmittedPos = lastPos;
        accumulatedDistanceSinceLastParticle = totalAvailable - spacingSum;
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
        void emitParticle(  Vec3 position, Vec3 velocity);
    }
}

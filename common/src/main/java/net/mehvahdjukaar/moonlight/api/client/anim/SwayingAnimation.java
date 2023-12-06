package net.mehvahdjukaar.moonlight.api.client.anim;

import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

import java.util.Random;
import java.util.function.Supplier;

public class SwayingAnimation extends SwingAnimation {

    //maximum allowed swing
    protected float maxSwingAngle = 45f;
    //minimum static swing
    protected float minSwingAngle = 2.5f;
    //max swing period
    protected float maxPeriod = 25f;

    protected float angleDamping = 150f;
    protected float periodDamping = 100f;


    // lower counter is used by hitting animation
    private int animationCounter = 800 + new Random().nextInt(80);
    private boolean inv = false;


    public SwayingAnimation(Supplier<Vector3f> getRotationAxis) {
        super(getRotationAxis);
    }

    @Override
    public void tick(boolean inWater) {

        //TODO: improve physics (water, swaying when it's not exposed to wind)

        this.animationCounter++;

        double timer = this.animationCounter;
        if (inWater) timer /= 2d;

        this.prevAngle = this.angle;
        //actually they are the inverse of damping. increase them to have less damping

        float a = minSwingAngle;
        float k = 0.01f;
        if (timer < 800) {
            a = (float) Math.max(maxSwingAngle * Math.exp(-(timer / angleDamping)), minSwingAngle);
            k = (float) Math.max(Math.PI * 2 * (float) Math.exp(-(timer / periodDamping)), 0.01f);
        }

        this.angle = a * Mth.cos((float) ((timer / maxPeriod) - k));
        this.angle *= this.inv ? -1 : 1;
        // this.angle = 90*(float)
        // Math.cos((float)counter/40f)/((float)this.counter/20f);;

    }

    @Override
    public void addImpulse(float vel) {
        //not added
    }

    @Override
    public void addPositiveImpulse(float vel) {
        //not added
    }

    @Override
    public float getAngle(float partialTicks) {
        return Mth.lerp(partialTicks, this.prevAngle, this.angle);
    }

    @Override
    public void reset() {
        animationCounter = 800;
    }

    @Override
    public boolean hit(Vec3 mot, double eMass) {
        if (mot.length() > 0.05) {

            Vec3 norm = new Vec3(mot.x, 0, mot.z).normalize();
            Vec3 vec = new Vec3(rotationAxis.get());
            double dot = norm.dot(vec);
            if (dot != 0) {
                this.inv = dot < 0;
            }
            if (Math.abs(dot) > 0.4) {
                if (this.animationCounter > 10) {
                    this.animationCounter = 0;
                    return true;
                } else {
                    this.animationCounter = 0;
                }
            }
        }
        return false;
    }

    @Override
    public boolean hitByEntity(Entity entity) {
        Vec3 mot = entity.getDeltaMovement();
        return hit(mot, 1);
    }
}

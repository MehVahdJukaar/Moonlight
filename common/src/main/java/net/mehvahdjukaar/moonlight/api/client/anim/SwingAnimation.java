package net.mehvahdjukaar.moonlight.api.client.anim;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

import java.util.function.Supplier;

public abstract class SwingAnimation {

   protected final Supplier<Vector3f> rotationAxis;
   protected float angle = 0;
   protected float prevAngle = 0;

   protected SwingAnimation(Supplier<Vector3f> axisGetter){
      this.rotationAxis = axisGetter;
   }

   public abstract void tick(boolean isInWater);
   // true if it should play sound

   //adds to velocity
   public abstract void addImpulse(float vel);

   public abstract void addPositiveImpulse(float vel);

   public abstract boolean hit(Vec3 eVel, double eMass);

   public abstract boolean hitByEntity(Entity entity);

   public abstract float getAngle(float partialTicks);

   public abstract void reset();
}

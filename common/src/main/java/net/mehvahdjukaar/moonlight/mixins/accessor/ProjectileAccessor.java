package net.mehvahdjukaar.moonlight.mixins.accessor;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.schedule.Activity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;
import java.util.Set;

@Mixin(Projectile.class)
public interface ProjectileAccessor {

    @Accessor("hasBeenShot")
    boolean getHasBeenShot();

    @Accessor("hasBeenShot")
    void setHasBeenShot(boolean value);
}

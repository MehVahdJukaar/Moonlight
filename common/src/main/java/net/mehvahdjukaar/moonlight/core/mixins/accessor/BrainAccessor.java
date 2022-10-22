package net.mehvahdjukaar.moonlight.core.mixins.accessor;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.minecraft.world.entity.schedule.Activity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;
import java.util.Set;

@Mixin(Brain.class)
public interface BrainAccessor<E extends LivingEntity> {

    @Accessor("sensors")
    Map<SensorType<? extends Sensor<? super E>>, Sensor<? super E>> getSensors();

    @Accessor("availableBehaviorsByPriority")
    Map<Integer, Map<Activity, Set<Behavior<? super E>>>> getAvailableBehaviorsByPriority();
}

package net.mehvahdjukaar.moonlight.core.mixins.accessor;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.BehaviorControl;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.minecraft.world.entity.schedule.Activity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.Map;
import java.util.Set;

@Mixin(Brain.class)
public interface BrainAccessor<E extends LivingEntity> {

    @Accessor("sensors")
    Map<SensorType<? extends Sensor<? super E>>, Sensor<? super E>> getSensors();

    @Accessor("availableBehaviorsByPriority")
    Map<Integer, Map<Activity, Set<BehaviorControl<? super E>>>> getAvailableBehaviorsByPriority();

    //brain memories only exist if registered up front, and setMemory throws on unregistered ones
    @Invoker("registerMemory")
    void invokeRegisterMemory(MemoryModuleType<?> memoryType);
}

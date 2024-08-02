package net.mehvahdjukaar.moonlight.api.events.neoforge;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Pair;
import net.mehvahdjukaar.moonlight.core.misc.VillagerBrainEventInternal;
import net.mehvahdjukaar.moonlight.api.events.IVillagerBrainEvent;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.behavior.BehaviorControl;
import net.minecraft.world.entity.ai.memory.ExpirableValue;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.schedule.Activity;
import net.neoforged.bus.api.Event;
import org.jetbrains.annotations.ApiStatus;

import java.util.Map;
import java.util.Optional;

public class VillagerBrainEvent extends Event implements IVillagerBrainEvent {

    //hack so we can extend Event class
    private final VillagerBrainEventInternal internal;

    public VillagerBrainEvent(Brain<Villager> brain, Villager villager) {
        this.internal = new VillagerBrainEventInternal(brain, villager);
    }

    @Override
    public Villager getVillager() {
        return internal.getVillager();
    }

    @Override
    public Map<MemoryModuleType<?>, Optional<? extends ExpirableValue<?>>> getMemories() {
        return internal.getMemories();
    }

    @Override
    public void addOrReplaceActivity(Activity activity, ImmutableList<? extends Pair<Integer, ? extends BehaviorControl<? super Villager>>> activityPackage) {
        this.internal.addOrReplaceActivity(activity, activityPackage);
    }

    @Override
    public void scheduleActivity(Activity activity, int startTime, int endTime) {
        this.internal.scheduleActivity(activity, startTime, endTime);
    }

    //this might be bad
    @Override
    public void addSensor(SensorType<? extends Sensor<Villager>> newSensor) {
        this.internal.addSensor(newSensor);
    }

    @Override
    public <P extends Pair<Integer, ? extends Behavior<Villager>>> boolean addTaskToActivity(Activity activity, P task) {
        return this.internal.addTaskToActivity(activity, task);
    }

    @ApiStatus.Internal
    public VillagerBrainEventInternal getInternal() {
        return internal;
    }

}

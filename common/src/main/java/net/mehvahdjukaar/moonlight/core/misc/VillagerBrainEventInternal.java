package net.mehvahdjukaar.moonlight.core.misc;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.mojang.datafixers.util.Pair;
import net.mehvahdjukaar.moonlight.core.Moonlight;
import net.mehvahdjukaar.moonlight.core.mixins.accessor.BrainAccessor;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.BehaviorControl;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.schedule.Activity;

import java.util.Map;
import java.util.Set;

public class VillagerBrainEventInternal {

    private final Brain<Villager> brain;
    private final Villager villager;

    public VillagerBrainEventInternal(Brain<Villager> brain, Villager villager) {
        this.brain = brain;
        this.villager = villager;
    }

    /**
     * If possible do not access the villager brain directly. The whole porpouse of this is to makde adding activities work better
     * between mods without modifying the brain directly. Use the methods below
     *
     * @return villager entity
     */
    public Villager getVillager() {
        return villager;
    }

    /**
     * add an activity to the brain.
     * However this isn't recommended since it doesn't completely clear its previous requirements from the requirements map. This might not be an issue tho
     * Try to use addTaskToActivity instead if you just want to add a task to an existing activity without completely overriding it
     *
     * @param activity        the identifier of the activity
     * @param activityPackage the play package itself that will be executed
     */
    public void addOrReplaceActivity(Activity activity, ImmutableList<? extends Pair<Integer, ? extends BehaviorControl<? super Villager>>> activityPackage) {
        this.brain.addActivity(activity, activityPackage, Set.of(), Set.of());
    }

    //this might be bad

    /**
     * Adds a sensor to the villager
     *
     * @param newSensor sensor to be added
     */
    public void addSensor(SensorType<? extends Sensor<Villager>> newSensor) {

        try {
            Map<SensorType<? extends Sensor<? super Villager>>, Sensor<? super Villager>> sensors =
                    ((BrainAccessor<Villager>) brain).getSensors();

            var sensorInstance = newSensor.create();
            sensors.put(newSensor, sensorInstance);

            for (MemoryModuleType<?> memoryModuleType : sensorInstance.requires()) {
                ((BrainAccessor<Villager>) brain).invokeRegisterMemory(memoryModuleType);
            }
        } catch (Exception e) {
            Moonlight.LOGGER.warn("failed to register pumpkin sensor type for villagers: {}", String.valueOf(e));
        }
    }


    /**
     * Used to add a single task to an existing activity. Useful so you can add to existing activities without overriding or having to override the entire activity.
     *
     * @param activity activity you want to add a task to
     * @param task     task to add with its priority
     * @return if successfull
     */
    public <P extends Pair<Integer, ? extends BehaviorControl<? super Villager>>> boolean addTaskToActivity(Activity activity, P task) {

        try {
            Map<Integer, Map<Activity, Set<BehaviorControl<? super Villager>>>> map =
                    ((BrainAccessor<Villager>) brain).getAvailableBehaviorsByPriority();

            var tasksWithSamePriority = map.computeIfAbsent(task.getFirst(), (m) -> Maps.newHashMap());

            var activityTaskSet = tasksWithSamePriority.computeIfAbsent(activity, (a) -> Sets.newLinkedHashSet());

            activityTaskSet.add(task.getSecond());

            return true;

        } catch (Exception e) {
            Moonlight.LOGGER.warn("failed to add task for activity {} for villagers: {}", activity, e);
        }
        return false;
    }

}

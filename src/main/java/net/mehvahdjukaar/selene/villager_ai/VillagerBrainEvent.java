package net.mehvahdjukaar.selene.villager_ai;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Pair;
import net.mehvahdjukaar.selene.Selene;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.ExpirableValue;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.schedule.Activity;
import net.minecraft.world.entity.schedule.Schedule;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class VillagerBrainEvent<T extends AbstractVillager> extends Event {

    protected final List<Pair<Activity, Integer>> scheduleBuilder = new ArrayList<>();

    private final Brain<T> brain;
    private final T villager;

    /**
     * used to add activities, memories, sensor types and modify schedules in a compatible way
     * Main feature is easily adding scheduled activities without overriding the whole schedule and adding sensor types
     */
    public VillagerBrainEvent(Brain<T> brain, T villager){
        this.brain = brain;
        this.villager = villager;

        this.setBaseSchedule(brain);
    }

    /**
     * If possible do not access the villager brain directly. The whole porpouse of this is to makde adding activities work better
     * between mods without modifying the brain directly. Use the methods below
     * @return villager entity
     */
    public T getVillager() {
        return villager;
    }

    /**
     * access the brain memories to add new ones or remove existing ones
     * @return brain memories
     */
    public Map<MemoryModuleType<?>, Optional<? extends ExpirableValue<?>>> getMemories(){
        return brain.getMemories();
    }

    /**
     * add a new activity to the brain. You will need to give the villager a new schedule task to be able to use it
     * @param activity the identifier of the activity
     * @param activityPackage the play package itself that will be executed
     */
    public void addActivity(Activity activity, ImmutableList<? extends Pair<Integer, ? extends Behavior<T>>> activityPackage){
        this.brain.addActivity(activity, activityPackage);
    }

    /**
     * modify the villager schedule to add said activity.
     */
    public void scheduleActivity()


    //this might be bad
    /**
     * Adds a sensor to the villager
     * @param newSensor sensor to be added
     */
    public void addSensor(SensorType<? extends Sensor<T>> newSensor) {
        if (SENSORS == null) SENSORS = ObfuscationReflectionHelper.findField(Brain.class, "f_21844_");

        SENSORS.setAccessible(true);
        try {
            var sensors = (Map<SensorType<? extends Sensor<T>>, Sensor<T>>) SENSORS.get(brain);

            var sensorInstance = newSensor.create();
            sensors.put(newSensor, sensorInstance);

            var memories = this.brain.getMemories();

            for (MemoryModuleType<?> memoryModuleType : sensorInstance.requires()) {
                memories.put(memoryModuleType, Optional.empty());
            }
        } catch (Exception e) {
            Selene.LOGGER.warn("failed to register pumpkin sensor type for villagers: " + e);
        }
    }
    private static Field SENSORS = null;
    private static Field TIMELINE = null;

    private List<Pair<Integer, Activity>> makeDefaultSchedule(T villager){
        //mimics vanilla behavior until I figure out how to decode a compiled schedule
        if(villager.isBaby()){
            return List.of(
                    Pair.of(10, Activity.IDLE),
                    Pair.of(3000, Activity.PLAY),
                    Pair.of(6000, Activity.IDLE),
                    Pair.of(10000, Activity.PLAY),
                    Pair.of(12000, Activity.REST)
            );
        }
        else {
            return List.of(
                    Pair.of(10, Activity.IDLE),
                    Pair.of(2000, Activity.WORK),
                    Pair.of(9000, Activity.MEET),
                    Pair.of(11000, Activity.IDLE),
                    Pair.of(12000, Activity.REST)
            );
        }
    }


}

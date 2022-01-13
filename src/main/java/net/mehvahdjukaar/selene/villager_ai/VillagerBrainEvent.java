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
import net.minecraft.world.entity.schedule.Activity;
import net.minecraft.world.entity.schedule.Schedule;
import net.minecraft.world.entity.schedule.ScheduleBuilder;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;

public class VillagerBrainEvent<T extends AbstractVillager> extends Event {

    private TreeMap<Integer, Activity> scheduleBuilder = null;

    private final Brain<T> brain;
    private final T villager;

    /**
     * used to add activities, memories, sensor types and modify schedules in a compatible way
     * Main feature is easily adding scheduled activities without overriding the whole schedule and adding sensor types
     */
    public VillagerBrainEvent(Brain<T> brain, T villager) {
        this.brain = brain;
        this.villager = villager;
    }

    /**
     * If possible do not access the villager brain directly. The whole porpouse of this is to makde adding activities work better
     * between mods without modifying the brain directly. Use the methods below
     *
     * @return villager entity
     */
    public T getVillager() {
        return villager;
    }

    /**
     * access the brain memories to add new ones or remove existing ones
     *
     * @return brain memories
     */
    public Map<MemoryModuleType<?>, Optional<? extends ExpirableValue<?>>> getMemories() {
        return brain.getMemories();
    }

    /**
     * add a new activity to the brain. You will need to give the villager a new schedule task to be able to use it
     *
     * @param activity        the identifier of the activity
     * @param activityPackage the play package itself that will be executed
     */
    public void addActivity(Activity activity, ImmutableList<? extends Pair<Integer, ? extends Behavior<T>>> activityPackage) {
        this.brain.addActivity(activity, activityPackage);
    }

    /**
     * Adds an activity to the schedule. will override any activity that is in that specified time window
     * Note that subsequent call to this from other mods in later event execution might override your activity if the time window is the same
     * If it's not it might be shortened or cut in two
     *
     * @param activity  actiity to register
     * @param startTime day time at which activity will start
     * @param endTime   day time at which activity will end. can also be less than start time
     */
    public void scheduleActivity(Activity activity, int startTime, int endTime) {

        if (this.scheduleBuilder == null) {
            //make default schedule builder if not initialized
            this.scheduleBuilder = this.makeDefaultSchedule(villager);
        }

        //crappy code incoming lol
        TreeMap<Integer, Activity> newSchedule = new TreeMap<>();

        newSchedule.put(startTime, activity);

        Activity previousActivity = this.scheduleBuilder.lastEntry().getValue();
        for (var e : this.scheduleBuilder.entrySet()) {
            int key = e.getKey();
            if (key < endTime) {
                previousActivity = e.getValue();
            }
            //only adds if in correct time window
            if (startTime < endTime) {
                if (key < startTime || key > endTime) {
                    newSchedule.put(key, e.getValue());
                }
            } else {
                if (key > endTime && key < startTime) {
                    newSchedule.put(key, e.getValue());
                }
            }
        }
        newSchedule.put(endTime, previousActivity);

        this.scheduleBuilder = newSchedule;
    }


    //this might be bad

    /**
     * Adds a sensor to the villager
     *
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

    private TreeMap<Integer, Activity> makeDefaultSchedule(T villager) {
        TreeMap<Integer, Activity> map = new TreeMap<>();
        //mimics vanilla behavior until I figure out how to decode a compiled schedule
        if (villager.isBaby()) {
            map.put(10, Activity.IDLE);
            map.put(3000, Activity.PLAY);
            map.put(6000, Activity.IDLE);
            map.put(10000, Activity.PLAY);
            map.put(12000, Activity.REST);
        } else {

            map.put(10, Activity.IDLE);
            map.put(2000, Activity.WORK);
            map.put(9000, Activity.MEET);
            map.put(11000, Activity.IDLE);
            map.put(12000, Activity.REST);
        }
        return map;
    }

    protected Schedule buildFinalizedSchedule() {
        ScheduleBuilder builder = new ScheduleBuilder(VillagerAIManager.CUSTOM_VILLAGER_SCHEDULE.get());
        for (var e : this.scheduleBuilder.entrySet()) {
            builder.changeActivityAt(e.getKey(), e.getValue());
        }
        return builder.build();
    }

    protected boolean hasCustomSchedule() {
        return this.scheduleBuilder != null;
    }

}

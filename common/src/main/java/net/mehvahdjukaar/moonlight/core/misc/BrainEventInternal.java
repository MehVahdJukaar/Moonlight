package net.mehvahdjukaar.moonlight.core.misc;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.mojang.datafixers.util.Pair;
import net.mehvahdjukaar.moonlight.api.util.VillagerAIManager;
import net.mehvahdjukaar.moonlight.core.Moonlight;
import net.mehvahdjukaar.moonlight.core.mixins.accessor.BrainAccessor;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.ExpirableValue;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.schedule.Activity;
import net.minecraft.world.entity.schedule.Schedule;
import net.minecraft.world.entity.schedule.ScheduleBuilder;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;

public class BrainEventInternal {

    private TreeMap<Integer, Activity> scheduleBuilder = null;

    private final Brain<Villager> brain;
    private final Villager villager;

    /**
     * used to add activities, memories, sensor types and modify schedules in a compatible way
     * Main feature is easily adding scheduled activities without overriding the whole schedule and adding sensor types
     */
    public BrainEventInternal(Brain<Villager> brain, Villager villager) {
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
     * access the brain memories to add new ones or remove existing ones
     * Important: to addListener new memory types use the static method in VillagerAIManager otherwise they will not be able to be saved if you add them here manually
     *
     * @return brain memories
     */
    public Map<MemoryModuleType<?>, Optional<? extends ExpirableValue<?>>> getMemories() {
        return brain.getMemories();
    }

    /**
     * add an activity to the brain.
     * However this isn't recommended since it doesn't completely clear its previous requirements from the requirements map. This might not be an issue tho
     * Try to use addTaskToActivity instead if you just want to add a task to an existing activity without completely overriding it
     *
     * @param activity        the identifier of the activity
     * @param activityPackage the play package itself that will be executed
     */
    public void addOrReplaceActivity(Activity activity, ImmutableList<? extends Pair<Integer, ? extends Behavior<? super Villager>>> activityPackage) {
        this.brain.addActivity(activity, activityPackage);
    }


    /**
     * Adds an activity to the schedule. will override any activity that is in that specified time window
     * Note that subsequent call to this from other mods in later event execution might override your activity if the time window is the same
     * If it's not it might be shortened or cut in two
     *
     * @param activity  activity to addListener
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
    public void addSensor(SensorType<? extends Sensor<Villager>> newSensor) {

        try {
            Map<SensorType<? extends Sensor<Villager>>, Sensor<Villager>> sensors = ((BrainAccessor) brain).getSensors();

            var sensorInstance = newSensor.create();
            sensors.put(newSensor, sensorInstance);

            var memories = this.brain.getMemories();

            for (MemoryModuleType<?> memoryModuleType : sensorInstance.requires()) {
                memories.put(memoryModuleType, Optional.empty());
            }
        } catch (Exception e) {
            Moonlight.LOGGER.warn("failed to addListener pumpkin sensor type for villagers: " + e);
        }
    }


    /**
     * Used to add a single task to an existing activity. Useful so you can add to existing activities without overriding or having to override the entire activity.
     * Alternatively you can define your own activity and add it to the villager schedule using scheduleActivity
     *
     * @param activity activity you want to add a task to
     * @param task     task to add with its priority
     * @return if successfull
     */
    public <P extends Pair<Integer, ? extends Behavior<Villager>>> boolean addTaskToActivity(Activity activity, P task) {

        try {
            Map<Integer, Map<Activity, Set<Behavior<Villager>>>> map =
                    (Map<Integer, Map<Activity, Set<Behavior<Villager>>>>) ((BrainAccessor) brain).getAvailableBehaviorsByPriority();

            var tasksWithSamePriority = map.computeIfAbsent(task.getFirst(), (m) -> Maps.newHashMap());

            var activityTaskSet = tasksWithSamePriority.computeIfAbsent(activity, (a) -> Sets.newLinkedHashSet());

            activityTaskSet.add(task.getSecond());

            return true;

        } catch (Exception e) {
            Moonlight.LOGGER.warn("failed to add task for activity {} for villagers: {}", activity, e);
        }
        return false;
    }


    private TreeMap<Integer, Activity> makeDefaultSchedule(Villager villager) {
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

    public Schedule buildFinalizedSchedule() {
        ScheduleBuilder builder = new ScheduleBuilder(VillagerAIManager.CUSTOM_VILLAGER_SCHEDULE.get());
        for (var e : this.scheduleBuilder.entrySet()) {
            builder.changeActivityAt(e.getKey(), e.getValue());
        }
        return builder.build();
    }

    public boolean hasCustomSchedule() {
        return this.scheduleBuilder != null;
    }

}

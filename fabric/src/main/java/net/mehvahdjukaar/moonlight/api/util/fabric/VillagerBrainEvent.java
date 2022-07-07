package net.mehvahdjukaar.moonlight.api.util.fabric;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Pair;
import net.mehvahdjukaar.moonlight.api.util.IVillagerBrainEvent;
import net.mehvahdjukaar.moonlight.core.misc.BrainEventInternal;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.ExpirableValue;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.schedule.Activity;
import net.minecraft.world.entity.schedule.Schedule;

import java.util.Map;
import java.util.Optional;

public class VillagerBrainEvent implements IVillagerBrainEvent {

    //hack so we can extend Event class
    private final BrainEventInternal internal;

    /**
     * used to add activities, memories, sensor types and modify schedules in a compatible way
     * Main feature is easily adding scheduled activities without overriding the whole schedule and adding sensor types
     */
    public VillagerBrainEvent(Brain<Villager> brain, Villager villager) {
        this.internal = new BrainEventInternal(brain, villager);
    }

    /**
     * If possible do not access the villager brain directly. The whole porpouse of this is to makde adding activities work better
     * between mods without modifying the brain directly. Use the methods below
     *
     * @return villager entity
     */
    public Villager getVillager() {
        return internal.getVillager();
    }

    /**
     * access the brain memories to add new ones or remove existing ones
     * Important: to addListener new memory types use the static method in VillagerAIManager otherwise they will not be able to be saved if you add them here manually
     *
     * @return brain memories
     */
    public Map<MemoryModuleType<?>, Optional<? extends ExpirableValue<?>>> getMemories() {
        return internal.getMemories();
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
        this.internal.addOrReplaceActivity(activity, activityPackage);
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
        this.internal.scheduleActivity(activity, startTime, endTime);
    }

    //this might be bad

    /**
     * Adds a sensor to the villager
     *
     * @param newSensor sensor to be added
     */
    public void addSensor(SensorType<? extends Sensor<Villager>> newSensor) {
        this.internal.addSensor(newSensor);
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
        return this.internal.addTaskToActivity(activity,task);
    }


    protected Schedule buildFinalizedSchedule() {
        return this.internal.buildFinalizedSchedule();
    }

    protected boolean hasCustomSchedule() {
        return this.internal.hasCustomSchedule();
    }

}

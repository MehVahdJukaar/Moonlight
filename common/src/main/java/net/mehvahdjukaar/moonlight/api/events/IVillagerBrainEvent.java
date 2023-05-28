package net.mehvahdjukaar.moonlight.api.events;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Pair;
import net.mehvahdjukaar.moonlight.core.misc.VillagerBrainEventInternal;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.behavior.BehaviorControl;
import net.minecraft.world.entity.ai.memory.ExpirableValue;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.schedule.Activity;

import java.util.Map;
import java.util.Optional;

public interface IVillagerBrainEvent extends SimpleEvent {

    /**
     * If possible do not access the villager brain directly. The whole porpouse of this is to makde adding activities work better
     * between mods without modifying the brain directly. Use the methods below
     *
     * @return villager entity
     */
    Villager getVillager();

    /**
     * access the brain memories to add new ones or remove existing ones
     * Important: to register a new memory types use the static method in VillagerAIManager otherwise they will not be able to be saved if you add them here manually
     *
     * @return brain memories
     */
    Map<MemoryModuleType<?>, Optional<? extends ExpirableValue<?>>> getMemories();

    /**
     * add an activity to the brain.
     * However this isn't recommended since it doesn't completely clear its previous requirements from the requirements map. This might not be an issue tho
     * Try to use addTaskToActivity instead if you just want to add a task to an existing activity without completely overriding it
     *
     * @param activity        the identifier of the activity
     * @param activityPackage the play package itself that will be executed
     */
    void addOrReplaceActivity(Activity activity, ImmutableList<? extends Pair<Integer, ? extends BehaviorControl<? super Villager>>> activityPackage);

    /**
     * Adds an activity to the schedule. will override any activity that is in that specified time window
     * Note that subsequent call to this from other mods in later event execution might override your activity if the time window is the same
     * If it's not it might be shortened or cut in two
     *
     * @param activity  activity to register
     * @param startTime day time at which activity will start
     * @param endTime   day time at which activity will end. can also be less than start time
     */
    void scheduleActivity(Activity activity, int startTime, int endTime);

    /**
     * Adds a sensor to the villager
     *
     * @param newSensor sensor to be added
     */
    void addSensor(SensorType<? extends Sensor<Villager>> newSensor);

    /**
     * Used to add a single task to an existing activity. Useful so you can add to existing activities without overriding or having to override the entire activity.
     * Alternatively you can define your own activity and add it to the villager schedule using scheduleActivity
     *
     * @param activity activity you want to add a task to
     * @param task     task to add with its priority
     * @return if successfull
     */
    <P extends Pair<Integer, ? extends Behavior<Villager>>> boolean addTaskToActivity(Activity activity, P task);

    //do not call
    VillagerBrainEventInternal getInternal();

}


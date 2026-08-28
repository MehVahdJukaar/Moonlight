package net.mehvahdjukaar.moonlight.api.events;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Pair;
import net.mehvahdjukaar.moonlight.core.misc.VillagerBrainEventInternal;
import net.minecraft.world.entity.ai.behavior.BehaviorControl;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.schedule.Activity;
import org.jetbrains.annotations.ApiStatus;

public interface IVillagerBrainEvent extends SimpleEvent {

    /**
     * If possible, do not access the villager brain directly.
     * The whole purpose of this is to make adding activities work better
     * between mods without modifying the brain directly.
     * Use the methods below
     *
     * @return villager entity
     */
    Villager getVillager();

    /**
     * Add an activity to the brain.
     * However, this isn't recommended as it doesn't completely clear its previous requirements from the requirement map.
     * This might not be an issue tho
     * Try to use addTaskToActivity instead if you just want to add a task to an existing activity without completely overriding it
     *
     * @param activity        the identifier of the activity
     * @param activityPackage the play package itself that will be executed
     */
    void addOrReplaceActivity(Activity activity, ImmutableList<? extends Pair<Integer, ? extends BehaviorControl<? super Villager>>> activityPackage);

    /**
     * Adds a sensor to the villager
     *
     * @param newSensor sensor to be added
     */
    void addSensor(SensorType<? extends Sensor<Villager>> newSensor);

    /**
     * Used to add a single task to an existing activity.
     * Useful so you can add to existing activities without overriding or having to override the entire activity.
     *
     * @param activity activity you want to add a task to
     * @param task     task to add with its priority
     * @return if successfull
     */
    <P extends Pair<Integer, ? extends BehaviorControl<? super Villager>>> boolean addTaskToActivity(Activity activity, P task);

    //do not call
    @ApiStatus.Internal
    VillagerBrainEventInternal getInternal();

}


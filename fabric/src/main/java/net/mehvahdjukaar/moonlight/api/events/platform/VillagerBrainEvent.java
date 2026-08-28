package net.mehvahdjukaar.moonlight.api.events.platform;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Pair;
import net.mehvahdjukaar.moonlight.api.events.IVillagerBrainEvent;
import net.mehvahdjukaar.moonlight.core.misc.VillagerBrainEventInternal;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.BehaviorControl;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.schedule.Activity;
import org.jetbrains.annotations.ApiStatus;

public class VillagerBrainEvent implements IVillagerBrainEvent {

    //hack so we can extend Event class
    private final VillagerBrainEventInternal internal;

    /**
     * used to add activities and sensor types in a compatible way
     */
    public VillagerBrainEvent(Brain<Villager> brain, Villager villager) {
        this.internal = new VillagerBrainEventInternal(brain, villager);
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
     * add an activity to the brain.
     * However this isn't recommended since it doesn't completely clear its previous requirements from the requirements map. This might not be an issue tho
     * Try to use addTaskToActivity instead if you just want to add a task to an existing activity without completely overriding it
     *
     * @param activity        the identifier of the activity
     * @param activityPackage the play package itself that will be executed
     */
    public void addOrReplaceActivity(Activity activity, ImmutableList<? extends Pair<Integer, ? extends BehaviorControl<? super Villager>>> activityPackage) {
        this.internal.addOrReplaceActivity(activity, activityPackage);
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
     *
     * @param activity activity you want to add a task to
     * @param task     task to add with its priority
     * @return if successfull
     */
    public <P extends Pair<Integer, ? extends BehaviorControl<? super Villager>>> boolean addTaskToActivity(Activity activity, P task) {
        return this.internal.addTaskToActivity(activity, task);
    }

    @ApiStatus.Internal
    public VillagerBrainEventInternal getInternal() {
        return internal;
    }
}

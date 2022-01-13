package net.mehvahdjukaar.selene.villager_ai;

import net.mehvahdjukaar.selene.Selene;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.schedule.Schedule;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class VillagerAIManager {
    public static final DeferredRegister<Schedule> SCHEDULES = DeferredRegister.create(ForgeRegistries.SCHEDULES, Selene.MOD_ID);

    protected static final RegistryObject<Schedule> CUSTOM_VILLAGER_SCHEDULE =
            SCHEDULES.register("villager_baby_halloween", Schedule::new);

    //dont even have priority lol
    private static final List<Consumer<VillagerBrainEvent<?>>> LISTENERS = new ArrayList<>();


    /**
     * registers a listener that will be called when villager brain is initialized. Used to add schedules, activities, sensor types and memories
     * @param callback Listener to VillagerBrainEvent
     */
    public static void addVillagerAiEventListener(Consumer<VillagerBrainEvent<?>> callback) {
        LISTENERS.add(callback);
    }

    //called by mixin
    public static void onRegisterBrainGoals(Brain<Villager> brain, AbstractVillager villager) {
        if(villager instanceof Villager v) {
            var event = new VillagerBrainEvent<>(brain, v);

            for (var l : LISTENERS) {
                l.accept(event);
            }
            //dont waste time if it doesn't have a custom schedule
            if (event.hasCustomSchedule()) {
                //finalize schedule
                brain.setSchedule(event.buildFinalizedSchedule());
                brain.updateActivityFromSchedule(villager.level.getDayTime(), villager.level.getGameTime());
            }
        }
    }


    //just loads up the class to register the schedule
    public static void init() {
    }
}

package net.mehvahdjukaar.moonlight.core.misc;

import dev.architectury.injectables.annotations.ExpectPlatform;
import net.mehvahdjukaar.moonlight.api.platform.event.EventHelper;
import net.mehvahdjukaar.moonlight.api.platform.registry.RegHelper;
import net.mehvahdjukaar.moonlight.core.Moonlight;
import net.minecraft.core.Registry;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.schedule.Schedule;

import java.util.function.Supplier;

public class VillagerAIInternal {


    public static void init() {
    }

    //schedule to which all the tasks are registered to
    public static final Supplier<Schedule> CUSTOM_VILLAGER_SCHEDULE =
            RegHelper.register(Moonlight.res("custom_villager_schedule"), Schedule::new, Registry.SCHEDULE);


    //called by mixin. Do not call
    public static void onRegisterBrainGoals(Brain<Villager> brain, AbstractVillager villager) {
        if (villager instanceof Villager v) {
            var event = createEvent(brain, v);
            EventHelper.postEvent(event, IVillagerBrainEvent.class);
            //don't waste time if it doesn't have a custom schedule
            var internal = event.getInternal();
            if (internal.hasCustomSchedule()) {
                //finalize schedule
                brain.setSchedule(internal.buildFinalizedSchedule());
                brain.updateActivityFromSchedule(villager.level.getDayTime(),villager.level.getGameTime());
            }
        }
    }

    @ExpectPlatform
    public static IVillagerBrainEvent createEvent(Brain<Villager> brain, Villager villager){
        throw new AssertionError();
    }
}

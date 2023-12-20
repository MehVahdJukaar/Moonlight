package net.mehvahdjukaar.moonlight.core.misc;

import dev.architectury.injectables.annotations.ExpectPlatform;
import net.mehvahdjukaar.moonlight.api.events.IVillagerBrainEvent;
import net.mehvahdjukaar.moonlight.api.events.MoonlightEventsHelper;
import net.mehvahdjukaar.moonlight.api.platform.RegHelper;
import net.mehvahdjukaar.moonlight.core.Moonlight;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.schedule.Schedule;

import java.util.function.Supplier;

public class VillagerAIInternal {


    public static void init() {
    }

    //called by mixin. Do not call
    public static void onRegisterBrainGoals(Brain<Villager> brain, AbstractVillager villager) {
        if (villager instanceof Villager v) {
            var event = createEvent(brain, v);
            MoonlightEventsHelper.postEvent(event, IVillagerBrainEvent.class);
            //don't waste time if it doesn't have a custom schedule
            var internal = event.getInternal();
            if (internal.hasCustomSchedule()) {
                //finalize schedule
                brain.setSchedule(internal.buildFinalizedSchedule());
                brain.updateActivityFromSchedule(villager.level().getDayTime(),villager.level().getGameTime());
            }
        }
    }

    @ExpectPlatform
    public static IVillagerBrainEvent createEvent(Brain<Villager> brain, Villager villager){
        throw new AssertionError();
    }
}

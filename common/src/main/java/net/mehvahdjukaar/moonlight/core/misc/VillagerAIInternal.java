package net.mehvahdjukaar.moonlight.core.misc;

import net.mehvahdjukaar.candlelight.api.PlatformImpl;
import net.mehvahdjukaar.moonlight.api.events.IVillagerBrainEvent;
import net.mehvahdjukaar.moonlight.api.events.MoonlightEventsHelper;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.npc.Villager;

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

    @PlatformImpl
    public static IVillagerBrainEvent createEvent(Brain<Villager> brain, Villager villager){
        throw new AssertionError();
    }
}

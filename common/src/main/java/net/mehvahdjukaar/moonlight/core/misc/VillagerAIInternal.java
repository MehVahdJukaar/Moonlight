package net.mehvahdjukaar.moonlight.core.misc;

import net.mehvahdjukaar.candlelight.api.PlatformImpl;
import net.mehvahdjukaar.moonlight.api.events.IVillagerBrainEvent;
import net.mehvahdjukaar.moonlight.api.events.MoonlightEventsHelper;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.npc.villager.AbstractVillager;
import net.minecraft.world.entity.npc.villager.Villager;

public class VillagerAIInternal {


    public static void init() {
    }

    //called by mixin. Do not call
    public static void onRegisterBrainGoals(Brain<Villager> brain, AbstractVillager villager) {
        if (villager instanceof Villager v) {
            MoonlightEventsHelper.postEvent(createEvent(brain, v), IVillagerBrainEvent.class);
        }
    }

    @PlatformImpl
    public static IVillagerBrainEvent createEvent(Brain<Villager> brain, Villager villager){
        throw new AssertionError();
    }
}

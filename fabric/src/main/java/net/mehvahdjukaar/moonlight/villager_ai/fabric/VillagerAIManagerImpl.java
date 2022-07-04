package net.mehvahdjukaar.moonlight.villager_ai.fabric;

import net.mehvahdjukaar.moonlight.platform.event.EventHelper;
import net.mehvahdjukaar.moonlight.villager_ai.IVillagerBrainEvent;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.npc.Villager;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class VillagerAIManagerImpl {


    @org.jetbrains.annotations.ApiStatus.Internal
    public static void onRegisterBrainGoals(Brain<Villager> brain, AbstractVillager villager) {
        if (villager instanceof Villager v) {
            var event = new VillagerBrainEvent(brain, v);
            EventHelper.postEvent(event, IVillagerBrainEvent.class);
            //dont waste time if it doesn't have a custom schedule
            if (event.hasCustomSchedule()) {
                //finalize schedule
                brain.setSchedule(event.buildFinalizedSchedule());
                brain.updateActivityFromSchedule(villager.level.getDayTime(), villager.level.getGameTime());
            }
        }
    }
}

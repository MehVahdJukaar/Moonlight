package net.mehvahdjukaar.moonlight.villager_ai.forge;

import net.mehvahdjukaar.moonlight.resources.SimpleTagBuilder;
import net.mehvahdjukaar.moonlight.villager_ai.IVillagerBrainEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackCompatibility;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraft.tags.TagBuilder;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.npc.Villager;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.AddPackFindersEvent;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class VillagerAIManagerImpl {
    @org.jetbrains.annotations.ApiStatus.Internal
    public static void onRegisterBrainGoals(Brain<Villager> brain, AbstractVillager villager) {
        if (villager instanceof Villager v) {
            var event = new VillagerBrainEvent(brain, v);
            MinecraftForge.EVENT_BUS.post(event);
            //dont waste time if it doesn't have a custom schedule
            if (event.hasCustomSchedule()) {
                //finalize schedule
                brain.setSchedule(event.buildFinalizedSchedule());
                brain.updateActivityFromSchedule(villager.level.getDayTime(), villager.level.getGameTime());
            }
        }
    }

    public static void addListener(Consumer<IVillagerBrainEvent> eventConsumer) {
        MinecraftForge.EVENT_BUS.addListener(e -> eventConsumer.accept((VillagerBrainEvent) e));
    }

}


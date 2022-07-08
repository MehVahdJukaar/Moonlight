package net.mehvahdjukaar.moonlight.core.misc.forge;

import net.mehvahdjukaar.moonlight.api.events.IVillagerBrainEvent;
import net.mehvahdjukaar.moonlight.api.util.forge.VillagerBrainEvent;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.npc.Villager;

public class VillagerAIInternalImpl {

    public static IVillagerBrainEvent createEvent(Brain<Villager> brain, Villager villager) {
        return new VillagerBrainEvent(brain, villager);
    }

}


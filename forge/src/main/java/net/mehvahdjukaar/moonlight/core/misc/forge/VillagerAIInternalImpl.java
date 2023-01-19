package net.mehvahdjukaar.moonlight.core.misc.forge;

import net.mehvahdjukaar.moonlight.api.events.IVillagerBrainEvent;
import net.mehvahdjukaar.moonlight.api.events.forge.VillagerBrainEvent;
import net.mehvahdjukaar.moonlight.api.misc.RegSupplier;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.Sheep;
import net.minecraft.world.entity.npc.Villager;

public class VillagerAIInternalImpl {

    public static IVillagerBrainEvent createEvent(Brain<Villager> brain, Villager villager) {
        return new VillagerBrainEvent(brain, villager);
    }
}


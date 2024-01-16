package net.mehvahdjukaar.moonlight.core.misc.fabric;

import net.mehvahdjukaar.moonlight.api.events.IVillagerBrainEvent;
import net.mehvahdjukaar.moonlight.api.events.fabric.VillagerBrainEvent;
import net.mehvahdjukaar.moonlight.api.fluids.ModFlowingFluid;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.npc.Villager;

public class VillagerAIInternalImpl {

    public static IVillagerBrainEvent createEvent(Brain<Villager> brain, Villager villager) {
        return new VillagerBrainEvent(brain, villager);
    }
}

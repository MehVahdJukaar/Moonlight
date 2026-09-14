package net.mehvahdjukaar.moonlight.core.misc;

import net.mehvahdjukaar.candlelight.api.PlatformImpl;
import net.mehvahdjukaar.moonlight.api.events.IVillagerBrainEvent;
import net.mehvahdjukaar.moonlight.api.events.MoonlightEventsHelper;
import net.mehvahdjukaar.moonlight.core.mixins.accessor.BrainAccessor;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryMap;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.npc.villager.AbstractVillager;
import net.minecraft.world.entity.npc.villager.Villager;

public class VillagerAIInternal {


    public static void init() {
    }

    //called by mixin. Do not call
    public static void onBrainMade(Brain<Villager> brain, AbstractVillager villager, Brain.Packed packed) {
        if (villager instanceof Villager v) {
            MoonlightEventsHelper.postEvent(createEvent(brain, v), IVillagerBrainEvent.class);
            //slots registered by listeners werent there when the brain loaded packed, so they are still empty
            for (MemoryMap.Value<?> memory : packed.memories()) {
                if (brain.checkMemory(memory.type(), MemoryStatus.VALUE_ABSENT)) {
                    ((BrainAccessor<Villager>) brain).invokeSetMemoryInternal(memory);
                }
            }
        }
    }

    @PlatformImpl
    public static IVillagerBrainEvent createEvent(Brain<Villager> brain, Villager villager){
        throw new AssertionError();
    }
}

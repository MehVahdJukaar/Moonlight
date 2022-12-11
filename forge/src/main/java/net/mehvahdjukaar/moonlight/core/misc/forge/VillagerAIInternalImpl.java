package net.mehvahdjukaar.moonlight.core.misc.forge;

import net.mehvahdjukaar.moonlight.api.events.IVillagerBrainEvent;
import net.mehvahdjukaar.moonlight.api.events.forge.VillagerBrainEvent;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.item.CreativeModeTabs;

public class VillagerAIInternalImpl {

    public static IVillagerBrainEvent createEvent(Brain<Villager> brain, Villager villager) {
        return new VillagerBrainEvent(brain, villager);
    }

}


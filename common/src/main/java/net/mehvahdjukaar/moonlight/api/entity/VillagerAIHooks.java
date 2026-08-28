package net.mehvahdjukaar.moonlight.api.entity;

import net.mehvahdjukaar.moonlight.api.events.IVillagerBrainEvent;
import net.mehvahdjukaar.moonlight.api.events.MoonlightEventsHelper;

import java.util.function.Consumer;

public class VillagerAIHooks {

    /**
     * Register an event listener for the villager brain event.
     * On forge Use the subscribe event annotation instead
     */
    public static void addBrainModification(Consumer<IVillagerBrainEvent> eventConsumer){
        MoonlightEventsHelper.addListener(eventConsumer, IVillagerBrainEvent.class);
    }

}

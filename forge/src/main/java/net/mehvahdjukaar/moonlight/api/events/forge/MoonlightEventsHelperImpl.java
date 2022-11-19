package net.mehvahdjukaar.moonlight.api.events.forge;

import net.mehvahdjukaar.moonlight.api.events.IFireConsumeBlockEvent;
import net.mehvahdjukaar.moonlight.api.events.ILightningStruckBlockEvent;
import net.mehvahdjukaar.moonlight.api.events.IVillagerBrainEvent;
import net.mehvahdjukaar.moonlight.api.events.SimpleEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.Event;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class MoonlightEventsHelperImpl {

    private static final Map<Class<? extends SimpleEvent>, List<Consumer<? extends SimpleEvent>>> LISTENERS = new IdentityHashMap<>();

    @SuppressWarnings("unchecked")
    public static <T extends SimpleEvent> void addListener(Consumer<T> listener, Class<T> eventClass) {
        //hacky
        if (eventClass == IVillagerBrainEvent.class) {
            Consumer<VillagerBrainEvent> eventConsumer = e -> listener.accept((T) e);
            MinecraftForge.EVENT_BUS.addListener(eventConsumer);
        } else if (eventClass == IFireConsumeBlockEvent.class) {
            Consumer<FireConsumeBlockEvent> eventConsumer = e -> listener.accept((T) e);
            MinecraftForge.EVENT_BUS.addListener(eventConsumer);
        } else if (eventClass == ILightningStruckBlockEvent.class) {
            Consumer<LightningStruckBlockEvent> eventConsumer = e -> listener.accept((T) e);
            MinecraftForge.EVENT_BUS.addListener(eventConsumer);
        } else {
            //other 2 events dont work on forge bus for some reason... Randomly too
            LISTENERS.computeIfAbsent(eventClass, ev -> new ArrayList<>()).add(listener);
        }
    }

    public static <T extends SimpleEvent> void postEvent(T event, Class<T> eventClass) {
        if (event instanceof Event e) {
            MinecraftForge.EVENT_BUS.post(e);
        } else {
            var consumers = LISTENERS.get(eventClass);
            if (consumers != null) {
                ((List<Consumer<T>>) (Object) consumers).forEach(e -> e.accept(event));
            }
        }
    }

}

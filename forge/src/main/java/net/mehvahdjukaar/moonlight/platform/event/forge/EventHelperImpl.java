package net.mehvahdjukaar.moonlight.platform.event.forge;

import net.mehvahdjukaar.moonlight.platform.event.SimpleEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.Event;

import java.util.function.Consumer;

public class EventHelperImpl {

    @SuppressWarnings("unchecked")
    public static <T extends SimpleEvent> void addListener(Consumer<T> listener, Class<T> eventClass) {
        Consumer<Event> c = (Consumer<Event>) (Object) listener;
        MinecraftForge.EVENT_BUS.addListener(c);
    }

    public static <T extends SimpleEvent> void postEvent(T event, Class<T> eventClass) {
        MinecraftForge.EVENT_BUS.post((Event) event);
    }


}

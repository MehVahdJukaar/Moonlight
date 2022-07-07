package net.mehvahdjukaar.moonlight.api.platform.event.forge;

import net.mehvahdjukaar.moonlight.api.platform.event.SimpleEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.Event;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class EventHelperImpl {

    private static final Map<Class<? extends SimpleEvent>, List<Consumer<? extends SimpleEvent>>> LISTENERS = new HashMap<>();

    @SuppressWarnings("unchecked")
    public static <T extends SimpleEvent> void addListener(Consumer<T> listener, Class<T> eventClass) {
        try {
            Consumer<Event> c = (Consumer<Event>) (Object) listener;
            MinecraftForge.EVENT_BUS.addListener(c);
        }catch (Exception e){
            LISTENERS.computeIfAbsent(eventClass, ev -> new ArrayList<>()).add(listener);
        }
    }

    public static <T extends SimpleEvent> void postEvent(T event, Class<T> eventClass) {
        if(event instanceof Event) {
            MinecraftForge.EVENT_BUS.post((Event) event);
        }else{
            var L = LISTENERS.get(eventClass);
            if (L != null) {
                ((List<Consumer<T>>) (Object) L).forEach(e -> e.accept(event));
            }
        }
    }


}

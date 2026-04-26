package net.mehvahdjukaar.moonlight.api.events;

import net.mehvahdjukaar.candlelight.api.PlatformImpl;

import java.util.function.Consumer;

/**
 * Helps fire the few events that this library defines. needed to work on both loaders
 */
public class MoonlightEventsHelper {

    @PlatformImpl
    public static <T extends SimpleEvent> void addListener(Consumer<T> listener, Class<T> eventClass){
        throw new AssertionError();
    }

    @PlatformImpl
    public static <T extends SimpleEvent> void postEvent(T event, Class<T> eventClass){
        throw new AssertionError();
    }


}

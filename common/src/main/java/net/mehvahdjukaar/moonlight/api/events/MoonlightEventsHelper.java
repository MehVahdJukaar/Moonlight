package net.mehvahdjukaar.moonlight.api.events;

import dev.architectury.injectables.annotations.ExpectPlatform;

import java.util.function.Consumer;

/**
 * Helps fire the few events that this library defines. needed to work on both loaders
 */
public class MoonlightEventsHelper {

    @ExpectPlatform
    public static <T extends SimpleEvent> void addListener(Consumer<T> listener, Class<T> eventClass){
        throw new AssertionError();
    }

    @ExpectPlatform
    public static <T extends SimpleEvent> void postEvent(T event, Class<T> eventClass){
        throw new AssertionError();
    }


}

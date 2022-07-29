package net.mehvahdjukaar.moonlight.api.events.fabric;

import net.mehvahdjukaar.moonlight.api.events.SimpleEvent;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.phys.HitResult;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class MoonlightEventsHelperImpl {

    private static final Map<Class<? extends SimpleEvent>, List<Consumer<? extends SimpleEvent>>> LISTENERS = new HashMap<>();


    public static <T extends SimpleEvent> void addListener(Consumer<T> listener, Class<T> eventClass) {
        LISTENERS.computeIfAbsent(eventClass, e -> new ArrayList<>()).add(listener);
    }

    @SuppressWarnings("unchecked")
    public static <T extends SimpleEvent> void postEvent(T event, Class<T> eventClass) {
        var L = LISTENERS.get(eventClass);
        if (L != null) {
            ((List<Consumer<T>>) (Object) L).forEach(e -> e.accept(event));
        }
    }

}

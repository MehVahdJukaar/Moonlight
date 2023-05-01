package net.mehvahdjukaar.moonlight.api.events.fabric;

import net.fabricmc.fabric.mixin.resource.loader.client.KeyedResourceReloadListenerClientMixin;
import net.mehvahdjukaar.moonlight.api.events.SimpleEvent;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.phys.HitResult;

import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.function.Consumer;

public class MoonlightEventsHelperImpl {

    private static final Map<Class<? extends SimpleEvent>, Queue<Consumer<? extends SimpleEvent>>> LISTENERS = new ConcurrentHashMap<>();


    public static <T extends SimpleEvent> void addListener(Consumer<T> listener, Class<T> eventClass) {
        LISTENERS.computeIfAbsent(eventClass, e -> new ConcurrentLinkedDeque<>()).add(listener);
    }

    @SuppressWarnings("unchecked")
    public static <T extends SimpleEvent> void postEvent(T event, Class<T> eventClass) {
        var consumers = LISTENERS.get(eventClass);
        if (consumers != null) {
            ((Queue<Consumer<T>>) (Object) consumers).forEach(e -> e.accept(event));
        }
    }

}

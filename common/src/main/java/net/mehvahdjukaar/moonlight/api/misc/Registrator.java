package net.mehvahdjukaar.moonlight.api.misc;

import net.minecraft.resources.ResourceLocation;

import java.util.function.Supplier;

public interface Registrator<T> {

    void register(ResourceLocation name, T instance);

    default void register(String name, T instance) {
        register(new ResourceLocation(name), instance);
    }

}

package net.mehvahdjukaar.moonlight.platform.registry.fabric;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class RegistryQueue<T> {
    private final Registry<T> registry;
    private final List<EntryWrapper<? extends T>> entries = new ArrayList<>();

    public RegistryQueue(Registry<T> registry) {
        this.registry = registry;
    }

    public Registry<T> getRegistry() {
        return registry;
    }

    public <A extends T> Supplier<A> add(Supplier<A> factory, ResourceLocation name) {
        EntryWrapper<A> wrapper = new EntryWrapper<>(()->Registry.register(registry, name, factory.get()));
        entries.add(wrapper);
        return wrapper;
    }
    public <A extends T> Supplier<A> add(Supplier<A> factory) {
        EntryWrapper<A> wrapper = new EntryWrapper<>(factory);
        entries.add(wrapper);
        return wrapper;
    }

    void initializeEntries() {
        entries.forEach(EntryWrapper::run);
    }


    static class EntryWrapper<T> implements Supplier<T> {
        private Supplier<T> regSupplier;
        private T entry;

        public EntryWrapper(Supplier<T> registration) {
            this.regSupplier = registration;
        }

        @Override
        public T get() {
            return entry;
        }

        void run(){
            this.entry = regSupplier.get();
            regSupplier = null;
        }

    }
}



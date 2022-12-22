package net.mehvahdjukaar.moonlight.api.platform.fabric;

import com.mojang.serialization.Lifecycle;
import net.mehvahdjukaar.moonlight.api.misc.RegSupplier;
import net.mehvahdjukaar.moonlight.api.misc.Registrator;
import net.mehvahdjukaar.moonlight.api.platform.RegHelper;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.WritableRegistry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.ApiStatus;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

@ApiStatus.Internal
public class RegistryQueue<T> {
    private final ResourceKey<? extends Registry<T>> registry;
    private final List<EntryWrapper<? extends T, T>> entries = new ArrayList<>();
    private final List<Consumer<Registrator<T>>> batchRegistration = new ArrayList<>();

    public RegistryQueue(ResourceKey<? extends Registry<T>> registry) {
        this.registry = registry;
    }

    public ResourceKey<? extends Registry<T>> getRegistry() {
        return registry;
    }

    public <A extends T> EntryWrapper<A, T> add(Supplier<A> factory, ResourceLocation name) {
        EntryWrapper<A, T> wrapper = new EntryWrapper<>(name, factory, registry);
        entries.add(wrapper);
        return wrapper;
    }

    public void add(Consumer<Registrator<T>> eventListener) {
        batchRegistration.add(eventListener);
    }

    void initializeEntries() {
        entries.forEach(EntryWrapper::initialize);
        batchRegistration.forEach(e -> e.accept((n, s) -> RegHelper.registerAsync(n, () -> s, registry)));
    }


    static class EntryWrapper<T extends R, R> implements RegSupplier<T> {
        private final ResourceLocation id;
        private final ResourceKey<? extends Registry<R>> registry;
        private Supplier<T> regSupplier;
        private T entry;
        private Holder<T> holder;


        public  EntryWrapper(ResourceLocation id, Supplier<T> factory, ResourceKey<? extends Registry<R>> registry) {
            this.regSupplier = factory;
            this.id = id;
            this.registry = registry;
        }


        @Override
        public T get() {
            return entry;
        }

        @Override
        public ResourceLocation getId() {
            return id;
        }

        @Override
        public Holder<T> getHolder() {
            return holder;
        }

        void initialize() {
            this.holder = ((WritableRegistry) registry).register(ResourceKey.create(registry, id), regSupplier.get(), Lifecycle.stable());
            this.entry = this.holder.value();
            regSupplier = null;
        }

    }
}



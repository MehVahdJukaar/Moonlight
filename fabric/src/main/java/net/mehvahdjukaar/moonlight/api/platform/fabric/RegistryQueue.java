package net.mehvahdjukaar.moonlight.api.platform.fabric;

import com.mojang.datafixers.util.Either;
import net.fabricmc.fabric.api.client.rendering.v1.BuiltinItemRendererRegistry;
import net.mehvahdjukaar.moonlight.api.client.ICustomItemRendererProvider;
import net.mehvahdjukaar.moonlight.api.misc.RegSupplier;
import net.mehvahdjukaar.moonlight.api.misc.Registrator;
import net.mehvahdjukaar.moonlight.api.platform.PlatHelper;
import net.mehvahdjukaar.moonlight.api.platform.RegHelper;
import net.minecraft.core.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;

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
        entries.forEach(e -> e.initialize(true));
        batchRegistration.forEach(e -> e.accept((n, s) -> RegHelper.registerAsync(n, () -> s, registry)));
    }


    static class EntryWrapper<T extends R, R> implements RegSupplier<T> {
        private ResourceKey<? extends Registry<R>> registryKey;
        private Supplier<T> regSupplier;

        private final ResourceKey<R> id;
        private Holder<T> holder = null;


        public EntryWrapper(ResourceLocation id, Supplier<T> factory, ResourceKey<? extends Registry<R>> registry) {
            this.regSupplier = factory;
            this.id = ResourceKey.create(registry, id);
            this.registryKey = registry;
        }


        @Override
        public T get() {
            return value();
        }

        @Override
        public ResourceLocation getId() {
            return id.location();
        }

        @Override
        public ResourceKey<R> getKey() {
            return id;
        }

        void initialize(boolean throwMissingReg) {
            if (this.holder != null) return;
            WritableRegistry writableRegistry = (WritableRegistry) BuiltInRegistries.REGISTRY.get(registryKey.location());
            if (writableRegistry == null) {
                if (throwMissingReg)
                    throw new IllegalStateException("Registry not found: " + registryKey.location());
                return;
            }
            this.holder = writableRegistry.register(id, regSupplier.get(), RegistrationInfo.BUILT_IN);
            regSupplier = null;
            registryKey = null;

            R entry = this.holder.value();
            if (PlatHelper.getPhysicalSide().isClient() && entry instanceof ICustomItemRendererProvider pr) {
                if (BuiltinItemRendererRegistry.INSTANCE.get(pr) == null) {
                    BuiltinItemRendererRegistry.INSTANCE.register(pr,
                            (BuiltinItemRendererRegistry.DynamicItemRenderer) pr.getRendererFactory().get());
                } else {
                    if (PlatHelper.isDev()) throw new AssertionError();
                }
            }
        }

        @Override
        public T value() {
            initialize(true);
            if (this.holder == null) {
                throw new NullPointerException("Trying to access unbound value: " + this.id);
            }

            return (T) this.holder.value();
        }

        @Override
        public boolean isBound() {
            initialize(false);
            return this.holder != null && this.holder.isBound();
        }

        @Override
        public boolean is(ResourceLocation location) {
            return this.id.location().equals(location);
        }

        @Override
        public boolean is(ResourceKey<T> resourceKey) {
            return resourceKey == this.id;
        }

        @Override
        public boolean is(Predicate<ResourceKey<T>> predicate) {
            return predicate.test((ResourceKey<T>) this.id);
        }

        @Override
        public boolean is(TagKey<T> tagKey) {
            initialize(false);
            return this.holder != null && this.holder.is(tagKey);
        }

        @Override
        public boolean is(Holder<T> holder) {
            initialize(false);
            return this.holder != null && this.holder.is(holder);
        }

        @Override
        public Stream<TagKey<T>> tags() {
            initialize(false);
            return this.holder != null ? this.holder.tags() : Stream.empty();
        }

        @Override
        public Either<ResourceKey<T>, T> unwrap() {
            return Either.left((ResourceKey<T>) this.id);
        }

        @Override
        public Optional<ResourceKey<T>> unwrapKey() {
            return Optional.of((ResourceKey<T>) this.id);
        }

        @Override
        public Kind kind() {
            return Kind.REFERENCE;
        }

        @Override
        public boolean canSerializeIn(HolderOwner<T> owner) {
            initialize(false);
            return this.holder != null && this.holder.canSerializeIn(owner);
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            return obj instanceof Holder<?> h && h.kind() == Kind.REFERENCE && h.is((ResourceKey) this.id);
        }

        @Override
        public int hashCode() {
            return this.id.hashCode();
        }

        @Override
        public String toString() {
            return String.format(Locale.ENGLISH, "RegistryHolderSupplier{%s}", this.id);
        }
    }
}



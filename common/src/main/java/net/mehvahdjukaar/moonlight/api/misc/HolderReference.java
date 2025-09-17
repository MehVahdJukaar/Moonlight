package net.mehvahdjukaar.moonlight.api.misc;

import net.mehvahdjukaar.moonlight.api.util.Utils;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.Optional;
import java.util.WeakHashMap;
import java.util.function.Predicate;

public class HolderReference<T> {

    private final ResourceKey<Registry<T>> registryKey;
    private final ResourceKey<T> key;

    private final WeakHashMap<HolderLookup.Provider, Holder<T>> cache = new WeakHashMap<>();

    private static final WeakHashSet<HolderReference<?>> REFERENCES = new WeakHashSet<>();

    @ApiStatus.Internal
    public static void clearCache() {
        REFERENCES.forEach(HolderReference::invalidateCache);
    }

    private void invalidateCache() {
        cache.clear();
    }


    protected HolderReference(ResourceKey<Registry<T>> registryKey, ResourceKey<T> key) {
        this.registryKey = registryKey;
        this.key = key;

        REFERENCES.add(this);
    }

    public static <A> HolderReference<A> of(String id, ResourceKey<Registry<A>> registry) {
        return of(ResourceLocation.tryParse(id), registry);
    }

    public static <A> HolderReference<A> of(ResourceLocation location, ResourceKey<Registry<A>> registry) {
        return new HolderReference<>(registry, ResourceKey.create(registry, location));
    }

    public static <A> HolderReference<A> of(ResourceKey<A> key) {
        return new HolderReference<>(ResourceKey.createRegistryKey(key.registry()), key);
    }

    public static <A> HolderReference.Opt<A> optional(ResourceKey<A> key) {
        return new HolderReference.Opt<>(ResourceKey.createRegistryKey(key.registry()), key);
    }

    public T getUnsafe() {
        return get(Utils.hackyGetRegistryAccess());
    }

    public T get(Entity entity) {
        return get(entity.level());
    }

    public T get(Level level) {
        return get(level.registryAccess());
    }

    public T get(HolderLookup.Provider r) {
        return getHolder(r).value();
    }

    @Deprecated(forRemoval = true)
    public Holder<T> getHolderUnsafe() {
        return getHolder(Utils.hackyGetRegistryAccess());
    }

    public Holder<T> getHolder(Entity entity) {
        return getHolder(entity.level());
    }

    public Holder<T> getHolder(Level level) {
        return getHolder(level.registryAccess());
    }

    public Holder<T> getHolder(HolderLookup.Provider r) {
        var holder = cache.get(r);
        if (holder != null) return holder;
        var lookupReg = r.lookup(registryKey);
        var reg = lookupReg.get();
        holder = lookup(reg);
        cache.put(r, holder);
        return holder;
    }

    public Holder<T> lookup(HolderGetter<T> lookup) {
        try {
            return lookup.getOrThrow(key);
        } catch (Exception e) {
            String extra = "";
            if (lookup instanceof HolderLookup<T> l) {
                extra = ".\nRegistry content was: " + l.listElements().map(b -> b.key().location()).toList();
            }
            throw new RuntimeException("Failed to get object from registry: " + key +
                    ".\nCalled from " + Thread.currentThread() + ".\n" + extra);
        }
    }

    public String getRegisteredName() {
        return key.location().toString();
    }

    public ResourceLocation getID() {
        return key.location();
    }

    public ResourceKey<T> getKey() {
        return key;
    }

    public boolean is(ResourceLocation location) {
        return registryKey.location().equals(location);
    }

    public boolean is(ResourceKey<T> resourceKey) {
        return resourceKey == key;
    }

    public boolean is(Predicate<ResourceKey<T>> predicate) {
        return predicate.test(key);
    }

    public boolean is(Holder<T> other) {
        return other.unwrapKey().get() == key;
    }

    @Override
    public String toString() {
        return "DynamicHolder{" + key + '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof HolderReference<?> that)) return false;
        return Objects.equals(registryKey, that.registryKey) && Objects.equals(key, that.key);
    }

    @Override
    public int hashCode() {
        return Objects.hash(registryKey, key);
    }

    public static class Opt<T> extends HolderReference<T> {

        protected Opt(ResourceKey<Registry<T>> registryKey, ResourceKey<T> key) {
            super(registryKey, key);
        }

        public boolean isPresent(HolderLookup.Provider r) {
            return getHolder(r) != null;
        }

        public Optional<Holder<T>> asOptionalHolder(HolderLookup.Provider r) {
            return Optional.ofNullable(getHolder(r));
        }

        public Optional<T> asOptional(HolderLookup.Provider r) {
            return Optional.ofNullable(get(r));
        }

        @Nullable
        @Override
        public T get(HolderLookup.Provider r) {
            var h = super.getHolder(r);
            return h != null ? h.value() : null;
        }

        @Nullable
        @Override
        public T get(Level level) {
            return super.get(level);
        }

        @Nullable
        @Override
        public T get(Entity entity) {
            return super.get(entity);
        }

        @Nullable
        @Override
        public T getUnsafe() {
            return super.getUnsafe();
        }

        @Nullable
        @Override
        public Holder<T> getHolder(HolderLookup.Provider r) {
            return super.getHolder(r);
        }

        @Nullable
        @Override
        public Holder<T> getHolder(Level level) {
            return super.getHolder(level);
        }

        @Deprecated(forRemoval = true)
        @Nullable
        @Override
        public Holder<T> getHolderUnsafe() {
            return super.getHolderUnsafe();
        }

        @Nullable
        @Override
        public Holder<T> getHolder(Entity entity) {
            return super.getHolder(entity);
        }

        @Nullable
        @Override
        public Holder<T> lookup(HolderGetter<T> lookup) {
            try {
                return super.lookup(lookup);
            } catch (Exception e) {
                return null;
            }
        }
    }
}


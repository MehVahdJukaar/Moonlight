package net.mehvahdjukaar.moonlight.api.misc;

import net.mehvahdjukaar.moonlight.api.util.Utils;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import org.jetbrains.annotations.ApiStatus;

import java.util.Objects;
import java.util.WeakHashMap;
import java.util.function.Predicate;

public class HolderRef<T> {

    private final ResourceKey<Registry<T>> registryKey;
    private final ResourceKey<T> key;

    private final WeakHashMap<HolderLookup.Provider, Holder<T>> cache = new WeakHashMap<>();

    private static final WeakHashSet<HolderRef<?>> REFERENCES = new WeakHashSet<>();

    @ApiStatus.Internal
    public static void clearCache() {
        REFERENCES.forEach(HolderRef::invalidateCache);
    }

    private void invalidateCache() {
        cache.clear();
    }


    protected HolderRef(ResourceKey<Registry<T>> registryKey, ResourceKey<T> key) {
        this.registryKey = registryKey;
        this.key = key;

        REFERENCES.add(this);
    }

    public static <A> HolderRef<A> wrap(A obj, ResourceKey<Registry<A>> registry) {
        return of(Utils.getID(obj), registry);
    }

    public static <A> HolderRef<A> of(String id, ResourceKey<Registry<A>> registry) {
        return of(ResourceLocation.tryParse(id), registry);
    }

    public static <A> HolderRef<A> of(ResourceLocation location, ResourceKey<Registry<A>> registry) {
        return new HolderRef<>(registry, ResourceKey.create(registry, location));
    }

    public static <A> HolderRef<A> of(ResourceKey<A> key) {
        return new HolderRef<>(ResourceKey.createRegistryKey(key.registry()), key);
    }

    public static <A> OptHolderRef<A> optional(ResourceLocation location, ResourceKey<Registry<A>> registry) {
        return new OptHolderRef<>(registry, ResourceKey.create(registry, location));
    }

    public static <A> OptHolderRef<A> optional(ResourceKey<A> key) {
        return new OptHolderRef<>(ResourceKey.createRegistryKey(key.registry()), key);
    }

    public T get(Entity entity) {
        return get(entity.level());
    }

    //TODO: remove
    public T get(Level level) {
        return get(level.registryAccess());
    }

    public T get(LevelReader level) {
        return get(level.registryAccess());
    }

    public T get(HolderLookup.Provider r) {
        return getHolder(r).value();
    }

    public boolean is(T object, HolderLookup.Provider r) {
        return getHolder(r).value() == object;
    }

    public boolean is(T object, LevelReader level) {
        return is(object, level.registryAccess());
    }

    public boolean is(TagKey<T> tag, HolderLookup.Provider r) {
        return getHolder(r).is(tag);
    }

    public boolean is(TagKey<T> tag, LevelReader level) {
        return is(tag, level.registryAccess());
    }

    public Holder<T> getHolder(Entity entity) {
        return getHolder(entity.level());
    }

    public Holder<T> getHolder(Level level) {
        return getHolder(level.registryAccess());
    }

    public Holder<T> getHolder(LevelReader level) {
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
        if (!(o instanceof HolderRef<?> that)) return false;
        return Objects.equals(registryKey, that.registryKey) && Objects.equals(key, that.key);
    }

    @Override
    public int hashCode() {
        return Objects.hash(registryKey, key);
    }

    @Deprecated(forRemoval = true)
    public static class Opt<T> extends OptHolderRef<T> {

        protected Opt(ResourceKey<Registry<T>> registryKey, ResourceKey<T> key) {
            super(registryKey, key);
        }
    }
}


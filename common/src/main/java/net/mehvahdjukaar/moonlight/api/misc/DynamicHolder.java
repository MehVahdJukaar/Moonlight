package net.mehvahdjukaar.moonlight.api.misc;

import com.mojang.datafixers.util.Either;
import net.mehvahdjukaar.moonlight.api.util.Utils;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderOwner;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;

//can be statically stored and persists across world loads
/**
 * A soft reference to an object in a Data pack registry
 * Like registry object but can be invalidated and works for data pack registries
 */
//TODO: add optionals
public class DynamicHolder<T> implements Supplier<T>, Holder<T> {

    @ApiStatus.Internal
    public static void onDataReload() {
        REFERENCES.forEach(DynamicHolder::invalidateInstance);
    }

    private static final WeakHashSet<DynamicHolder<?>> REFERENCES = new WeakHashSet<>();

    private final ResourceKey<Registry<T>> registryKey;
    private final ResourceKey<T> key;

    // needs to be thread local because of data pack stuff. datapack registries will have 2 objects with the same key
    private final ThreadLocal<Holder<T>> instance = new ThreadLocal<>();

    protected DynamicHolder(ResourceKey<Registry<T>> registryKey, ResourceKey<T> key) {
        this.registryKey = registryKey;
        this.key = key;
        REFERENCES.add(this);
    }

    public static <A> DynamicHolder<A> of(String id, ResourceKey<Registry<A>> registry) {
        return of(ResourceLocation.tryParse(id), registry);
    }

    public static <A> DynamicHolder<A> of(ResourceLocation location, ResourceKey<Registry<A>> registry) {
        return new DynamicHolder<>(registry, ResourceKey.create(registry, location));
    }

    public static <A> DynamicHolder<A> of(ResourceKey<A> key) {
        return new DynamicHolder<>(ResourceKey.createRegistryKey(key.registry()), key);
    }

    private void invalidateInstance() {
        instance.remove();
    }

    @NotNull
    protected Holder<T> getInstance() {
        Holder<T> value = instance.get();
        if (value == null) {
            var r = Utils.hackyGetRegistryAccess();
            Registry<T> reg = r.registryOrThrow(registryKey);
            try {
                value = reg.getHolderOrThrow(key);
                instance.set(value);
            } catch (Exception e) {
                throw new RuntimeException("Failed to get object from registry: " + key +
                        ".\nCalled from " + Thread.currentThread() + ".\n" +
                        "Registry content was: " + reg.entrySet().stream().map(b -> b.getKey().location()).toList(), e);
            }
        }
        return value;
    }

    @Override
    public String getRegisteredName() {
        return key.location().toString();
    }

    public ResourceLocation getID() {
        return key.location();
    }

    public ResourceKey<T> getKey() {
        return key;
    }

    @Deprecated(forRemoval = true)
    @NotNull
    public T get() {
        return getInstance().value();
    }

    @Override
    public T value() {
        return getInstance().value();
    }

    @Override
    public boolean isBound() {
        return getInstance().isBound();
    }

    @Override
    public boolean is(ResourceLocation location) {
        return registryKey.location().equals(location);
    }

    @Override
    public boolean is(ResourceKey<T> resourceKey) {
        return resourceKey == key;
    }

    @Override
    public boolean is(Predicate<ResourceKey<T>> predicate) {
        return predicate.test(key);
    }

    public boolean is(Holder<T> other){
        return other == this || other.unwrapKey().get() == key;
    }

    @Override
    public boolean is(TagKey<T> tagKey) {
        return getInstance().is(tagKey);
    }

    @Override
    public boolean is(Holder<T> holder) {
        return instance().is(holder);
    }

    @Override
    public Stream<TagKey<T>> tags() {
        return getInstance().tags();
    }

    @Override
    public Either<ResourceKey<T>, T> unwrap() {
        return Either.left(this.key);
    }

    @Override
    public Optional<ResourceKey<T>> unwrapKey() {
        return Optional.of(key);
    }

    @Override
    public Kind kind() {
        return getInstance().kind();
    }

    @Override
    public boolean canSerializeIn(HolderOwner<T> owner) {
        return getInstance().canSerializeIn(owner);
    }
}

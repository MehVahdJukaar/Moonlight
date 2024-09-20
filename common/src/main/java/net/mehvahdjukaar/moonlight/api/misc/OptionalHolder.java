package net.mehvahdjukaar.moonlight.api.misc;

import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.stream.Stream;

public class OptionalHolder<T> extends DynamicHolder<T> {
    private boolean resolved = false;
    private boolean empty = false;

    protected OptionalHolder(ResourceKey<Registry<T>> registryKey, ResourceKey<T> key) {
        super(registryKey, key);
    }

    public static <A> OptionalHolder<A> of(ResourceLocation location, ResourceKey<Registry<A>> registry) {
        return new OptionalHolder<>(registry, ResourceKey.create(registry, location));
    }

    public static <A> OptionalHolder<A> of(ResourceKey<A> key) {
        return new OptionalHolder<>(ResourceKey.createRegistryKey(key.registry()), key);
    }

    public static <A> OptionalHolder<A> of(String id, ResourceKey<Registry<A>> registry) {
        return of(ResourceLocation.tryParse(id), registry);
    }

    @Override
    protected void invalidateInstance() {
        super.invalidateInstance();
        resolved = false;
        empty = false;
    }

    @Nullable
    @Override
    protected Holder<T> getInstance() {
        if (!resolved) {
            resolved = true;
            try {
                return super.getInstance();
            } catch (Exception ignored) {
            }
            empty = instance.get() != null;
        }
        return instance.get();
    }

    @Override
    public Stream<TagKey<T>> tags() {
        var i = getInstance();
        if (i != null) return i.tags();
        return Stream.empty();
    }

    @Override
    public boolean is(TagKey<T> tagKey) {
        var i = getInstance();
        if (i != null) return i.is(tagKey);
        return false;
    }

    @Nullable
    @Override
    public T get() {
        return super.get();
    }

    @Nullable
    @Override
    public T value() {
        var i = getInstance();
        if (i != null) return i.value();
        return null;
    }

    public Optional<Holder<T>> asOptional() {
        return Optional.ofNullable(getInstance());
    }

    public Optional<T> asOptionalValue() {
        return Optional.ofNullable(value());
    }

    public boolean isPresent() {
        return !isEmpty();
    }

    public boolean isEmpty() {
        if(!resolved) getInstance();
        return empty;
    }
}

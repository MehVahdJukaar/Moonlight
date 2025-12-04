package net.mehvahdjukaar.moonlight.api.misc;

import net.mehvahdjukaar.moonlight.api.util.math.MthUtils;
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
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.function.Consumer;

public class OptHolderRef<T> extends HolderRef<T> {


    public static <A> OptHolderRef<A> of(ResourceLocation location, ResourceKey<Registry<A>> registry) {
        return new OptHolderRef<>(registry, ResourceKey.create(registry, location));
    }

    public static <A> OptHolderRef<A> of(ResourceKey<A> key) {
        return new OptHolderRef<>(ResourceKey.createRegistryKey(key.registry()), key);
    }

    protected OptHolderRef(ResourceKey<Registry<T>> registryKey, ResourceKey<T> key) {
        super(registryKey, key);
    }

    public boolean isPresent(HolderLookup.Provider r) {
        return getHolder(r) != null;
    }

    public boolean isPresent(LevelReader level) {
        return getHolder(level) != null;
    }

    public void ifPresent(HolderLookup.Provider r, Consumer<T> consumer) {
        Holder<T> h = getHolder(r);
        if (h != null) {
            consumer.accept(h.value());
        }
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

    @Override
    public boolean is(T object, HolderLookup.Provider r) {
        Holder<T> holder = getHolder(r);
        if (holder == null) return false;
        return holder.value() == object;
    }

    @Override
    public boolean is(TagKey<T> tag, HolderLookup.Provider r) {
        Holder<T> holder = getHolder(r);
        if (holder == null) return false;
        return holder.is(tag);
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
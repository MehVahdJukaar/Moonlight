package net.mehvahdjukaar.moonlight.api.misc;

import com.google.common.base.Suppliers;
import com.mojang.datafixers.util.Either;
import net.mehvahdjukaar.moonlight.api.util.Utils;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.HolderOwner;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;

//optional supplier of a builtin registry
public class OptRegSupplier<A> implements RegSupplier<A> {

    private final Supplier<A> supp;
    private final Supplier<Holder<A>> holderSupplier;
    private final Identifier id;
    private final ResourceKey<A> key;

    protected OptRegSupplier(Registry<A> reg, Identifier loc) {
        this.supp = Suppliers.memoize(() -> reg.getOptional(loc).orElse(null));
        this.holderSupplier = Suppliers.memoize(() -> reg.get(loc).orElse(null));
        this.id = loc;
        this.key = ResourceKey.create(reg.key(), loc);
    }

    public static <A> OptRegSupplier<A> of(Identifier location, Registry<A> registry) {
        return new OptRegSupplier<>(registry, location);
    }

    public static <A> OptRegSupplier<A> of(Identifier location, ResourceKey<Registry<A>> registry) {
        Registry<A> reg = (Registry<A>) BuiltInRegistries.REGISTRY.getOrThrow((ResourceKey) registry).value();
        return new OptRegSupplier<>(reg, location);
    }

    public static <A> OptRegSupplier<A> wrap(A obj, ResourceKey<Registry<A>> registry) {
        Registry<A> reg = (Registry<A>) BuiltInRegistries.REGISTRY.getOrThrow((ResourceKey) registry).value();
        return wrap(obj, reg);
    }

    public static <A> OptRegSupplier<A> wrap(A obj, Registry<A> registry) {
        return of(Utils.getID(obj), registry);
    }

    public Optional<Holder<A>> asOptionalHolder() {
        return Optional.ofNullable(holderSupplier.get());
    }

    @Nullable
    @Override
    public A value() {
        return this.get();
    }

    @Override
    public boolean isBound() {
        if (isPresent()) {
            return holderSupplier.get().isBound();
        }
        return false;
    }

    @Override
    public boolean is(Identifier location) {
        return this.id.equals(location);
    }

    @Override
    public boolean is(ResourceKey<A> resourceKey) {
        return this.key.equals(resourceKey);
    }

    @Override
    public boolean is(Predicate<ResourceKey<A>> predicate) {
        return predicate.test(this.key);
    }

    @Override
    public boolean is(TagKey<A> tag) {
        Holder<A> h = holderSupplier.get();
        return h != null && h.is(tag);
    }

    @Override
    public boolean is(Holder<A> holder) {
        Holder<A> h = holderSupplier.get();
        return h != null && h.equals(holder);
    }

    @Override
    public Stream<TagKey<A>> tags() {
        Holder<A> h = holderSupplier.get();
        if (h != null) {
            return h.tags();
        }
        return Stream.empty();
    }

    @Override
    public Either<ResourceKey<A>, A> unwrap() {
        return Either.right(this.get());
    }

    @Override
    public Optional<ResourceKey<A>> unwrapKey() {
        return Optional.ofNullable(this.key);
    }

    @Override
    public Kind kind() {
        if (isPresent()) {
            return holderSupplier.get().kind();
        }
        return Kind.DIRECT;
    }

    @Override
    public boolean areComponentsBound() {
        Holder<A> h = holderSupplier.get();
        return h != null && h.areComponentsBound();
    }

    @Override
    public DataComponentMap components() {
        Holder<A> h = holderSupplier.get();
        return h != null ? h.components() : DataComponentMap.EMPTY;
    }

    @Override
    public boolean canSerializeIn(HolderOwner<A> owner) {
        if (isPresent()) {
            return holderSupplier.get().canSerializeIn(owner);
        }
        return false;
    }

    public boolean isPresent() {
        return this.get() != null;
    }

    public void ifPresent(Consumer<A> consumer) {
        A a = this.get();
        if (a != null) {
            consumer.accept(a);
        }
    }

    @Nullable
    @Override
    public A get() {
        return supp.get();
    }

    @Override
    public Identifier getId() {
        return id;
    }

    @Nullable
    @Override
    public ResourceKey<A> getKey() {
        return key;
    }

    @Nullable
    public Holder<A> getHolder() {
        return holderSupplier.get();
    }
}

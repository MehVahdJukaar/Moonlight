package net.mehvahdjukaar.moonlight.api.misc;

import com.google.common.base.Suppliers;
import net.mehvahdjukaar.moonlight.api.util.Utils;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;

//optional supplier of a builtin registry
public class OptRegSupplier<A> implements RegSupplier<A> {

    private final Supplier<A> supp;
    private final Supplier<Holder<A>> holderSupplier;
    private final ResourceLocation id;
    private final ResourceKey<A> key;

    protected OptRegSupplier(Registry<A> reg, ResourceLocation loc) {
        this.supp = Suppliers.memoize(() -> reg.get(loc));
        this.holderSupplier = Suppliers.memoize(() -> reg.getHolder(loc).orElse(null));
        this.id = loc;
        this.key = ResourceKey.create(reg.key(), loc);
    }

    public static <A> OptRegSupplier<A> of(ResourceLocation location, Registry<A> registry) {
        return new OptRegSupplier<>(registry, location);
    }

    public static <A> OptRegSupplier<A> of(ResourceLocation location, ResourceKey<Registry<A>> registry) {
        Registry<A> reg = BuiltInRegistries.REGISTRY.getOrThrow((ResourceKey) registry);
        return new OptRegSupplier<>(reg, location);
    }

    public static <A> OptRegSupplier<A> wrap(A obj, ResourceKey<Registry<A>> registry) {
        Registry<A> reg = BuiltInRegistries.REGISTRY.getOrThrow((ResourceKey) registry);
        return wrap(obj, reg);
    }

    public static <A> OptRegSupplier<A> wrap(A obj, Registry<A> registry) {
        return of(Utils.getID(obj), registry);
    }

    public Optional<Holder<A>> asOptionalHolder() {
        return Optional.empty();
    }

    @Override
    public boolean is(TagKey<A> tag) {
        Holder<A> h = this.getHolder();
        return h != null && h.is(tag);
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
    public ResourceLocation getId() {
        return id;
    }

    @Nullable
    @Override
    public ResourceKey<A> getKey() {
        return key;
    }

    @Nullable
    @Override
    public Holder<A> getHolder() {
        return holderSupplier.get();
    }
}

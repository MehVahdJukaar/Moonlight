package net.mehvahdjukaar.moonlight.api.misc;

import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;

import java.util.function.Supplier;

/**
 * Basically a registry object wrapper and simple supplier with id and holder functionality for fabric
 * Supplier of a builtin registry. for the others use HolderRef or OptHolderRef
 */
// maybe implement holder here
public interface RegSupplier<T> extends Supplier<T>, Holder<T> {

    @Override
    T get();

    ResourceLocation getId();

    ResourceKey<T> getKey();

    @Deprecated(forRemoval = true)
    default Holder<T> getHolder() {
        return this;
    }

    default boolean is(T other) {
        return this.get() == other;
    }
}

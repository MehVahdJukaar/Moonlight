package net.mehvahdjukaar.moonlight.api.util;

import com.google.common.base.Suppliers;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

public interface INamedSupplier<T> extends Supplier<T> {

    @Nullable
    ResourceLocation getId();

    @Nullable
    T get();

    @NotNull
    default T getOrThrow() {
        var t = get();
        if (t == null) throw new AssertionError("Failed to get object with ID " + getId());
        return t;
    }

    static <T> INamedSupplier<T> memoize(ResourceLocation id, Supplier<T> supp) {
        var instance = Suppliers.memoize(supp::get);
        return new INamedSupplier<>() {
            @Override
            public ResourceLocation getId() {
                return id;
            }

            @Override
            public T get() {
                return instance.get();
            }
        };
    }

}

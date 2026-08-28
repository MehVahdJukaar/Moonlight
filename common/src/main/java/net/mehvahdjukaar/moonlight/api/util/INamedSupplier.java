package net.mehvahdjukaar.moonlight.api.util;

import com.google.common.base.Suppliers;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

public interface INamedSupplier<T> extends Supplier<T> {

    @Nullable
    Identifier getId();

    @Nullable
    T get();

    @NotNull
    default T getOrThrow() {
        var t = get();
        if (t == null) throw new AssertionError("Failed to get object with ID " + getId());
        return t;
    }

    static <T> INamedSupplier<T> memoize(Identifier id, Supplier<T> supp) {
        var instance = Suppliers.memoize(supp::get);
        return new INamedSupplier<>() {
            @Override
            public Identifier getId() {
                return id;
            }

            @Override
            public T get() {
                return instance.get();
            }
        };
    }

}

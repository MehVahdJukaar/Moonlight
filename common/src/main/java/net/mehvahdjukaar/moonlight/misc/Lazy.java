package net.mehvahdjukaar.moonlight.misc;

import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

public class Lazy<T> implements Supplier<T> {

    private Supplier<T> supplier;
    private T value = null;

    private Lazy(@NotNull Supplier<T> supplier) {
        this.supplier = supplier;
    }

    public static <A> Lazy<A> of(Supplier<A> o) {
        return new Lazy<>(o);
    }

    public T getValue() {
        return value;
    }

    @Override
    public T get() {
        if (supplier != null) {
            this.value = supplier.get();
            this.supplier = null;
        }
        return value;
    }
}

package net.mehvahdjukaar.moonlight.api.misc;

import java.util.Objects;
import java.util.function.Function;

@FunctionalInterface
public interface TriFunction<T, U, V, R> {

    R apply(T t, U u, V v);

    default <W> TriFunction<T, U, V, W> andThen(final Function<? super R, ? extends W> after) {
        Objects.requireNonNull(after);
        return (final T t, final U u, final V v) -> after.apply(apply(t, u, v));
    }
}

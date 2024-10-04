package net.mehvahdjukaar.moonlight.api.misc;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

public record DefaultMap<A, B>(B obj) implements Map<A, B> {

    public static <A, B> DefaultMap<A, B> of(B obj) {
        return new DefaultMap<>(obj);
    }

    @Override
    public int size() {
        return 1;
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public boolean containsKey(Object o) {
        return true;
    }

    @Override
    public boolean containsValue(Object o) {
        return o.equals(this.obj);
    }

    @Override
    public B get(Object o) {
        return null;
    }

    @Override
    public @Nullable B put(A a, B b) {
        throw new UnsupportedOperationException();
    }

    @Override
    public B remove(Object o) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void putAll(@NotNull Map<? extends A, ? extends B> map) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException();
    }

    @Override
    public @NotNull Set<A> keySet() {
        return Set.of();
    }

    @Override
    public @NotNull Collection<B> values() {
        return List.of(obj);
    }

    @Override
    public @NotNull Set<Entry<A, B>> entrySet() {
        return Set.of(); //not correct aaa
    }


}

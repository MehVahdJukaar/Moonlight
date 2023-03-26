package net.mehvahdjukaar.moonlight.api.misc;

@FunctionalInterface
public interface QuadConsumer<K, V, S, T> {
    void accept(K k, V v, S s, T t);
}
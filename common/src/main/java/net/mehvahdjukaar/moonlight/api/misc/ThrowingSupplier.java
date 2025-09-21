package net.mehvahdjukaar.moonlight.api.misc;

public interface ThrowingSupplier<T> {

    T get() throws Exception;
}

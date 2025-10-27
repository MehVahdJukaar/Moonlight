package net.mehvahdjukaar.moonlight.api.misc;

import java.lang.reflect.Field;

public class TField<C, T> {

    private final Field field;

    public static <C, T> TField<C, T> of(Class<C> clazz, String name) {
        return new TField<>(clazz, name);
    }

    public TField(Class<C> field, String name) {
        try {
            this.field = field.getDeclaredField(name);
            this.field.setAccessible(true);
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }

    public T get(C obj) {
        try {
            return (T) field.get(obj);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public void set(C obj, T value) {
        try {
            field.set(obj, value);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}

package net.mehvahdjukaar.moonlight.api.platform.configs;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.RecordComponent;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

final class ConfigBeans {

    static <T> Supplier<T> define(ConfigBuilder builder, Class<T> type, T defaultValue) {
        return type.isRecord() ? defineRecord(builder, type, defaultValue) : definePojo(builder, type, defaultValue);
    }

    private static <T> Supplier<T> definePojo(ConfigBuilder builder, Class<T> type, T defaultValue) {
        List<Field> fields = new ArrayList<>();
        List<Supplier<?>> readers = new ArrayList<>();
        try {
            for (Field f : type.getDeclaredFields()) {
                int mods = f.getModifiers();
                if (Modifier.isStatic(mods) || Modifier.isTransient(mods)) continue;
                f.setAccessible(true);
                fields.add(f);
                readers.add(defineField(builder, f.getName(), f.getType(), f.get(defaultValue)));
            }
            var ctor = type.getDeclaredConstructor();
            ctor.setAccessible(true);
            return () -> {
                try {
                    T instance = ctor.newInstance();
                    for (int i = 0; i < fields.size(); i++) fields.get(i).set(instance, readers.get(i).get());
                    return instance;
                } catch (ReflectiveOperationException e) {
                    throw new RuntimeException("Failed to build bean " + type.getName(), e);
                }
            };
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("defineBean: " + type.getName() + " needs a no-arg constructor and readable fields", e);
        }
    }

    private static <T> Supplier<T> defineRecord(ConfigBuilder builder, Class<T> type, T defaultValue) {
        RecordComponent[] comps = type.getRecordComponents();
        List<Supplier<?>> readers = new ArrayList<>();
        Class<?>[] paramTypes = new Class<?>[comps.length];
        try {
            for (int i = 0; i < comps.length; i++) {
                paramTypes[i] = comps[i].getType();
                readers.add(defineField(builder, comps[i].getName(), comps[i].getType(),
                        comps[i].getAccessor().invoke(defaultValue)));
            }
            var ctor = type.getDeclaredConstructor(paramTypes);
            ctor.setAccessible(true);
            return () -> {
                try {
                    Object[] args = new Object[readers.size()];
                    for (int i = 0; i < args.length; i++) args[i] = readers.get(i).get();
                    return ctor.newInstance(args);
                } catch (ReflectiveOperationException e) {
                    throw new RuntimeException("Failed to build record " + type.getName(), e);
                }
            };
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("defineBean: failed to read record " + type.getName(), e);
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static Supplier<?> defineField(ConfigBuilder builder, String name, Class<?> type, Object current) {
        if (type == boolean.class || type == Boolean.class) return builder.define(name, (Boolean) current);
        if (type == int.class || type == Integer.class)
            return builder.define(name, (Integer) current, Integer.MIN_VALUE, Integer.MAX_VALUE);
        if (type == double.class || type == Double.class)
            return builder.define(name, (Double) current, -Double.MAX_VALUE, Double.MAX_VALUE);
        if (type == float.class || type == Float.class)
            return builder.define(name, (Float) current, -Float.MAX_VALUE, Float.MAX_VALUE);
        // must reject null: a null-accepting validator makes NeoForge treat a MISSING string field as valid, so it
        // never writes the default, and then re-corrects the spec every load -> endless "config is not correct" loop
        if (type == String.class) return builder.define(name, (String) current);
        if (type.isEnum()) return builder.define(name, (Enum) current);
        throw new IllegalArgumentException("defineBean: unsupported field type " + type.getName() + " for field '" + name + "'");
    }
}

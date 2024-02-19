package net.mehvahdjukaar.moonlight.api;

import net.mehvahdjukaar.moonlight.api.platform.PlatHelper;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class ModSharedVariables {

    private static final Map<String, Supplier<String>> STRINGS = new HashMap<>();
    private static final Map<String, Supplier<Double>> DOUBLES = new HashMap<>();
    private static final Map<String, Supplier<Boolean>> BOOL = new HashMap<>();

    public static void registerString(String name, Supplier<String> supp) {
        if (STRINGS.put(name, supp) != null && PlatHelper.isDev()) {
            throw new AssertionError("There already exist a shared variable with name " + name);
        }
    }

    public static void registerDouble(String name, Supplier<Double> supp) {
        if (DOUBLES.put(name, supp) != null && PlatHelper.isDev()) {
            throw new AssertionError("There already exist a shared variable with name " + name);
        }
    }

    public static void registerBool(String name, Supplier<Boolean> supp) {
        if (BOOL.put(name, supp) != null && PlatHelper.isDev()) {
            throw new AssertionError("There already exist a shared variable with name " + name);
        }
    }

    @Nullable
    public static Double getDouble(String name) {
        var sup = DOUBLES.get(name);
        return sup == null ? null : sup.get();
    }

    @Nullable
    public static Boolean getBool(String name) {
        var sup = BOOL.get(name);
        return sup == null ? null : sup.get();
    }

    @Nullable
    public static String getString(String name) {
        var sup = STRINGS.get(name);
        return sup == null ? null : sup.get();
    }
}

package net.mehvahdjukaar.moonlight.neoforge;

import net.mehvahdjukaar.moonlight.core.Moonlight;
import net.minecraft.client.renderer.LevelRenderer;
import net.neoforged.fml.ModLoadingException;
import net.neoforged.fml.config.ConfigTracker;
import net.neoforged.fml.config.IConfigSpec;
import net.neoforged.fml.config.ModConfig;
import org.slf4j.Logger;
import org.slf4j.Marker;
import sun.misc.Unsafe;

import java.lang.reflect.Field;
import java.util.Objects;
import java.util.function.UnaryOperator;

public class ConfigHacks {

    static {
        try {
            replaceStaticField(ConfigTracker.class, "LOGGER", o -> new ShushLogger((Logger) o));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // Get Unsafe instance (reflectively)
    private static Unsafe getUnsafe() throws ReflectiveOperationException {
        Field u = Unsafe.class.getDeclaredField("theUnsafe");
        u.setAccessible(true);
        return (Unsafe) u.get(null);
    }

    @SuppressWarnings("unchecked")
    public static <T> T replaceStaticField(Class<?> ownerClass,
                                           String fieldName,
                                           UnaryOperator<T> operator) throws Exception {
        Objects.requireNonNull(ownerClass, "ownerClass");
        Objects.requireNonNull(fieldName, "fieldName");
        Objects.requireNonNull(operator, "operator");

        Field field = ownerClass.getDeclaredField(fieldName);
        field.setAccessible(true);

        // Ensure it's static
        if ((field.getModifiers() & java.lang.reflect.Modifier.STATIC) == 0) {
            throw new IllegalArgumentException("Field " + fieldName + " is not static");
        }

        // Read current value (static -> target null)
        T current = (T) field.get(null);

        // Compute replacement
        T replacement = operator.apply(current);

        // Write replacement with Unsafe (bypass final)
        Unsafe unsafe = getUnsafe();
        Object base = unsafe.staticFieldBase(field);
        long offset = unsafe.staticFieldOffset(field);
        unsafe.putObjectVolatile(base, offset, replacement);

        return current;
    }

    public static void init() {

    }


    //I cant mixin in FML. That's why this madness
    private record ShushLogger(Logger base) implements Logger {

        @Override
        public String getName() {
            return base.getName();
        }

        @Override
        public boolean isTraceEnabled() {
            return base.isTraceEnabled();
        }

        @Override
        public void trace(String msg) {
            base.trace(msg);
        }

        @Override
        public void trace(String format, Object arg) {
            base.trace(format, arg);
        }

        @Override
        public void trace(String format, Object arg1, Object arg2) {
            base.trace(format, arg1, arg2);
        }

        @Override
        public void trace(String format, Object... arguments) {
            base.trace(format);
        }

        @Override
        public void trace(String msg, Throwable t) {
            base.trace(msg, t);
        }

        @Override
        public boolean isTraceEnabled(Marker marker) {
            return base.isTraceEnabled(marker);
        }

        @Override
        public void trace(Marker marker, String msg) {
            base.trace(marker, msg);
        }

        @Override
        public void trace(Marker marker, String format, Object arg) {
            base.trace(marker, format, arg);
        }

        @Override
        public void trace(Marker marker, String format, Object arg1, Object arg2) {
            base.trace(marker, format, arg1, arg2);
        }

        @Override
        public void trace(Marker marker, String format, Object... argArray) {
            base.trace(marker, format, argArray);
        }

        @Override
        public void trace(Marker marker, String msg, Throwable t) {
            base.trace(marker, msg, t);
        }

        @Override
        public boolean isDebugEnabled() {
            return base.isDebugEnabled();
        }

        @Override
        public void debug(String msg) {
            base.debug(msg);
        }

        @Override
        public void debug(String format, Object arg) {
            base.debug(format, arg);
        }

        @Override
        public void debug(String format, Object arg1, Object arg2) {
            base.debug(format, arg1, arg2);
        }

        @Override
        public void debug(String format, Object... arguments) {
            base.debug(format);
        }

        @Override
        public void debug(String msg, Throwable t) {
            base.debug(msg, t);
        }

        @Override
        public boolean isDebugEnabled(Marker marker) {
            return base.isDebugEnabled(marker);
        }

        @Override
        public void debug(Marker marker, String msg) {
            base.debug(marker, msg);
        }

        @Override
        public void debug(Marker marker, String format, Object arg) {
            base.debug(marker, format, arg);
        }

        @Override
        public void debug(Marker marker, String format, Object arg1, Object arg2) {
            base.debug(marker, format, arg1, arg2);
        }

        @Override
        public void debug(Marker marker, String format, Object... arguments) {
            base.debug(marker, format, arguments);
        }

        @Override
        public void debug(Marker marker, String msg, Throwable t) {
            base.debug(marker, msg, t);
        }

        @Override
        public boolean isInfoEnabled() {
            return base.isInfoEnabled();
        }

        @Override
        public void info(String msg) {
            base.info(msg);
        }

        @Override
        public void info(String format, Object arg) {
            base.info(format, arg);
        }

        @Override
        public void info(String format, Object arg1, Object arg2) {
            base.info(format, arg1, arg2);
        }

        @Override
        public void info(String format, Object... arguments) {
            base.info(format);
        }

        @Override
        public void info(String msg, Throwable t) {
            base.info(msg, t);
        }

        @Override
        public boolean isInfoEnabled(Marker marker) {
            return base.isInfoEnabled(marker);
        }

        @Override
        public void info(Marker marker, String msg) {
            base.info(marker, msg);
        }

        @Override
        public void info(Marker marker, String format, Object arg) {
            base.info(marker, format, arg);
        }

        @Override
        public void info(Marker marker, String format, Object arg1, Object arg2) {
            base.info(marker, format, arg1, arg2);
        }

        @Override
        public void info(Marker marker, String format, Object... arguments) {
            base.info(marker, format, arguments);
        }

        @Override
        public void info(Marker marker, String msg, Throwable t) {
            base.info(marker, msg, t);
        }

        @Override
        public boolean isWarnEnabled() {
            return base.isWarnEnabled();
        }

        @Override
        public void warn(String msg) {
            base.warn(msg);
        }

        @Override
        public void warn(String format, Object arg) {
            base.warn(format, arg);
        }

        @Override
        public void warn(String format, Object... arguments) {
            base.warn(format, arguments);
        }

        @Override
        public void warn(String format, Object arg1, Object arg2) {
            if (arg1 instanceof IConfigSpec.ILoadedConfig lc && isMyConfig(lc)) {
                return; //forge shush!
            }
            base.warn(format, arg1, arg2);
        }

        private static boolean isMyConfig(IConfigSpec.ILoadedConfig lc) {
            try {
                var method = lc.getClass().getDeclaredMethod("modConfig");
                method.setAccessible(true);
                ModConfig cf = (ModConfig) method.invoke(lc);
                return Moonlight.getDependents().contains(cf.getModId());
            } catch (Exception ignored){
            }
            return false;
        }

        @Override
        public void warn(String msg, Throwable t) {
            base.warn(msg, t);
        }

        @Override
        public boolean isWarnEnabled(Marker marker) {
            return base.isWarnEnabled(marker);
        }

        @Override
        public void warn(Marker marker, String msg) {
            base.warn(marker, msg);
        }

        @Override
        public void warn(Marker marker, String format, Object arg) {
            base.warn(marker, format, arg);
        }

        @Override
        public void warn(Marker marker, String format, Object arg1, Object arg2) {
            base.warn(marker, format, arg1, arg2);
        }

        @Override
        public void warn(Marker marker, String format, Object... arguments) {
            base.warn(marker, format, arguments);
        }

        @Override
        public void warn(Marker marker, String msg, Throwable t) {
            base.warn(marker, msg, t);
        }

        @Override
        public boolean isErrorEnabled() {
            return base.isErrorEnabled();
        }

        @Override
        public void error(String msg) {
            base.error(msg);
        }

        @Override
        public void error(String format, Object arg) {
            base.error(format, arg);
        }

        @Override
        public void error(String format, Object arg1, Object arg2) {
            base.error(format, arg1, arg2);
        }

        @Override
        public void error(String format, Object... arguments) {
            base.error(format, arguments);
        }

        @Override
        public void error(String msg, Throwable t) {
            base.error(msg, t);
        }

        @Override
        public boolean isErrorEnabled(Marker marker) {
            return base.isErrorEnabled(marker);
        }

        @Override
        public void error(Marker marker, String msg) {
            base.error(marker, msg);
        }

        @Override
        public void error(Marker marker, String format, Object arg) {
            base.error(marker, format, arg);
        }

        @Override
        public void error(Marker marker, String format, Object arg1, Object arg2) {
            base.error(marker, format, arg1, arg2);
        }

        @Override
        public void error(Marker marker, String format, Object... arguments) {
            base.error(marker, format, arguments);
        }

        @Override
        public void error(Marker marker, String msg, Throwable t) {
            base.error(marker, msg, t);
        }

        //implement all Logger methods delegating to base
    }
}

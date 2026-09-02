package net.mehvahdjukaar.moonlight.api.platform.configs.options;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import net.mehvahdjukaar.moonlight.api.platform.configs.ModConfigHolder;
import net.mehvahdjukaar.moonlight.api.platform.configs.IConfigValue;
import net.mehvahdjukaar.moonlight.api.util.math.Range;
import net.mehvahdjukaar.codecui.Schema;
import net.mehvahdjukaar.codecui.SchemaCodec;
import net.minecraft.core.Vec3i;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * One editable value on a config screen. Most kinds extend SimpleConfigOption and sit on a single stored value of the
 * same type. The grouped ones (range, vec3, json) sit on several stored values and extend this class directly.
 */
public abstract class ConfigOption<T> extends ConfigNode {

    protected final T defaultValue;

    protected ConfigOption(Component title, @Nullable Component description, T defaultValue) {
        super(title, description);
        this.defaultValue = defaultValue;
    }

    /** When a change takes effect. A grouped row reports the heaviest of the values behind it. */
    public ConfigReloadType reloadType() {
        return backingValues()
                .map(IConfigValue::reloadType)
                .max(Comparator.comparingInt(Enum::ordinal))
                .orElse(ConfigReloadType.NONE);
    }

    // one stored value for a simple row, several for a grouped one
    protected abstract Stream<IConfigValue<?>> backingValues();

    // some handles are plain suppliers we made up, so keep only the real stored values
    protected static Stream<IConfigValue<?>> storedValuesOf(Supplier<?>... handles) {
        return Arrays.stream(handles)
                .filter(h -> h instanceof IConfigValue)
                .map(h -> (IConfigValue<?>) h);
    }

    public abstract T get();

    public T defaultValue() {
        return defaultValue;
    }

    /** Writes an already checked value back to the config and saves it. */
    public abstract void apply(ModConfigHolder holder, Object value);

    public abstract static class SimpleConfigOption<T> extends ConfigOption<T> {

        protected final IConfigValue<T> handle;

        protected SimpleConfigOption(Component title, @Nullable Component description, IConfigValue<T> handle, T defaultValue) {
            super(title, description, defaultValue);
            this.handle = handle;
        }

        @Override
        public T get() {
            return handle.get();
        }

        // cast is safe, the value always comes from the widget bound to this row
        @Override
        @SuppressWarnings("unchecked")
        public void apply(ModConfigHolder holder, Object value) {
            holder.manuallySetValue(this.handle, (T) value);
        }

        @Override
        protected Stream<IConfigValue<?>> backingValues() {
            return Stream.of(this.handle);
        }

        @ApiStatus.Internal
        public IConfigValue<T> handle() {
            return this.handle;
        }
    }

    public static class BooleanValue extends SimpleConfigOption<Boolean> {
        private boolean feature;
        private List<BooleanValue> dependencies = List.of();

        public BooleanValue(Component title, @Nullable Component description, IConfigValue<Boolean> handle, Boolean defaultValue) {
            super(title, description, handle, defaultValue);
        }

        public boolean isFeature() {
            return feature;
        }

        @ApiStatus.Internal
        public void setFeature(boolean feature) {
            this.feature = feature;
        }

        /** Other features that must be on for this one to do anything, in the order they were declared. */
        public List<BooleanValue> dependencies() {
            return dependencies;
        }

        @ApiStatus.Internal
        public void addDependency(BooleanValue dependency) {
            var next = new ArrayList<>(this.dependencies);
            next.add(dependency);
            this.dependencies = List.copyOf(next);
        }
    }

    public static class IntValue extends SimpleConfigOption<Integer> {
        public final int min;
        public final int max;

        public IntValue(Component title, @Nullable Component description, IConfigValue<Integer> handle, Integer defaultValue, int min, int max) {
            super(title, description, handle, defaultValue);
            this.min = min;
            this.max = max;
        }
    }

    public static class IntSliderValue extends IntValue {
        public IntSliderValue(Component title, @Nullable Component description, IConfigValue<Integer> handle, Integer defaultValue, int min, int max) {
            super(title, description, handle, defaultValue, min, max);
        }
    }

    public static class DoubleValue extends SimpleConfigOption<Double> {
        public final double min;
        public final double max;

        public DoubleValue(Component title, @Nullable Component description, IConfigValue<Double> handle, Double defaultValue, double min, double max) {
            super(title, description, handle, defaultValue);
            this.min = min;
            this.max = max;
        }
    }

    public static class DoubleSliderValue extends DoubleValue {
        public DoubleSliderValue(Component title, @Nullable Component description, IConfigValue<Double> handle, Double defaultValue, double min, double max) {
            super(title, description, handle, defaultValue, min, max);
        }
    }

    /** A 0 to 1 double shown as a percentage slider. */
    public static class PercentValue extends DoubleValue {
        public PercentValue(Component title, @Nullable Component description, IConfigValue<Double> handle, Double defaultValue) {
            super(title, description, handle, defaultValue, 0.0, 1.0);
        }
    }

    public static class FloatValue extends SimpleConfigOption<Float> {
        public final float min;
        public final float max;

        public FloatValue(Component title, @Nullable Component description, IConfigValue<Float> handle, Float defaultValue, float min, float max) {
            super(title, description, handle, defaultValue);
            this.min = min;
            this.max = max;
        }
    }

    public static class FloatSliderValue extends FloatValue {
        public FloatSliderValue(Component title, @Nullable Component description, IConfigValue<Float> handle, Float defaultValue, float min, float max) {
            super(title, description, handle, defaultValue, min, max);
        }
    }

    public static class EnumValue<E extends Enum<E>> extends SimpleConfigOption<E> {
        public final E[] options;

        public EnumValue(Component title, @Nullable Component description, IConfigValue<E> handle, E defaultValue, E[] options) {
            super(title, description, handle, defaultValue);
            this.options = options;
        }
    }

    public static class StringValue extends SimpleConfigOption<String> {
        @Nullable
        public final Predicate<Object> validator;

        public StringValue(Component title, @Nullable Component description, IConfigValue<String> handle, String defaultValue, @Nullable Predicate<Object> validator) {
            super(title, description, handle, defaultValue);
            this.validator = validator;
        }

        public boolean isValid(String value) {
            return validator == null || validator.test(value);
        }
    }

    public static class RegexValue extends StringValue {
        public RegexValue(Component title, @Nullable Component description, IConfigValue<String> handle, String defaultValue) {
            super(title, description, handle, defaultValue, o -> o instanceof String s && isValidRegex(s));
        }

        public static boolean isValidRegex(String s) {
            try {
                Pattern.compile(s);
                return true;
            } catch (Exception e) {
                return false;
            }
        }
    }

    public static class DropdownValue extends SimpleConfigOption<String> {
        public final Supplier<List<String>> options;
        @Nullable
        public final Function<String, ItemStack> icon;

        public DropdownValue(Component title, @Nullable Component description, IConfigValue<String> handle,
                             String defaultValue, Supplier<List<String>> options,
                             @Nullable Function<String, ItemStack> icon) {
            super(title, description, handle, defaultValue);
            this.options = options;
            this.icon = icon;
        }
    }

    public static class ColorValue extends SimpleConfigOption<Integer> {
        public final boolean hasAlpha;

        public ColorValue(Component title, @Nullable Component description, IConfigValue<Integer> handle, Integer defaultValue) {
            this(title, description, handle, defaultValue, true);
        }

        public ColorValue(Component title, @Nullable Component description, IConfigValue<Integer> handle,
                          Integer defaultValue, boolean hasAlpha) {
            super(title, description, handle, defaultValue);
            this.hasAlpha = hasAlpha;
        }
    }

    public static class RangeValue extends ConfigOption<Range> {
        public final Supplier<Double> minHandle;
        public final Supplier<Double> maxHandle;
        public final double min;
        public final double max;

        public RangeValue(Component title, @Nullable Component description, Supplier<Double> minHandle,
                          Supplier<Double> maxHandle, Range defaultValue, double min, double max) {
            super(title, description, defaultValue);
            this.minHandle = minHandle;
            this.maxHandle = maxHandle;
            this.min = min;
            this.max = max;
        }

        @Override
        public Range get() {
            return new Range(minHandle.get(), maxHandle.get());
        }

        @Override
        public void apply(ModConfigHolder holder, Object value) {
            Range range = (Range) value;
            holder.manuallySetValue(minHandle, range.min());
            holder.manuallySetValue(maxHandle, range.max());
        }

        @Override
        protected Stream<IConfigValue<?>> backingValues() {
            return storedValuesOf(minHandle, maxHandle);
        }
    }

    public static class Vec3Value extends ConfigOption<Vec3> {
        public final Supplier<Double> xHandle;
        public final Supplier<Double> yHandle;
        public final Supplier<Double> zHandle;
        public final double min;
        public final double max;

        public Vec3Value(Component title, @Nullable Component description, Supplier<Double> xHandle,
                         Supplier<Double> yHandle, Supplier<Double> zHandle, Vec3 defaultValue, double min, double max) {
            super(title, description, defaultValue);
            this.xHandle = xHandle;
            this.yHandle = yHandle;
            this.zHandle = zHandle;
            this.min = min;
            this.max = max;
        }

        @Override
        public Vec3 get() {
            return new Vec3(xHandle.get(), yHandle.get(), zHandle.get());
        }

        @Override
        public void apply(ModConfigHolder holder, Object value) {
            Vec3 v = (Vec3) value;
            holder.manuallySetValue(xHandle, v.x);
            holder.manuallySetValue(yHandle, v.y);
            holder.manuallySetValue(zHandle, v.z);
        }

        @Override
        protected Stream<IConfigValue<?>> backingValues() {
            return storedValuesOf(xHandle, yHandle, zHandle);
        }
    }

    public static class Vec3iValue extends ConfigOption<Vec3i> {
        public final Supplier<Integer> xHandle;
        public final Supplier<Integer> yHandle;
        public final Supplier<Integer> zHandle;
        public final int min;
        public final int max;

        public Vec3iValue(Component title, @Nullable Component description, Supplier<Integer> xHandle,
                          Supplier<Integer> yHandle, Supplier<Integer> zHandle, Vec3i defaultValue, int min, int max) {
            super(title, description, defaultValue);
            this.xHandle = xHandle;
            this.yHandle = yHandle;
            this.zHandle = zHandle;
            this.min = min;
            this.max = max;
        }

        @Override
        public Vec3i get() {
            return new Vec3i(xHandle.get(), yHandle.get(), zHandle.get());
        }

        @Override
        public void apply(ModConfigHolder holder, Object value) {
            Vec3i v = (Vec3i) value;
            holder.manuallySetValue(xHandle, v.getX());
            holder.manuallySetValue(yHandle, v.getY());
            holder.manuallySetValue(zHandle, v.getZ());
        }

        @Override
        protected Stream<IConfigValue<?>> backingValues() {
            return storedValuesOf(xHandle, yHandle, zHandle);
        }
    }

    public static class ListValue extends SimpleConfigOption<List<String>> {
        @Nullable
        public final Predicate<String> entryValidator;
        @Nullable
        public final Supplier<List<String>> options;
        @Nullable
        public final Function<String, ItemStack> icon;

        public ListValue(Component title, @Nullable Component description, IConfigValue<List<String>> handle,
                         List<String> defaultValue, @Nullable Predicate<String> entryValidator) {
            this(title, description, handle, defaultValue, entryValidator, null, null);
        }

        public ListValue(Component title, @Nullable Component description, IConfigValue<List<String>> handle,
                         List<String> defaultValue, @Nullable Predicate<String> entryValidator,
                         @Nullable Supplier<List<String>> options, @Nullable Function<String, ItemStack> icon) {
            super(title, description, handle, defaultValue);
            this.entryValidator = entryValidator;
            this.options = options;
            this.icon = icon;
        }

        public boolean isValidEntry(String entry) {
            return entryValidator == null || entryValidator.test(entry);
        }
    }

    public static class JsonValue extends ConfigOption<String> {
        public static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

        private final Supplier<JsonElement> json;

        // on NeoForge the spec can't be read at define time, never call json.get() here
        public JsonValue(Component title, @Nullable Component description, Supplier<JsonElement> json) {
            super(title, description, null);
            this.json = json;
        }

        @Override
        public String get() {
            return GSON.toJson(json.get());
        }

        @Override
        public String defaultValue() {
            return GSON.toJson(json.get());
        }

        @Override
        public void apply(ModConfigHolder holder, Object value) {
            holder.manuallySetValue(json, JsonParser.parseString((String) value));
        }

        @Override
        protected Stream<IConfigValue<?>> backingValues() {
            return storedValuesOf(json);
        }
    }

    public static class SchemaValue<T> extends ConfigOption<T> {
        private final IConfigValue<T> handle;
        private final Supplier<T> lazyDefault;
        public final SchemaCodec<T> codec;

        public SchemaValue(Component title, @Nullable Component description, IConfigValue<T> handle,
                           Supplier<T> lazyDefault, SchemaCodec<T> codec) {
            super(title, description, null);
            this.handle = handle;
            this.lazyDefault = lazyDefault;
            this.codec = codec;
        }

        public Schema<T> schema() {
            return codec.schema();
        }

        @Override
        public T get() {
            return handle.get();
        }

        @Override
        public T defaultValue() {
            return lazyDefault.get();
        }

        @Override
        @SuppressWarnings("unchecked")
        public void apply(ModConfigHolder holder, Object value) {
            holder.manuallySetValue(this.handle, (T) value);
        }

        @Override
        protected Stream<IConfigValue<?>> backingValues() {
            return Stream.of(this.handle);
        }
    }

    public static class UnsupportedValue extends ConfigOption<Object> {
        private final Supplier<Object> handle;

        public UnsupportedValue(Component title, @Nullable Component description, Supplier<Object> handle) {
            super(title, description, null);
            this.handle = handle;
        }

        @Override
        public Object get() {
            return handle.get();
        }

        @Override
        public void apply(ModConfigHolder holder, Object value) {
        }

        @Override
        protected Stream<IConfigValue<?>> backingValues() {
            return storedValuesOf(handle);
        }
    }
}

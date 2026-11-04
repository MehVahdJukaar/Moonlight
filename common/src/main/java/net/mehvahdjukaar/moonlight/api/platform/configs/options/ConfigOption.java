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
import net.mehvahdjukaar.moonlight.api.util.math.Vec2i;
import net.minecraft.core.Vec3i;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.time.LocalTime;
import java.time.Month;
import java.time.MonthDay;
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

    public static class BooleanOption extends SimpleConfigOption<Boolean> {
        private boolean feature;
        private List<BooleanOption> dependencies = List.of();

        public BooleanOption(Component title, @Nullable Component description, IConfigValue<Boolean> handle, Boolean defaultValue) {
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
        public List<BooleanOption> dependencies() {
            return dependencies;
        }

        @ApiStatus.Internal
        public void addDependency(BooleanOption dependency) {
            var next = new ArrayList<>(this.dependencies);
            next.add(dependency);
            this.dependencies = List.copyOf(next);
        }
    }

    public static class IntOption extends SimpleConfigOption<Integer> {
        public final int min;
        public final int max;

        public IntOption(Component title, @Nullable Component description, IConfigValue<Integer> handle, Integer defaultValue, int min, int max) {
            super(title, description, handle, defaultValue);
            this.min = min;
            this.max = max;
        }
    }

    public static class IntSliderOption extends IntOption {
        public IntSliderOption(Component title, @Nullable Component description, IConfigValue<Integer> handle, Integer defaultValue, int min, int max) {
            super(title, description, handle, defaultValue, min, max);
        }
    }

    public static class DoubleOption extends SimpleConfigOption<Double> {
        public final double min;
        public final double max;

        public DoubleOption(Component title, @Nullable Component description, IConfigValue<Double> handle, Double defaultValue, double min, double max) {
            super(title, description, handle, defaultValue);
            this.min = min;
            this.max = max;
        }
    }

    public static class DoubleSliderOption extends DoubleOption {
        public DoubleSliderOption(Component title, @Nullable Component description, IConfigValue<Double> handle, Double defaultValue, double min, double max) {
            super(title, description, handle, defaultValue, min, max);
        }
    }

    /** A 0 to 1 double shown as a percentage slider. */
    public static class PercentOption extends DoubleOption {
        public PercentOption(Component title, @Nullable Component description, IConfigValue<Double> handle, Double defaultValue) {
            super(title, description, handle, defaultValue, 0.0, 1.0);
        }
    }

    public static class FloatOption extends SimpleConfigOption<Float> {
        public final float min;
        public final float max;

        public FloatOption(Component title, @Nullable Component description, IConfigValue<Float> handle, Float defaultValue, float min, float max) {
            super(title, description, handle, defaultValue);
            this.min = min;
            this.max = max;
        }
    }

    public static class FloatSliderOption extends FloatOption {
        public FloatSliderOption(Component title, @Nullable Component description, IConfigValue<Float> handle, Float defaultValue, float min, float max) {
            super(title, description, handle, defaultValue, min, max);
        }
    }

    public static class EnumOption<E extends Enum<E>> extends SimpleConfigOption<E> {
        public final E[] options;

        public EnumOption(Component title, @Nullable Component description, IConfigValue<E> handle, E defaultValue, E[] options) {
            super(title, description, handle, defaultValue);
            this.options = options;
        }
    }

    public static class StringOption extends SimpleConfigOption<String> {
        @Nullable
        public final Predicate<Object> validator;

        public StringOption(Component title, @Nullable Component description, IConfigValue<String> handle, String defaultValue, @Nullable Predicate<Object> validator) {
            super(title, description, handle, defaultValue);
            this.validator = validator;
        }

        public boolean isValid(String value) {
            return validator == null || validator.test(value);
        }
    }

    public static class RegexOption extends StringOption {
        public RegexOption(Component title, @Nullable Component description, IConfigValue<String> handle, String defaultValue) {
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

    public static class DropdownOption extends SimpleConfigOption<String> {
        public final Supplier<List<String>> options;
        @Nullable
        public final Function<String, ItemStack> icon;

        public DropdownOption(Component title, @Nullable Component description, IConfigValue<String> handle,
                              String defaultValue, Supplier<List<String>> options,
                              @Nullable Function<String, ItemStack> icon) {
            super(title, description, handle, defaultValue);
            this.options = options;
            this.icon = icon;
        }
    }

    public static class ColorOption extends SimpleConfigOption<Integer> {
        public final boolean hasAlpha;

        public ColorOption(Component title, @Nullable Component description, IConfigValue<Integer> handle, Integer defaultValue) {
            this(title, description, handle, defaultValue, true);
        }

        public ColorOption(Component title, @Nullable Component description, IConfigValue<Integer> handle,
                           Integer defaultValue, boolean hasAlpha) {
            super(title, description, handle, defaultValue);
            this.hasAlpha = hasAlpha;
        }
    }

    public static class RangeOption extends ConfigOption<Range> {
        public final Supplier<Double> minHandle;
        public final Supplier<Double> maxHandle;
        public final double min;
        public final double max;

        public RangeOption(Component title, @Nullable Component description, Supplier<Double> minHandle,
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

    public static class Vec2Option extends ConfigOption<Vec2> {
        public final Supplier<Float> xHandle;
        public final Supplier<Float> yHandle;
        public final float min;
        public final float max;

        public Vec2Option(Component title, @Nullable Component description, Supplier<Float> xHandle,
                          Supplier<Float> yHandle, Vec2 defaultValue, float min, float max) {
            super(title, description, defaultValue);
            this.xHandle = xHandle;
            this.yHandle = yHandle;
            this.min = min;
            this.max = max;
        }

        @Override
        public Vec2 get() {
            return new Vec2(xHandle.get(), yHandle.get());
        }

        @Override
        public void apply(ModConfigHolder holder, Object value) {
            Vec2 v = (Vec2) value;
            holder.manuallySetValue(xHandle, v.x);
            holder.manuallySetValue(yHandle, v.y);
        }

        @Override
        protected Stream<IConfigValue<?>> backingValues() {
            return storedValuesOf(xHandle, yHandle);
        }
    }

    public static class Vec2iOption extends ConfigOption<Vec2i> {
        public final Supplier<Integer> xHandle;
        public final Supplier<Integer> yHandle;
        public final int min;
        public final int max;

        public Vec2iOption(Component title, @Nullable Component description, Supplier<Integer> xHandle,
                           Supplier<Integer> yHandle, Vec2i defaultValue, int min, int max) {
            super(title, description, defaultValue);
            this.xHandle = xHandle;
            this.yHandle = yHandle;
            this.min = min;
            this.max = max;
        }

        @Override
        public Vec2i get() {
            return new Vec2i(xHandle.get(), yHandle.get());
        }

        @Override
        public void apply(ModConfigHolder holder, Object value) {
            Vec2i v = (Vec2i) value;
            holder.manuallySetValue(xHandle, v.x());
            holder.manuallySetValue(yHandle, v.y());
        }

        @Override
        protected Stream<IConfigValue<?>> backingValues() {
            return storedValuesOf(xHandle, yHandle);
        }
    }

    public static class Vec3Option extends ConfigOption<Vec3> {
        public final Supplier<Double> xHandle;
        public final Supplier<Double> yHandle;
        public final Supplier<Double> zHandle;
        public final double min;
        public final double max;

        public Vec3Option(Component title, @Nullable Component description, Supplier<Double> xHandle,
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

    public static class Vec3iOption extends ConfigOption<Vec3i> {
        public final Supplier<Integer> xHandle;
        public final Supplier<Integer> yHandle;
        public final Supplier<Integer> zHandle;
        public final int min;
        public final int max;

        public Vec3iOption(Component title, @Nullable Component description, Supplier<Integer> xHandle,
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

    public static class DateOption extends ConfigOption<MonthDay> {
        public final Supplier<Integer> monthHandle;
        public final Supplier<Integer> dayHandle;

        public DateOption(Component title, @Nullable Component description, Supplier<Integer> monthHandle,
                          Supplier<Integer> dayHandle, MonthDay defaultValue) {
            super(title, description, defaultValue);
            this.monthHandle = monthHandle;
            this.dayHandle = dayHandle;
        }

        public static MonthDay of(int month, int day) {
            Month m = Month.of(Math.clamp(month, 1, 12));
            return MonthDay.of(m, Math.clamp(day, 1, m.maxLength()));
        }

        @Override
        public MonthDay get() {
            return of(monthHandle.get(), dayHandle.get());
        }

        @Override
        public void apply(ModConfigHolder holder, Object value) {
            MonthDay d = (MonthDay) value;
            holder.manuallySetValue(monthHandle, d.getMonthValue());
            holder.manuallySetValue(dayHandle, d.getDayOfMonth());
        }

        @Override
        protected Stream<IConfigValue<?>> backingValues() {
            return storedValuesOf(monthHandle, dayHandle);
        }
    }

    public static class TimeOption extends ConfigOption<LocalTime> {
        public final Supplier<Integer> hourHandle;
        public final Supplier<Integer> minuteHandle;

        public TimeOption(Component title, @Nullable Component description, Supplier<Integer> hourHandle,
                          Supplier<Integer> minuteHandle, LocalTime defaultValue) {
            super(title, description, defaultValue);
            this.hourHandle = hourHandle;
            this.minuteHandle = minuteHandle;
        }

        public static LocalTime of(int hour, int minute) {
            return LocalTime.of(Math.clamp(hour, 0, 23), Math.clamp(minute, 0, 59));
        }

        @Override
        public LocalTime get() {
            return of(hourHandle.get(), minuteHandle.get());
        }

        @Override
        public void apply(ModConfigHolder holder, Object value) {
            LocalTime t = (LocalTime) value;
            holder.manuallySetValue(hourHandle, t.getHour());
            holder.manuallySetValue(minuteHandle, t.getMinute());
        }

        @Override
        protected Stream<IConfigValue<?>> backingValues() {
            return storedValuesOf(hourHandle, minuteHandle);
        }
    }

    public static class ListOption extends SimpleConfigOption<List<String>> {
        @Nullable
        public final Predicate<String> entryValidator;
        @Nullable
        public final Supplier<List<String>> options;
        @Nullable
        public final Function<String, ItemStack> icon;

        public ListOption(Component title, @Nullable Component description, IConfigValue<List<String>> handle,
                          List<String> defaultValue, @Nullable Predicate<String> entryValidator) {
            this(title, description, handle, defaultValue, entryValidator, null, null);
        }

        public ListOption(Component title, @Nullable Component description, IConfigValue<List<String>> handle,
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

    /** A list of hex color strings, edited with a color field per entry. */
    public static class ColorListOption extends ListOption {
        public final boolean hasAlpha;

        public ColorListOption(Component title, @Nullable Component description, IConfigValue<List<String>> handle,
                               List<String> defaultValue, @Nullable Predicate<String> entryValidator, boolean hasAlpha) {
            super(title, description, handle, defaultValue, entryValidator);
            this.hasAlpha = hasAlpha;
        }
    }

    public static class JsonOption extends ConfigOption<String> {
        public static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

        private final Supplier<JsonElement> json;

        // on NeoForge the spec can't be read at define time, never call json.get() here
        public JsonOption(Component title, @Nullable Component description, Supplier<JsonElement> json) {
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

    public static class SchemaOption<T> extends SimpleConfigOption<T> {
        private final Supplier<T> lazyDefault;
        public final SchemaCodec<T> codec;

        public SchemaOption(Component title, @Nullable Component description, IConfigValue<T> handle,
                            Supplier<T> lazyDefault, SchemaCodec<T> codec) {
            super(title, description, handle, null);
            this.lazyDefault = lazyDefault;
            this.codec = codec;
        }

        public Schema<T> schema() {
            return codec.schema();
        }

        @Override
        public T defaultValue() {
            return lazyDefault.get();
        }
    }

    public static class UnsupportedOption extends ConfigOption<Object> {
        private final Supplier<Object> handle;

        public UnsupportedOption(Component title, @Nullable Component description, Supplier<Object> handle) {
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

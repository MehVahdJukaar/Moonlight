package net.mehvahdjukaar.moonlight.api.platform.configs.options;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import net.mehvahdjukaar.moonlight.api.platform.configs.ModConfigHolder;
import net.mehvahdjukaar.moonlight.api.platform.configs.WritableConfigValue;
import net.mehvahdjukaar.moonlight.api.util.math.Range;
import net.minecraft.core.Vec3i;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * An editable leaf config value in the loader independent screen model. Most kinds are backed by a single writable
 * value of the same type {@code T} and so extend {@link SimpleConfigOption}; the compound kinds (range/vec3/json) sit on
 * top of several backing leaves and extend this class directly, implementing {@link #get()}/{@link #apply}/
 * {@link #backingMeta()} from those leaves. This is the single loader independent bridge the whole screen is built on.
 *
 * @param <T> the value type
 */
public abstract class ConfigOption<T> extends ConfigNode {

    protected final T defaultValue;

    protected ConfigOption(Component title, @Nullable Component description, T defaultValue) {
        super(title, description);
        this.defaultValue = defaultValue;
    }

    /**
     * How a change to this value takes effect (drives the reload/restart icon on its screen row). Derived from the
     * backing leaf value(s), which are the source of truth: a grouped row (range/vec3) reports the highest-severity
     * reload among its members — the aggregate of the bunch wins.
     */
    public ConfigReloadType reloadType() {
        return backingMeta()
                .map(WritableConfigValue::reloadType)
                .max(Comparator.comparingInt(Enum::ordinal))
                .orElse(ConfigReloadType.NONE);
    }

    /** The backing leaf value(s), read for their change metadata: one for a leaf row, several for a grouped one. */
    protected abstract Stream<WritableConfigValue<?>> backingMeta();

    /** Picks the {@link WritableConfigValue} leaves out of the given backing handles (a handle may be synthetic). */
    protected static Stream<WritableConfigValue<?>> metaOf(Supplier<?>... handles) {
        return Arrays.stream(handles)
                .filter(h -> h instanceof WritableConfigValue)
                .map(h -> (WritableConfigValue<?>) h);
    }

    /**
     * The currently saved value.
     */
    public abstract T get();

    public T defaultValue() {
        return defaultValue;
    }

    /**
     * Writes the given (already validated) value back to the underlying config and saves it.
     */
    public abstract void apply(ModConfigHolder holder, Object value);

    /**
     * The common case: an option backed by a single writable leaf of the same type {@code T}. Reading, writing and
     * change metadata all go straight through that one {@link WritableConfigValue} — the very object {@code define(...)}
     * returned (a {@code ConfigValue} on Fabric, a {@code ValueWrapper} on NeoForge). The compound kinds instead sit on
     * top of several leaves and extend {@link ConfigOption} directly.
     */
    public abstract static class SimpleConfigOption<T> extends ConfigOption<T> {

        protected final WritableConfigValue<T> handle;

        protected SimpleConfigOption(Component title, @Nullable Component description, WritableConfigValue<T> handle, T defaultValue) {
            super(title, description, defaultValue);
            this.handle = handle;
        }

        @Override
        public T get() {
            return handle.get();
        }

        /**
         * The cast is safe because {@code value} always originates from a control bound to this entry.
         */
        @Override
        @SuppressWarnings("unchecked")
        public void apply(ModConfigHolder holder, Object value) {
            holder.manuallySetValue(this.handle, (T) value);
        }

        @Override
        protected Stream<WritableConfigValue<?>> backingMeta() {
            return Stream.of(this.handle);
        }
    }

    // ===== concrete value kinds =====

    public static class BooleanValue extends SimpleConfigOption<Boolean> {
        public BooleanValue(Component title, @Nullable Component description, WritableConfigValue<Boolean> handle, Boolean defaultValue) {
            super(title, description, handle, defaultValue);
        }
    }

    public static class IntValue extends SimpleConfigOption<Integer> {
        public final int min;
        public final int max;

        public IntValue(Component title, @Nullable Component description, WritableConfigValue<Integer> handle, Integer defaultValue, int min, int max) {
            super(title, description, handle, defaultValue);
            this.min = min;
            this.max = max;
        }
    }

    /**
     * An int drawn as a slider instead of a text field. The type itself is the "draw me as a slider" signal:
     * the control registry keys on the exact class, so no separate style flag is needed.
     */
    public static class IntSliderValue extends IntValue {
        public IntSliderValue(Component title, @Nullable Component description, WritableConfigValue<Integer> handle, Integer defaultValue, int min, int max) {
            super(title, description, handle, defaultValue, min, max);
        }
    }

    public static class DoubleValue extends SimpleConfigOption<Double> {
        public final double min;
        public final double max;

        public DoubleValue(Component title, @Nullable Component description, WritableConfigValue<Double> handle, Double defaultValue, double min, double max) {
            super(title, description, handle, defaultValue);
            this.min = min;
            this.max = max;
        }
    }

    /** A double drawn as a slider instead of a text field. See {@link IntSliderValue}. */
    public static class DoubleSliderValue extends DoubleValue {
        public DoubleSliderValue(Component title, @Nullable Component description, WritableConfigValue<Double> handle, Double defaultValue, double min, double max) {
            super(title, description, handle, defaultValue, min, max);
        }
    }

    /** A {@code [0, 1]} double drawn as a slider that displays a percentage. */
    public static class PercentValue extends DoubleValue {
        public PercentValue(Component title, @Nullable Component description, WritableConfigValue<Double> handle, Double defaultValue) {
            super(title, description, handle, defaultValue, 0.0, 1.0);
        }
    }

    public static class FloatValue extends SimpleConfigOption<Float> {
        public final float min;
        public final float max;

        public FloatValue(Component title, @Nullable Component description, WritableConfigValue<Float> handle, Float defaultValue, float min, float max) {
            super(title, description, handle, defaultValue);
            this.min = min;
            this.max = max;
        }
    }

    public static class FloatSliderValue extends FloatValue {
        public FloatSliderValue(Component title, @Nullable Component description, WritableConfigValue<Float> handle, Float defaultValue, float min, float max) {
            super(title, description, handle, defaultValue, min, max);
        }
    }

    public static class EnumValue<E extends Enum<E>> extends SimpleConfigOption<E> {
        public final E[] options;

        public EnumValue(Component title, @Nullable Component description, WritableConfigValue<E> handle, E defaultValue, E[] options) {
            super(title, description, handle, defaultValue);
            this.options = options;
        }
    }

    public static class StringValue extends SimpleConfigOption<String> {
        @Nullable
        public final Predicate<Object> validator;

        public StringValue(Component title, @Nullable Component description, WritableConfigValue<String> handle, String defaultValue, @Nullable Predicate<Object> validator) {
            super(title, description, handle, defaultValue);
            this.validator = validator;
        }

        public boolean isValid(String value) {
            return validator == null || validator.test(value);
        }
    }

    public static class RegexValue extends StringValue {
        public RegexValue(Component title, @Nullable Component description, WritableConfigValue<String> handle, String defaultValue) {
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

        public DropdownValue(Component title, @Nullable Component description, WritableConfigValue<String> handle,
                             String defaultValue, Supplier<List<String>> options,
                             @Nullable Function<String, ItemStack> icon) {
            super(title, description, handle, defaultValue);
            this.options = options;
            this.icon = icon;
        }
    }

    public static class ColorValue extends SimpleConfigOption<Integer> {
        public ColorValue(Component title, @Nullable Component description, WritableConfigValue<Integer> handle, Integer defaultValue) {
            super(title, description, handle, defaultValue);
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
        protected Stream<WritableConfigValue<?>> backingMeta() {
            return metaOf(minHandle, maxHandle);
        }
    }

    /**
     * A {@link Vec3} value: three backing double config values ({@code x}/{@code y}/{@code z}) presented and edited
     * as one row of number fields. {@code min}/{@code max} are the shared accepted bounds of each component.
     * Like {@link RangeValue}, writing goes through all three handles at once.
     */
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
        protected Stream<WritableConfigValue<?>> backingMeta() {
            return metaOf(xHandle, yHandle, zHandle);
        }
    }

    /**
     * A {@link Vec3i} value: three backing int config values ({@code x}/{@code y}/{@code z}) presented and edited as
     * one row of number fields. The integer counterpart of {@link Vec3Value}.
     */
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
        protected Stream<WritableConfigValue<?>> backingMeta() {
            return metaOf(xHandle, yHandle, zHandle);
        }
    }

    /**
     * A list of strings, edited on a dedicated add/remove page. {@code entryValidator}, when present, marks
     * individual entries valid/invalid in the editor. When {@code options} is present each entry is picked with a
     * dropdown (with an optional {@code icon}, as in {@link DropdownValue}) instead of typed as free text.
     */
    public static class ListValue extends SimpleConfigOption<List<String>> {
        @Nullable
        public final Predicate<String> entryValidator;
        @Nullable
        public final Supplier<List<String>> options;
        @Nullable
        public final Function<String, ItemStack> icon;

        public ListValue(Component title, @Nullable Component description, WritableConfigValue<List<String>> handle,
                         List<String> defaultValue, @Nullable Predicate<String> entryValidator) {
            this(title, description, handle, defaultValue, entryValidator, null, null);
        }

        public ListValue(Component title, @Nullable Component description, WritableConfigValue<List<String>> handle,
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

    /**
     * A JSON-backed value (raw json or a reflection-serialized bean), edited as pretty-printed JSON text on a
     * dedicated page. The screen edits it as a {@code String}; {@link #apply} parses that string back into a
     * {@link JsonElement} and writes it through the underlying {@code Supplier<JsonElement>} handle. Both loaders
     * expose such a handle, so this stays loader independent.
     */
    public static class JsonValue extends ConfigOption<String> {
        public static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

        private final Supplier<JsonElement> json;

        // handle/default stay lazy: on NeoForge the spec can't be read at define time, so never call json.get() here
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
        protected Stream<WritableConfigValue<?>> backingMeta() {
            return metaOf(json); // the real leaf is the json handle, not the synthetic string handle
        }
    }

    /**
     * A value the screen can't yet edit (codec objects). Shown as a disabled placeholder row telling the user to
     * edit the file manually.
     */
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
            // not editable
        }

        @Override
        protected Stream<WritableConfigValue<?>> backingMeta() {
            return metaOf(handle);
        }
    }
}

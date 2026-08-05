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
 * value of the same type and extend {@link SimpleConfigOption}; the compound kinds (range/vec3/json) sit on top of
 * several backing leaves and extend this class directly.
 */
public abstract class ConfigOption<T> extends ConfigNode {

    protected final T defaultValue;

    protected ConfigOption(Component title, @Nullable Component description, T defaultValue) {
        super(title, description);
        this.defaultValue = defaultValue;
    }

    /**
     * How a change to this value takes effect. A grouped row (range/vec3) reports the highest severity among its
     * backing leaves.
     */
    public ConfigReloadType reloadType() {
        return backingMeta()
                .map(IConfigValue::reloadType)
                .max(Comparator.comparingInt(Enum::ordinal))
                .orElse(ConfigReloadType.NONE);
    }

    /** The backing leaf value(s): one for a leaf row, several for a grouped one. */
    protected abstract Stream<IConfigValue<?>> backingMeta();

    // handles may be synthetic suppliers, so only the real leaves are kept
    protected static Stream<IConfigValue<?>> metaOf(Supplier<?>... handles) {
        return Arrays.stream(handles)
                .filter(h -> h instanceof IConfigValue)
                .map(h -> (IConfigValue<?>) h);
    }

    /** The currently saved value. */
    public abstract T get();

    public T defaultValue() {
        return defaultValue;
    }

    /** Writes the given (already validated) value back to the underlying config and saves it. */
    public abstract void apply(ModConfigHolder holder, Object value);

    /**
     * An option backed by a single writable leaf of the same type. Reading, writing and change metadata all go
     * straight through the object {@code define(...)} returned (a {@code ConfigValue} on Fabric, a
     * {@code ValueWrapper} on NeoForge).
     */
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

        // cast is safe: value always originates from a control bound to this entry
        @Override
        @SuppressWarnings("unchecked")
        public void apply(ModConfigHolder holder, Object value) {
            holder.manuallySetValue(this.handle, (T) value);
        }

        @Override
        protected Stream<IConfigValue<?>> backingMeta() {
            return Stream.of(this.handle);
        }
    }

    // ===== concrete value kinds =====

    public static class BooleanValue extends SimpleConfigOption<Boolean> {
        // a "feature" boolean renders as the ✓/✗ toggle instead of the plain ON/OFF button. A category gate is drawn
        // the same way but keyed off the owning category's gate() rather than this flag
        private boolean feature;

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

    /** An int drawn as a slider instead of a text field. The control registry keys on the exact class. */
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

    /** A double drawn as a slider instead of a text field. See {@link IntSliderValue}. */
    public static class DoubleSliderValue extends DoubleValue {
        public DoubleSliderValue(Component title, @Nullable Component description, IConfigValue<Double> handle, Double defaultValue, double min, double max) {
            super(title, description, handle, defaultValue, min, max);
        }
    }

    /** A {@code [0, 1]} double drawn as a slider that displays a percentage. */
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
        /** When false the color has no alpha channel: it's edited and stored as plain RGB. */
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
        protected Stream<IConfigValue<?>> backingMeta() {
            return metaOf(minHandle, maxHandle);
        }
    }

    /** Three backing double values presented as one row of number fields. Bounds are shared by all components. */
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
        protected Stream<IConfigValue<?>> backingMeta() {
            return metaOf(xHandle, yHandle, zHandle);
        }
    }

    /** Integer counterpart of {@link Vec3Value}. */
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
        protected Stream<IConfigValue<?>> backingMeta() {
            return metaOf(xHandle, yHandle, zHandle);
        }
    }

    /**
     * A list of strings, edited on a dedicated add/remove page. With {@code options} present each entry is picked
     * from a dropdown instead of typed as free text.
     */
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

    /**
     * A JSON-backed value (raw json or a reflection-serialized bean), edited as pretty-printed text on a dedicated
     * page. The screen sees a {@code String}; {@link #apply} parses it back into a {@link JsonElement}.
     */
    public static class JsonValue extends ConfigOption<String> {
        public static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

        private final Supplier<JsonElement> json;

        // stays lazy: on NeoForge the spec can't be read at define time, so never call json.get() here
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
        protected Stream<IConfigValue<?>> backingMeta() {
            return metaOf(json);
        }
    }

    /**
     * A codec-backed object value that can be edited, since it carries a {@link SchemaCodec}: its row opens a form
     * generated from the schema instead of the "edit manually" placeholder. The wire format is unchanged.
     */
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
        protected Stream<IConfigValue<?>> backingMeta() {
            return Stream.of(this.handle);
        }
    }

    /** A value the screen can't edit. Shown as a disabled row telling the user to edit the file manually. */
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
        protected Stream<IConfigValue<?>> backingMeta() {
            return metaOf(handle);
        }
    }
}

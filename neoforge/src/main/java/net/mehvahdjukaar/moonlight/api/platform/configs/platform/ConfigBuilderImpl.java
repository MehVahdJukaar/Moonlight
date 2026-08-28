package net.mehvahdjukaar.moonlight.api.platform.configs.platform;

import com.google.common.base.Suppliers;
import com.google.gson.JsonElement;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JavaOps;
import com.mojang.serialization.JsonOps;
import net.mehvahdjukaar.codecui.SchemaCodec;
import net.mehvahdjukaar.moonlight.api.platform.configs.ConfigBuilder;
import net.mehvahdjukaar.moonlight.api.platform.configs.ConfigMetadata;
import net.mehvahdjukaar.moonlight.api.platform.configs.ConfigType;
import net.mehvahdjukaar.moonlight.api.platform.configs.options.ConfigOption;
import net.mehvahdjukaar.moonlight.api.platform.configs.options.ConfigReloadType;
import net.minecraft.network.chat.Component;
import net.mehvahdjukaar.moonlight.api.util.math.ColorUtils;
import net.mehvahdjukaar.moonlight.core.CompatHandler;
import net.mehvahdjukaar.moonlight.core.databuddy.ConfigHelper;
import net.mehvahdjukaar.moonlight.platform.ConfigHacks;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.common.ModConfigSpec;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import net.minecraft.world.item.ItemStack;

public class ConfigBuilderImpl extends ConfigBuilder {

    private final List<TrackedConfigValue<?>> trackedValues = new ArrayList<>();

    public static ConfigBuilder create(Identifier name, ConfigType type) {
        return new ConfigBuilderImpl(name, type);
    }

    private final ModConfigSpec.Builder builder;
    private final Deque<String> categoryStack = new ArrayDeque<>();

    public ConfigBuilderImpl(Identifier name, ConfigType type) {
        super(name, type);
        this.builder = new ModConfigSpec.Builder();
        ConfigHacks.init();
    }

    private void addUiRow(String name, ConfigOption<?> value) {
        recordOption(value);
        noteDefined(name, value, null); // .toml comments are handled in comment(); this only wires the row description
    }

    private Component uiTitle(String name) {
        return description(name);
    }

    @Nullable
    private Component uiDescription(String name) {
        String key = tooltipKey(name);
        return translations.containsKey(key) ? tooltip(name) : null;
    }

    @SuppressWarnings("unchecked")
    private ConfigOption.UnsupportedValue unsupported(String name, Supplier<?> handle) {
        return new ConfigOption.UnsupportedValue(uiTitle(name), uiDescription(name), (Supplier<Object>) handle);
    }

    // The flags stay set while a grouped value defines its hidden parts, so every piece of a range or vec3 gets the
    // same reload info
    private ConfigMetadata pendingMeta() {
        return new ConfigMetadata(this.pendingReload, this.pendingDynamicPacks);
    }

    private <T> T track(T value) {
        if (value instanceof TrackedConfigValue<?> trackedValue) {
            this.trackedValues.add(trackedValue);
        }
        return value;
    }

    @Override
    public String currentCategory() {
        return categoryStack.peekFirst();
    }

    @Nullable
    @Override
    public String parentCategory() {
        if (categoryStack.size() < 2) {
            return null;
        }
        var it = categoryStack.descendingIterator();
        it.next();
        return it.next();
    }

    @Override
    protected ForgeConfigHolder buildHolder() {
        return new ForgeConfigHolder(this.getName(), this.builder.build(), this.type,
                this.buildChangeCallback(), trackedValues, getUiRoot());
    }

    @Override
    public ConfigBuilderImpl push(String category) {
        builder.push(category);
        categoryStack.push(category);
        noteCategoryName(category);
        uiPush(Component.translatable(translationKey("")));
        return this;
    }

    @Override
    public ConfigBuilderImpl pop() {
        flushPendingComment(); // a trailing after-comment in this category has no following define to claim it
        builder.pop();
        categoryStack.pop();
        uiPop();
        return this;
    }

    @Override
    public Supplier<Boolean> define(String name, boolean defaultValue) {
        addTranslationsAndComments(name);
        var value = builder.define(name, defaultValue);
        var w = track(ForgeConfigValue.simple(value, pendingMeta()));
        addUiRow(name, new ConfigOption.BooleanValue(uiTitle(name), uiDescription(name), w, defaultValue));
        return w;
    }

    @Override
    public Supplier<Integer> define(String name, int defaultValue, int min, int max) {
        return defineInt(name, defaultValue, min, max, false);
    }

    @Override
    public Supplier<Integer> defineSlider(String name, int defaultValue, int min, int max) {
        return defineInt(name, defaultValue, min, max, true);
    }

    private Supplier<Integer> defineInt(String name, int defaultValue, int min, int max, boolean slider) {
        addTranslationsAndComments(name);
        var value = builder.defineInRange(name, defaultValue, min, max);
        var w = track(ForgeConfigValue.simple(value, pendingMeta()));
        addUiRow(name, slider
                ? new ConfigOption.IntSliderValue(uiTitle(name), uiDescription(name), w, defaultValue, min, max)
                : new ConfigOption.IntValue(uiTitle(name), uiDescription(name), w, defaultValue, min, max));
        return w;
    }

    @Override
    public Supplier<Double> define(String name, double defaultValue, double min, double max) {
        return defineDouble(name, defaultValue, min, max, false);
    }

    @Override
    public Supplier<Double> defineSlider(String name, double defaultValue, double min, double max) {
        return defineDouble(name, defaultValue, min, max, true);
    }

    private Supplier<Double> defineDouble(String name, double defaultValue, double min, double max, boolean slider) {
        addTranslationsAndComments(name);
        var value = builder.defineInRange(name, defaultValue, min, max);
        var w = track(ForgeConfigValue.simple(value, pendingMeta()));
        addUiRow(name, slider
                ? new ConfigOption.DoubleSliderValue(uiTitle(name), uiDescription(name), w, defaultValue, min, max)
                : new ConfigOption.DoubleValue(uiTitle(name), uiDescription(name), w, defaultValue, min, max));
        return w;
    }

    @Override
    public Supplier<Double> definePercentage(String name, double defaultValue) {
        addTranslationsAndComments(name);
        var value = builder.defineInRange(name, defaultValue, 0.0, 1.0);
        var w = track(ForgeConfigValue.simple(value, pendingMeta()));
        addUiRow(name, new ConfigOption.PercentValue(uiTitle(name), uiDescription(name), w, defaultValue));
        return w;
    }

    @ApiStatus.Experimental
    @Override
    public Supplier<Float> define(String name, float defaultValue, float min, float max) {
        return defineFloat(name, defaultValue, min, max, false);
    }

    @Override
    public Supplier<Float> defineSlider(String name, float defaultValue, float min, float max) {
        return defineFloat(name, defaultValue, min, max, true);
    }

    private Supplier<Float> defineFloat(String name, float defaultValue, float min, float max, boolean slider) {
        addTranslationsAndComments(name);
        var value = builder.defineInRange(name, defaultValue, min, max);
        var w = track(new ForgeConfigValue<Float, Double>(value, pendingMeta()) {
            @Override
            Float map(Double value) {
                return value.floatValue();
            }
            @Override
            Double unmap(Float value) {
                return (double) value;
            }
        });
        addUiRow(name, slider
                ? new ConfigOption.FloatSliderValue(uiTitle(name), uiDescription(name), w, defaultValue, min, max)
                : new ConfigOption.FloatValue(uiTitle(name), uiDescription(name), w, defaultValue, min, max));
        return w;
    }

    @Override
    public Supplier<Integer> defineColor(String name, int defaultValue, boolean hasAlpha) {
        addTranslationsAndComments(name);
        Codec<Integer> codec = ColorUtils.codec(hasAlpha);
        String def = (String) codec.encodeStart(JavaOps.INSTANCE, defaultValue).getOrThrow();
        var value = builder.define(name, def,
                o -> o instanceof String s && ColorUtils.isValidString(s));
        var w = track(ForgeConfigValue.fromString(value, codec, pendingMeta()));
        addUiRow(name, new ConfigOption.ColorValue(uiTitle(name), uiDescription(name), w, defaultValue, hasAlpha));
        return w;
    }

    @Override
    public Supplier<String> define(String name, String defaultValue, Predicate<Object> validator) {
        addTranslationsAndComments(name);
        var value = builder.define(name, defaultValue, validator);
        var w = track(ForgeConfigValue.simple(value, pendingMeta()));
        addUiRow(name, new ConfigOption.StringValue(uiTitle(name), uiDescription(name), w, defaultValue, validator));
        return w;
    }

    @Override
    protected Supplier<String> defineRegexInternal(String name, String defaultValue) {
        addTranslationsAndComments(name);
        var value = builder.define(name, defaultValue, ConfigBuilder.REGEX_CHECK);
        var w = track(ForgeConfigValue.simple(value, pendingMeta()));
        addUiRow(name, new ConfigOption.RegexValue(uiTitle(name), uiDescription(name), w, defaultValue));
        return w;
    }

    @Override
    protected Supplier<String> defineChoiceInternal(String name, String defaultValue, Predicate<Object> validator,
                                                    Supplier<List<String>> options, Function<String, ItemStack> icon) {
        addTranslationsAndComments(name);
        var value = builder.define(name, defaultValue, validator);
        var w = track(ForgeConfigValue.simple(value, pendingMeta()));
        addUiRow(name, new ConfigOption.DropdownValue(uiTitle(name), uiDescription(name), w, defaultValue, options, icon));
        return w;
    }

    public <T> Supplier<T> define(String name, Supplier<T> defaultValue, Predicate<Object> validator) {
        addTranslationsAndComments(name);
        var value = builder.define(name, defaultValue, validator);
        var w = track(ForgeConfigValue.simple(value, pendingMeta()));
        addUiRow(name, unsupported(name, w));
        return w;
    }

    @Override
    public <T extends String> Supplier<List<String>> define(String name, List<? extends T> defaultValue, Predicate<Object> predicate) {
        addTranslationsAndComments(name);
        var value = builder.defineList(name, defaultValue, predicate);
        @SuppressWarnings("unchecked")
        ModConfigSpec.ConfigValue<List<String>> listValue = (ModConfigSpec.ConfigValue<List<String>>) (ModConfigSpec.ConfigValue<?>) value;
        var w = track(ForgeConfigValue.simple(listValue, pendingMeta()));
        addUiRow(name, new ConfigOption.ListValue(uiTitle(name), uiDescription(name), w, List.copyOf(defaultValue),
                s -> predicate.test(s)));
        return w;
    }

    @Override
    protected Supplier<List<String>> defineListInternal(String name, List<String> defaultValue, Predicate<Object> entryValidator,
                                                        Supplier<List<String>> options, Function<String, ItemStack> icon) {
        addTranslationsAndComments(name);
        var value = builder.defineList(name, defaultValue, entryValidator);
        @SuppressWarnings("unchecked")
        ModConfigSpec.ConfigValue<List<String>> listValue = (ModConfigSpec.ConfigValue<List<String>>) (ModConfigSpec.ConfigValue<?>) value;
        var w = track(ForgeConfigValue.simple(listValue, pendingMeta()));
        addUiRow(name, new ConfigOption.ListValue(uiTitle(name), uiDescription(name), w, List.copyOf(defaultValue),
                entryValidator::test, options, icon));
        return w;
    }

    @Override
    public <T> Supplier<T> defineObject(String name, com.google.common.base.Supplier<T> defaultSupplier, Codec<T> rawCodec) {
        SchemaCodec<T> codec = SchemaCodec.wrap(rawCodec);
        addTranslationsAndComments(name);
        if (!writeObjectsAsJson) {
            var w = track(ConfigHelper.defineObject(builder, name, codec, defaultSupplier, pendingMeta()));
            addUiRow(name, new ConfigOption.SchemaValue<>(uiTitle(name), uiDescription(name), w, defaultSupplier::get, codec));
            return w;
        }

        com.google.common.base.Supplier<JsonElement> jsonSupplier = () -> {
            var e = codec.encodeStart(JsonOps.INSTANCE, defaultSupplier.get());
            var json = e.resultOrPartial(s -> {
                throw new RuntimeException("Invalid default value for config " + name + ": " + s);
            });
            if (json.isEmpty()) throw new RuntimeException("Invalid default value for config " + name);
            return json.get();
        };
        var w = track(ForgeConfigValue.codec(
                builder.define(name,
                        () -> jsonSupplier.get().toString().replace(" ", "").replace("\"", "'"),
                        o -> o != null && jsonSupplier.get().getClass().isAssignableFrom(o.getClass())),
                codec, pendingMeta()
        ));
        addUiRow(name, new ConfigOption.SchemaValue<>(uiTitle(name), uiDescription(name), w, defaultSupplier::get, codec));
        return w;
    }

    @Override
    public <T> Supplier<List<T>> defineObjectList(String name, com.google.common.base.Supplier<List<T>> defaultSupplier, Codec<T> codec) {
        builder.comment("This is a list. Add more entries with syntax [[...]]");
        return super.defineObjectList(name, defaultSupplier, codec);
    }

    @Override
    public Supplier<JsonElement> defineJson(String path, JsonElement defaultValue) {
        addTranslationsAndComments(path);
        var w = track(ForgeConfigValue.json(builder.define(path,
                defaultValue.toString().replace(" ", "").replace("\"", "'")), pendingMeta()));
        addUiRow(path, new ConfigOption.JsonValue(uiTitle(path), uiDescription(path), w));
        return w;
    }

    @Override
    public Supplier<JsonElement> defineJson(String path, Supplier<JsonElement> defaultValue) {
        addTranslationsAndComments(path);
        com.google.common.base.Supplier<JsonElement> lazyDefaultValue = Suppliers.memoize(defaultValue::get);
        var w = track(ForgeConfigValue.json(builder.define(path,
                () -> lazyDefaultValue.get().toString().replace(" ", "").replace("\"", "'"),
                o -> o != null && lazyDefaultValue.get().getClass().isAssignableFrom(o.getClass())), pendingMeta()));
        addUiRow(path, new ConfigOption.JsonValue(uiTitle(path), uiDescription(path), w));
        return w;
    }

    @Override
    public <V extends Enum<V>> Supplier<V> define(String name, V defaultValue) {
        addTranslationsAndComments(name);
        var value = builder.defineEnum(name, defaultValue);
        var w = track(ForgeConfigValue.simple(value, pendingMeta()));
        addUiRow(name, new ConfigOption.EnumValue<>(uiTitle(name), uiDescription(name), w, defaultValue,
                defaultValue.getDeclaringClass().getEnumConstants()));
        return w;
    }

    @Override
    protected void forwardReloadFlag(ConfigReloadType type) {
        // Forge applies these to the NEXT defined value, so forward them here (right before that define runs)
        if (type == ConfigReloadType.GAME_RESTART) {
            builder.gameRestart();
        } else if (type == ConfigReloadType.WORLD_RELOAD && !CompatHandler.CONFIGURED) {
            builder.worldRestart();
        }
    }

    @Override
    protected void addTranslationsAndComments(String name) {
        builder.translation(translationKey(name));
        forwardPendingComment();
        super.addTranslationsAndComments(name);
    }

    // must run before every builder.define(). after-comments never reach the toml, only the lang file and screen row
    private void forwardPendingComment() {
        String toForward = pollCommentToForward();
        if (toForward != null) builder.comment(toForward);
    }
}

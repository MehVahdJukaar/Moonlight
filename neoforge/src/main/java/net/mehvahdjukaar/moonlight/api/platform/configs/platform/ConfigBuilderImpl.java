package net.mehvahdjukaar.moonlight.api.platform.configs.platform;

import com.google.common.base.Suppliers;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JavaOps;
import com.mojang.serialization.JsonOps;
import net.mehvahdjukaar.moonlight.api.platform.configs.ConfigBuilder;
import net.mehvahdjukaar.moonlight.api.platform.configs.ConfigType;
import net.mehvahdjukaar.moonlight.api.platform.configs.options.ConfigOption;
import net.mehvahdjukaar.moonlight.api.platform.configs.options.ConfigReloadType;
import net.mehvahdjukaar.moonlight.api.resources.assets.LangBuilder;
import net.minecraft.network.chat.Component;
import net.mehvahdjukaar.moonlight.api.util.math.ColorUtils;
import net.mehvahdjukaar.moonlight.core.CompatHandler;
import net.mehvahdjukaar.moonlight.core.databuddy.ConfigHelper;
import net.mehvahdjukaar.moonlight.platform.ConfigHacks;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.common.ModConfigSpec;
import org.apache.http.annotation.Experimental;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import net.minecraft.world.item.ItemStack;

public class ConfigBuilderImpl extends ConfigBuilder {

    private final List<TrackedConfigValue<?>> trackedValues = new ArrayList<>();

    public static ConfigBuilder create(ResourceLocation name, ConfigType type) {
        return new ConfigBuilderImpl(name, type);
    }

    private final ModConfigSpec.Builder builder;
    private final Deque<String> cat = new ArrayDeque<>();

    public ConfigBuilderImpl(ResourceLocation name, ConfigType type) {
        super(name, type);
        this.builder = new ModConfigSpec.Builder();
        ConfigHacks.init();
    }

    private void ui(String name, ConfigOption<?> value) {
        recordOption(value); // add the screen row
        noteDefined(name, value, null); // Forge .toml comments are handled in comment(); this wires the row's description
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

    private <T> T track(T value) {
        value = applyPendingDynamicPacks(value);
        if (value instanceof TrackedConfigValue<?> trackedValue) {
            this.trackedValues.add(trackedValue);
        }
        return value;
    }

    @Override
    public String currentCategory() {
        return cat.peekFirst();
    }

    @Nullable
    @Override
    public String parentCategory() {
        if (cat.size() < 2) {
            return null;
        }
        var it = cat.descendingIterator();
        it.next();
        return it.next();
    }

    @Override
    public ForgeConfigHolder build() {
        return new ForgeConfigHolder(this.getName(), this.builder.build(), this.type,
                this.buildChangeCallback(), trackedValues, getUiRoot());
    }

    @Override
    public ConfigBuilderImpl push(String category) {
        builder.push(category);
        cat.push(category);
        translations.put(translationKey(""), LangBuilder.getReadableName(category));
        uiPush(Component.translatable(translationKey("")));
        return this;
    }

    @Override
    public ConfigBuilderImpl pop() {
        builder.pop();
        cat.pop();
        uiPop();
        return this;
    }

    @Override
    public Supplier<Boolean> define(String name, boolean defaultValue) {
        addTranslationsAndComments(name);
        var value = builder.define(name, defaultValue);
        var w = track(ValueWrapper.simple(value));
        ui(name, new ConfigOption.BooleanValue(uiTitle(name), uiDescription(name), w, defaultValue));
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
        var w = track(ValueWrapper.simple(value));
        ui(name, slider
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
        var w = track(ValueWrapper.simple(value));
        ui(name, slider
                ? new ConfigOption.DoubleSliderValue(uiTitle(name), uiDescription(name), w, defaultValue, min, max)
                : new ConfigOption.DoubleValue(uiTitle(name), uiDescription(name), w, defaultValue, min, max));
        return w;
    }

    @Override
    public Supplier<Double> definePercentage(String name, double defaultValue) {
        addTranslationsAndComments(name);
        var value = builder.defineInRange(name, defaultValue, 0.0, 1.0);
        var w = track(ValueWrapper.simple(value));
        ui(name, new ConfigOption.PercentValue(uiTitle(name), uiDescription(name), w, defaultValue));
        return w;
    }

    @Experimental
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
        var w = track(new ValueWrapper<Float, Double>(value) {
            @Override
            Float map(Double value) {
                return value.floatValue();
            }
            @Override
            Double unmap(Float value) {
                return (double) value;
            }
        });
        ui(name, slider
                ? new ConfigOption.FloatSliderValue(uiTitle(name), uiDescription(name), w, defaultValue, min, max)
                : new ConfigOption.FloatValue(uiTitle(name), uiDescription(name), w, defaultValue, min, max));
        return w;
    }

    @Override
    public Supplier<Integer> defineColor(String name, int defaultValue) {
        addTranslationsAndComments(name);
        String def = (String) ColorUtils.CODEC.encodeStart(JavaOps.INSTANCE, defaultValue).getOrThrow();
        var value = builder.define(name, def,
                o -> o instanceof String s && ColorUtils.isValidString(s));
        var w = track(ValueWrapper.fromString(value, ColorUtils.CODEC));
        ui(name, new ConfigOption.ColorValue(uiTitle(name), uiDescription(name), w, defaultValue));
        return w;
    }

    @Override
    public Supplier<String> define(String name, String defaultValue, Predicate<Object> validator) {
        addTranslationsAndComments(name);
        var value = builder.define(name, defaultValue, validator);
        var w = track(ValueWrapper.simple(value));
        ui(name, new ConfigOption.StringValue(uiTitle(name), uiDescription(name), w, defaultValue, validator));
        return w;
    }

    @Override
    protected Supplier<String> defineRegexSource(String name, String defaultValue) {
        addTranslationsAndComments(name);
        var value = builder.define(name, defaultValue, ConfigBuilder.REGEX_CHECK);
        var w = track(ValueWrapper.simple(value));
        ui(name, new ConfigOption.RegexValue(uiTitle(name), uiDescription(name), w, defaultValue));
        return w;
    }

    @Override
    protected Supplier<String> defineChoiceSource(String name, String defaultValue, Predicate<Object> validator,
                                                  Supplier<List<String>> options, Function<String, ItemStack> icon) {
        addTranslationsAndComments(name);
        var value = builder.define(name, defaultValue, validator);
        var w = track(ValueWrapper.simple(value));
        ui(name, new ConfigOption.DropdownValue(uiTitle(name), uiDescription(name), w, defaultValue, options, icon));
        return w;
    }

    public <T> Supplier<T> define(String name, Supplier<T> defaultValue, Predicate<Object> validator) {
        addTranslationsAndComments(name);
        var value = builder.define(name, defaultValue, validator);
        var w = track(ValueWrapper.simple(value));
        ui(name, unsupported(name, w));
        return w;
    }

    @Override
    public <T extends String> Supplier<List<String>> define(String name, List<? extends T> defaultValue, Predicate<Object> predicate) {
        addTranslationsAndComments(name);
        var value = builder.defineList(name, defaultValue, predicate);
        @SuppressWarnings("unchecked")
        ModConfigSpec.ConfigValue<List<String>> listValue = (ModConfigSpec.ConfigValue<List<String>>) (ModConfigSpec.ConfigValue<?>) value;
        var w = track(ValueWrapper.simple(listValue));
        ui(name, new ConfigOption.ListValue(uiTitle(name), uiDescription(name), w, List.copyOf(defaultValue),
                s -> predicate.test(s)));
        return w;
    }

    @Override
    protected Supplier<List<String>> defineListSource(String name, List<String> defaultValue, Predicate<Object> entryValidator,
                                                      Supplier<List<String>> options, Function<String, ItemStack> icon) {
        addTranslationsAndComments(name);
        var value = builder.defineList(name, defaultValue, entryValidator);
        @SuppressWarnings("unchecked")
        ModConfigSpec.ConfigValue<List<String>> listValue = (ModConfigSpec.ConfigValue<List<String>>) (ModConfigSpec.ConfigValue<?>) value;
        var w = track(ValueWrapper.simple(listValue));
        ui(name, new ConfigOption.ListValue(uiTitle(name), uiDescription(name), w, List.copyOf(defaultValue),
                entryValidator::test, options, icon));
        return w;
    }

    @Override
    public <T> Supplier<T> defineObject(String name, com.google.common.base.Supplier<T> defaultSupplier, Codec<T> codec) {
        if (usesDataBuddy) {
            var w = track(ConfigHelper.defineObject(builder, name, codec, defaultSupplier));
            ui(name, unsupported(name, w));
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
        var w = track(ValueWrapper.codec(
                builder.define(name,
                        () -> jsonSupplier.get().toString().replace(" ", "").replace("\"", "'"),
                        o -> o != null && jsonSupplier.get().getClass().isAssignableFrom(o.getClass())),
                codec
        ));
        ui(name, unsupported(name, w));
        return w;
    }

    @Override
    public <T> Supplier<List<T>> defineObjectList(String name, com.google.common.base.Supplier<List<T>> defaultSupplier, Codec<T> codec) {
        builder.comment("This is a list. Add more entries with syntax [[...]]");
        return super.defineObjectList(name, defaultSupplier, codec);
    }

    @Override
    public Supplier<JsonElement> defineJson(String path, JsonElement defaultValue) {
        var w = track(ValueWrapper.json(builder.define(path,
                defaultValue.toString().replace(" ", "").replace("\"", "'"))));
        ui(path, new ConfigOption.JsonValue(uiTitle(path), uiDescription(path), w));
        return w;
    }

    @Override
    public Supplier<JsonElement> defineJson(String path, Supplier<JsonElement> defaultValue) {
        com.google.common.base.Supplier<JsonElement> lazyDefaultValue = Suppliers.memoize(defaultValue::get);
        var w = track(ValueWrapper.json(builder.define(path,
                () -> lazyDefaultValue.get().toString().replace(" ", "").replace("\"", "'"),
                o -> o != null && lazyDefaultValue.get().getClass().isAssignableFrom(o.getClass()))));
        ui(path, new ConfigOption.JsonValue(uiTitle(path), uiDescription(path), w));
        return w;
    }

    @Override
    public <V extends Enum<V>> Supplier<V> define(String name, V defaultValue) {
        addTranslationsAndComments(name);
        var value = builder.defineEnum(name, defaultValue);
        var w = track(ValueWrapper.simple(value));
        ui(name, new ConfigOption.EnumValue<>(uiTitle(name), uiDescription(name), w, defaultValue,
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
        super.addTranslationsAndComments(name);
    }

    @Override
    public ConfigBuilder comment(String comment) {
        // Forge can only attach a .toml comment to the NEXT defined value, so only forward before-order comments
        // (nothing is waiting for an after-comment). After-order comments still reach the lang file and the
        // screen row via super, they just don't make it into the .toml file.
        if (!isAwaitingAfterComment()) {
            builder.comment(comment);
        }
        return super.comment(comment);
    }

    abstract static class ValueWrapper<T, C> implements TrackedConfigValue<T> {
        private final ModConfigSpec.ConfigValue<C> original;
        private T cachedValue = null;
        private C cachedRaw = null;
        private boolean initialized = false;
        private boolean affectsDynamicPacks;

        ValueWrapper(ModConfigSpec.ConfigValue<C> original) {
            this.original = original;
        }

        // simple pass‑through wrapper
        public static <T> ValueWrapper<T, T> simple(ModConfigSpec.ConfigValue<T> original) {
            return new ValueWrapper<>(original) {
                @Override
                T map(T value) { return value; }
                @Override
                T unmap(T value) { return value; }
            };
        }

        // wrapper that uses a Codec to convert between String and T (e.g. for colours)
        public static <T> ValueWrapper<T, String> fromString(ModConfigSpec.ConfigValue<String> original, Codec<T> codec) {
            return new ValueWrapper<>(original) {
                @Override
                T map(String value) {
                    return codec.parse(JavaOps.INSTANCE, value).getOrThrow();
                }
                @Override
                String unmap(T value) {
                    return codec.encodeStart(JavaOps.INSTANCE, value).getOrThrow().toString();
                }
            };
        }

        // wrapper that handles JSON config values (stored as String, exposed as JsonElement)
        public static ValueWrapper<JsonElement, String> json(ModConfigSpec.ConfigValue<String> original) {
            return new ValueWrapper<>(original) {
                @Override
                JsonElement map(String value) {
                    try {
                        // stored string uses single quotes to avoid escaping issues, revert to double quotes for parsing
                        return JsonParser.parseString(value.replace("'", "\""));
                    } catch (Exception e) {
                        throw new RuntimeException("Failed to parse JSON config value: " + value, e);
                    }
                }
                @Override
                String unmap(JsonElement value) {
                    // store as compact string with single quotes
                    return value.toString().replace(" ", "").replace("\"", "'");
                }
            };
        }

        public static <T> ValueWrapper<T, String> codec(ModConfigSpec.ConfigValue<String> original, Codec<T> codec) {
            return new ValueWrapper<>(original) {
                @Override
                T map(String raw) {
                    // raw is stored with single quotes, restore double quotes and parse
                    JsonElement json = JsonParser.parseString(raw.replace("'", "\""));
                    return codec.decode(JsonOps.INSTANCE, json)
                            .getOrThrow()
                            .getFirst();
                }

                @Override
                String unmap(T value) {
                    JsonElement json = codec.encodeStart(JsonOps.INSTANCE, value).getOrThrow();
                    // store with single quotes to avoid escaping issues
                    return json.toString().replace(" ", "").replace("\"", "'");
                }
            };
        }

        abstract T map(C value);
        abstract C unmap(T value);

        @Override
        public T get() {
            pollChanged();
            if (cachedValue == null && initialized) {
                cachedValue = map(cachedRaw);
            }
            return cachedValue;
        }

        @Override
        public boolean pollChanged() {
            C current = original.get();
            if (!initialized) {
                cachedRaw = current;
                cachedValue = map(current);
                initialized = true;
                return false;
            }
            if (!Objects.equals(cachedRaw, current)) {
                cachedRaw = current;
                cachedValue = map(current);
                return true;
            }
            return false;
        }

        @Override
        public boolean setValue(T value) {
            C raw = unmap(value);
            boolean changed = !initialized || !Objects.equals(cachedRaw, raw);
            original.set(raw);
            cachedRaw = raw;
            cachedValue = value;
            initialized = true;
            return changed;
        }

        @Override
        public boolean affectsDynamicPacks() {
            return affectsDynamicPacks;
        }

        @Override
        public void setAffectsDynamicPacks(boolean affectsDynamicPacks) {
            this.affectsDynamicPacks = affectsDynamicPacks;
        }
    }
}
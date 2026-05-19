package net.mehvahdjukaar.moonlight.api.platform.configs.platform;

import com.google.common.base.Suppliers;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JavaOps;
import com.mojang.serialization.JsonOps;
import net.mehvahdjukaar.moonlight.api.platform.configs.ConfigBuilder;
import net.mehvahdjukaar.moonlight.api.platform.configs.ConfigType;
import net.mehvahdjukaar.moonlight.api.resources.assets.LangBuilder;
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
import java.util.function.Predicate;
import java.util.function.Supplier;

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
                this.buildChangeCallback(), trackedValues);
    }

    @Override
    public ConfigBuilderImpl push(String category) {
        builder.push(category);
        cat.push(category);
        translations.put(translationKey(""), LangBuilder.getReadableName(category));
        return this;
    }

    @Override
    public ConfigBuilderImpl pop() {
        builder.pop();
        cat.pop();
        return this;
    }

    @Override
    public Supplier<Boolean> define(String name, boolean defaultValue) {
        addTranslationsAndComments(name);
        var value = builder.define(name, defaultValue);
        return track(ValueWrapper.simple(value));
    }

    @Override
    public Supplier<Integer> define(String name, int defaultValue, int min, int max) {
        addTranslationsAndComments(name);
        var value = builder.defineInRange(name, defaultValue, min, max);
        return track(ValueWrapper.simple(value));
    }

    @Override
    public Supplier<Double> define(String name, double defaultValue, double min, double max) {
        addTranslationsAndComments(name);
        var value = builder.defineInRange(name, defaultValue, min, max);
        return track(ValueWrapper.simple(value));
    }

    @Experimental
    @Override
    public Supplier<Float> define(String name, float defaultValue, float min, float max) {
        addTranslationsAndComments(name);
        var value = builder.defineInRange(name, defaultValue, min, max);
        return track(new ValueWrapper<Float, Double>(value) {
            @Override
            Float map(Double value) {
                return value.floatValue();
            }
            @Override
            Double unmap(Float value) {
                return (double) value;
            }
        });
    }

    @Override
    public Supplier<Integer> defineColor(String name, int defaultValue) {
        addTranslationsAndComments(name);
        String def = (String) ColorUtils.CODEC.encodeStart(JavaOps.INSTANCE, defaultValue).getOrThrow();
        var value = builder.define(name, def,
                o -> o instanceof String s && ColorUtils.isValidString(s));
        return track(ValueWrapper.fromString(value, ColorUtils.CODEC));
    }

    @Override
    public Supplier<String> define(String name, String defaultValue, Predicate<Object> validator) {
        addTranslationsAndComments(name);
        var value = builder.define(name, defaultValue, validator);
        return track(ValueWrapper.simple(value));
    }

    public <T> Supplier<T> define(String name, Supplier<T> defaultValue, Predicate<Object> validator) {
        addTranslationsAndComments(name);
        var value = builder.define(name, defaultValue, validator);
        return track(ValueWrapper.simple(value));
    }

    @Override
    public <T extends String> Supplier<List<String>> define(String name, List<? extends T> defaultValue, Predicate<Object> predicate) {
        addTranslationsAndComments(name);
        var value = builder.defineList(name, defaultValue, predicate);
        @SuppressWarnings("unchecked")
        ModConfigSpec.ConfigValue<List<String>> listValue = (ModConfigSpec.ConfigValue<List<String>>) (ModConfigSpec.ConfigValue<?>) value;
        return track(ValueWrapper.simple(listValue));
    }

    @Override
    public <T> Supplier<T> defineObject(String name, com.google.common.base.Supplier<T> defaultSupplier, Codec<T> codec) {
        if (usesDataBuddy) return track(ConfigHelper.defineObject(builder, name, codec, defaultSupplier));

        com.google.common.base.Supplier<JsonElement> jsonSupplier = () -> {
            var e = codec.encodeStart(JsonOps.INSTANCE, defaultSupplier.get());
            var json = e.resultOrPartial(s -> {
                throw new RuntimeException("Invalid default value for config " + name + ": " + s);
            });
            if (json.isEmpty()) throw new RuntimeException("Invalid default value for config " + name);
            return json.get();
        };
        return track(ValueWrapper.codec(
                builder.define(name,
                        () -> jsonSupplier.get().toString().replace(" ", "").replace("\"", "'"),
                        o -> o != null && jsonSupplier.get().getClass().isAssignableFrom(o.getClass())),
                codec
        ));
    }

    @Override
    public <T> Supplier<List<T>> defineObjectList(String name, com.google.common.base.Supplier<List<T>> defaultSupplier, Codec<T> codec) {
        builder.comment("This is a list. Add more entries with syntax [[...]]");
        return super.defineObjectList(name, defaultSupplier, codec);
    }

    @Override
    public Supplier<JsonElement> defineJson(String path, JsonElement defaultValue) {
        return track(ValueWrapper.json(builder.define(path,
                defaultValue.toString().replace(" ", "").replace("\"", "'"))));
    }

    @Override
    public Supplier<JsonElement> defineJson(String path, Supplier<JsonElement> defaultValue) {
        com.google.common.base.Supplier<JsonElement> lazyDefaultValue = Suppliers.memoize(defaultValue::get);
        return track(ValueWrapper.json(builder.define(path,
                () -> lazyDefaultValue.get().toString().replace(" ", "").replace("\"", "'"),
                o -> o != null && lazyDefaultValue.get().getClass().isAssignableFrom(o.getClass()))));
    }

    @Override
    public <V extends Enum<V>> Supplier<V> define(String name, V defaultValue) {
        addTranslationsAndComments(name);
        var value = builder.defineEnum(name, defaultValue);
        return track(ValueWrapper.simple(value));
    }

    @Override
    public ConfigBuilder gameRestart() {
        builder.gameRestart();
        return this;
    }

    @Override
    public ConfigBuilder worldReload() {
        if (!CompatHandler.CONFIGURED) {
            builder.worldRestart();
        }
        return this;
    }

    @Override
    protected void addTranslationsAndComments(String name) {
        builder.translation(translationKey(name));
        super.addTranslationsAndComments(name);
    }

    @Override
    public ConfigBuilder comment(String comment) {
        builder.comment(comment);
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
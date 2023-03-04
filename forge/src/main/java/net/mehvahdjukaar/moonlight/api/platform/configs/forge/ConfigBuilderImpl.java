package net.mehvahdjukaar.moonlight.api.platform.configs.forge;

import com.google.common.base.Suppliers;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import net.mehvahdjukaar.moonlight.api.platform.configs.ConfigBuilder;
import net.mehvahdjukaar.moonlight.api.platform.configs.ConfigType;
import net.mehvahdjukaar.moonlight.core.databuddy.ConfigHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class ConfigBuilderImpl extends ConfigBuilder {

    private final List<ForgeConfigSpec.ConfigValue<?>> requireGameRestart = new ArrayList<>();
    private boolean currentGameRestart;
    private ForgeConfigSpec.ConfigValue<?> currentValue;

    public static ConfigBuilder create(ResourceLocation name, ConfigType type) {
        return new ConfigBuilderImpl(name, type);
    }

    private final ForgeConfigSpec.Builder builder;

    private String cat = null;

    public ConfigBuilderImpl(ResourceLocation name, ConfigType type) {
        super(name, type);
        this.builder = new ForgeConfigSpec.Builder();
    }

    @Override
    public String currentCategory() {
        return cat;
    }


    @Override
    public ConfigSpecWrapper build() {
        return new ConfigSpecWrapper(this.getName(), this.builder.build(), this.type, this.synced,
                this.changeCallback, this.requireGameRestart);
    }

    @Override
    public ConfigBuilderImpl push(String category) {
        assert cat == null;
        builder.push(category);
        cat = category;
        return this;
    }

    @Override
    public ConfigBuilderImpl pop() {
        assert cat != null;
        builder.pop();
        cat = null;
        return this;
    }

    @Override
    public Supplier<Boolean> define(String name, boolean defaultValue) {
        maybeAddTranslationString(name);
        var value = builder.define(name, defaultValue);
        this.currentValue = value;
        maybeAddGameRestart();
        return value;
    }

    @Override
    public Supplier<Double> define(String name, double defaultValue, double min, double max) {
        maybeAddTranslationString(name);
        var value = builder.defineInRange(name, defaultValue, min, max);
        this.currentValue = value;
        maybeAddGameRestart();
        return value;
    }

    @Override
    public Supplier<Integer> define(String name, int defaultValue, int min, int max) {
        maybeAddTranslationString(name);
        var value = builder.defineInRange(name, defaultValue, min, max);
        this.currentValue = value;
        maybeAddGameRestart();
        return value;
    }

    @Override
    public Supplier<Integer> defineColor(String name, int defaultValue) {
        maybeAddTranslationString(name);
        var stringConfig = builder.define(name, Integer.toHexString(defaultValue), ConfigBuilder.COLOR_CHECK);
        this.currentValue = stringConfig;
        maybeAddGameRestart();
        return () -> Integer.parseUnsignedInt(stringConfig.get().replace("0x", ""), 16);
    }

    @Override
    public Supplier<String> define(String name, String defaultValue, Predicate<Object> validator) {
        maybeAddTranslationString(name);
        var value = builder.define(name, defaultValue, validator);
        this.currentValue = value;
        maybeAddGameRestart();
        return value;
    }

    public <T> Supplier<T> define(String name, Supplier<T> defaultValue, Predicate<Object> validator) {
        maybeAddTranslationString(name);
        var value = builder.define(name, defaultValue, validator);
        this.currentValue = value;
        maybeAddGameRestart();
        return value;
    }

    @Override
    public <T extends String> Supplier<List<String>> define(String name, List<? extends T> defaultValue, Predicate<Object> predicate) {
        maybeAddTranslationString(name);
        var value = builder.defineList(name, defaultValue, predicate);
        this.currentValue = value;
        maybeAddGameRestart();
        return () -> (List<String>) value.get();
    }

    @Override
    public <T> Supplier<T> defineObject(String name, com.google.common.base.Supplier<T> defaultSupplier, Codec<T> codec) {
        if (usesDataBuddy) return ConfigHelper.defineObject(builder, name, codec, defaultSupplier); //actual toml parse
        return StringCodecConfigValue.define(this, name, defaultSupplier, codec); //string based config
    }

    @Override
    public <T> Supplier<List<T>> defineObjectList(String name, com.google.common.base.Supplier<List<T>> defaultSupplier, Codec<T> codec) {
        builder.comment("This is a list. Add more entries with syntax [[...]]");
        return super.defineObjectList(name, defaultSupplier, codec);
    }

    private static class StringCodecConfigValue<T> implements Supplier<T> {

        private final StringJsonConfigValue inner;
        private final Codec<T> codec;
        private T cache;

        public static <T> StringCodecConfigValue<T> define(ConfigBuilderImpl cfg, String name, Supplier<T> defaultValueSupplier, Codec<T> codec) {
            Supplier<JsonElement> jsonSupplier = ()->{
                var e = codec.encodeStart(JsonOps.INSTANCE, defaultValueSupplier.get());
                var json = e.resultOrPartial(s -> {
                    throw new RuntimeException("Invalid default value for config " + name + ": " + s);
                });
                if (json.isEmpty()) throw new RuntimeException("Invalid default value for config " + name);
                return json.get();
            };

            var jsonConfig = cfg.defineJson(name, jsonSupplier);
            return new StringCodecConfigValue<>(jsonConfig, codec);
        }

        public StringCodecConfigValue(StringJsonConfigValue jsonConfig, Codec<T> codec) {
            this.inner = jsonConfig;
            this.codec = codec;
        }

        @Override
        public T get() {
            if (inner.hasBeenReset()) this.cache = null;
            if (cache == null) {
                var j = inner.get();
                var d = codec.decode(JsonOps.INSTANCE, j);
                var o = d.resultOrPartial(s -> {
                    throw new RuntimeException("Failed to decode config: " + s);
                });
                if (o.isEmpty()) throw new RuntimeException("Failed to parse decode with value" + j);
                return o.get().getFirst();
            }
            return null;
        }
    }

    @Override
    public StringJsonConfigValue defineJson(String path, JsonElement defaultValue) {
        return StringJsonConfigValue.define(this, path, defaultValue);
    }

    @Override
    public StringJsonConfigValue defineJson(String path, Supplier<JsonElement> defaultValue) {
        return StringJsonConfigValue.define(this, path, defaultValue);
    }

    private static class StringJsonConfigValue implements Supplier<JsonElement> {

        private static final Field cachedValue = ObfuscationReflectionHelper.findField(ForgeConfigSpec.ConfigValue.class, "cachedValue");

        static {
            cachedValue.setAccessible(true);
        }

        private final ForgeConfigSpec.ConfigValue<String> inner;
        private JsonElement cache = null;

        public static StringJsonConfigValue define(ConfigBuilderImpl cfg, String path, Supplier<JsonElement> defaultValueSupplier) {
            com.google.common.base.Supplier<JsonElement> lazyDefaultValue = Suppliers.memoize(defaultValueSupplier::get);
            return new StringJsonConfigValue(cfg.define(path, () -> lazyDefaultValue.get().toString().replace(" ", "")
                    .replace("\"", "'"), o -> o != null && lazyDefaultValue.get().getClass().isAssignableFrom(o.getClass())));
        }

        public static StringJsonConfigValue define(ConfigBuilderImpl cfg, String path, JsonElement defaultValue) {
            return new StringJsonConfigValue(cfg.define(path, defaultValue.toString().replace(" ", "")
                    .replace("\"", "'")));
        }

        StringJsonConfigValue(Supplier<String> innerConfig) {
            this.inner = (ForgeConfigSpec.ConfigValue<String>) innerConfig;
        }

        @Override
        public JsonElement get() {
            if (hasBeenReset()) {
                this.cache = null;
            }
            if (cache == null) {
                String s = inner.get().replace("'", "\"");
                try {
                    this.cache = JsonParser.parseString(s);
                } catch (Exception e) {
                    throw new RuntimeException("Failed to parse json config: ", e);
                }
            }
            return cache;
        }

        public boolean hasBeenReset() {
            try {
                return cachedValue.get(inner) == null;
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public <V extends Enum<V>> Supplier<V> define(String name, V defaultValue) {
        maybeAddTranslationString(name);
        var value = builder.defineEnum(name, defaultValue);
        this.currentValue = value;
        maybeAddGameRestart();
        return value;
    }

    private void maybeAddGameRestart() {
        if (currentGameRestart && currentValue != null) {
            requireGameRestart.add(currentValue);
            currentGameRestart = false;
            currentValue = null;
        }
    }

    @Override
    public ConfigBuilder gameRestart() {
        this.currentGameRestart = true;
        return this;
    }

    @Override
    public ConfigBuilder worldReload() {
        builder.worldRestart();
        return this;
    }

    @Override
    public ConfigBuilder comment(String comment) {
        builder.comment(comment); //.translationKey(getTranslationName());
        //TODO: choose. either add a translation or a comment literal not both
        return super.comment(comment);
    }
}

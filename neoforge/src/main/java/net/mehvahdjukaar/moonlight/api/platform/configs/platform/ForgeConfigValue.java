package net.mehvahdjukaar.moonlight.api.platform.configs.platform;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JavaOps;
import com.mojang.serialization.JsonOps;
import net.mehvahdjukaar.moonlight.api.platform.configs.ConfigMetadata;
import net.mehvahdjukaar.moonlight.api.platform.configs.options.ConfigReloadType;
import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.Objects;

abstract class ForgeConfigValue<T, C> implements TrackedConfigValue<T> {
    private final ModConfigSpec.ConfigValue<C> original;
    private final ConfigMetadata meta;
    private T cachedValue = null;
    private C cachedRaw = null;
    private boolean initialized = false;

    ForgeConfigValue(ModConfigSpec.ConfigValue<C> original, ConfigMetadata meta) {
        this.original = original;
        this.meta = meta;
    }

    public static <T> ForgeConfigValue<T, T> simple(ModConfigSpec.ConfigValue<T> original, ConfigMetadata meta) {
        return new ForgeConfigValue<>(original, meta) {
            @Override
            T map(T value) { return value; }
            @Override
            T unmap(T value) { return value; }
        };
    }

    public static <T> ForgeConfigValue<T, String> fromString(ModConfigSpec.ConfigValue<String> original, Codec<T> codec, ConfigMetadata meta) {
        return new ForgeConfigValue<>(original, meta) {
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

    public static ForgeConfigValue<JsonElement, String> json(ModConfigSpec.ConfigValue<String> original, ConfigMetadata meta) {
        return new ForgeConfigValue<>(original, meta) {
            @Override
            JsonElement map(String value) {
                try {
                    return JsonParser.parseString(value.replace("'", "\""));
                } catch (Exception e) {
                    throw new RuntimeException("Failed to parse JSON config value: " + value, e);
                }
            }
            @Override
            String unmap(JsonElement value) {
                return value.toString().replace(" ", "").replace("\"", "'");
            }
        };
    }

    public static <T> ForgeConfigValue<T, String> codec(ModConfigSpec.ConfigValue<String> original, Codec<T> codec, ConfigMetadata meta) {
        return new ForgeConfigValue<>(original, meta) {
            @Override
            T map(String raw) {
                JsonElement json = JsonParser.parseString(raw.replace("'", "\""));
                return codec.decode(JsonOps.INSTANCE, json)
                        .getOrThrow()
                        .getFirst();
            }

            @Override
            String unmap(T value) {
                JsonElement json = codec.encodeStart(JsonOps.INSTANCE, value).getOrThrow();
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
        // NeoForge's ConfigValue.set() skips refreshing its own cache for worldRestart/gameRestart values, so
        // original.get() would return the stale value and pollChanged() would revert the set, so clear the cache
        original.clearCache();
        cachedRaw = raw;
        cachedValue = value;
        initialized = true;
        return changed;
    }

    @Override
    public boolean affectsDynamicPacks() {
        return meta.affectsDynamicPacks();
    }

    @Override
    public ConfigReloadType reloadType() {
        return meta.reloadType();
    }
}

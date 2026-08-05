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

// NeoForge leaf value: a loader independent TrackedConfigValue view over a raw ModConfigSpec.ConfigValue. The stored
// raw type is mapped to/from the exposed one by map/unmap, so colours, json and beans live behind the same interface
// as plain values. Change metadata is injected once at construction.
abstract class ValueWrapper<T, C> implements TrackedConfigValue<T> {
    private final ModConfigSpec.ConfigValue<C> original;
    private final ConfigMetadata meta;
    private T cachedValue = null;
    private C cachedRaw = null;
    private boolean initialized = false;

    ValueWrapper(ModConfigSpec.ConfigValue<C> original, ConfigMetadata meta) {
        this.original = original;
        this.meta = meta;
    }

    // simple pass-through wrapper
    public static <T> ValueWrapper<T, T> simple(ModConfigSpec.ConfigValue<T> original, ConfigMetadata meta) {
        return new ValueWrapper<>(original, meta) {
            @Override
            T map(T value) { return value; }
            @Override
            T unmap(T value) { return value; }
        };
    }

    // uses a Codec to convert between String and T, for colours and the like
    public static <T> ValueWrapper<T, String> fromString(ModConfigSpec.ConfigValue<String> original, Codec<T> codec, ConfigMetadata meta) {
        return new ValueWrapper<>(original, meta) {
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

    // JSON values: stored as String, exposed as JsonElement
    public static ValueWrapper<JsonElement, String> json(ModConfigSpec.ConfigValue<String> original, ConfigMetadata meta) {
        return new ValueWrapper<>(original, meta) {
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

    public static <T> ValueWrapper<T, String> codec(ModConfigSpec.ConfigValue<String> original, Codec<T> codec, ConfigMetadata meta) {
        return new ValueWrapper<>(original, meta) {
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
        // NeoForge's ConfigValue.set() skips refreshing its own cache for worldRestart/gameRestart values, so
        // original.get() would keep returning the stale value and pollChanged() would revert the freshly set one on
        // the next read (the config screen snapping back after Save). Clearing the cache makes NeoForge re-read what
        // we just wrote, matching Fabric where a set is effective immediately and the reload badge stays advisory
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

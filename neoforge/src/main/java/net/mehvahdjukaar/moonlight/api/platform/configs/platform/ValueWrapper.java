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

// Wraps a raw ModConfigSpec.ConfigValue so the rest of the code can use it through TrackedConfigValue and not care
// which loader it's on. map/unmap convert between what's stored and what's handed out, so colours, json and beans
// look the same as plain values from the outside. The reload flags are passed in once, at construction
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

    // no conversion, the stored type is the exposed one
    public static <T> ValueWrapper<T, T> simple(ModConfigSpec.ConfigValue<T> original, ConfigMetadata meta) {
        return new ValueWrapper<>(original, meta) {
            @Override
            T map(T value) { return value; }
            @Override
            T unmap(T value) { return value; }
        };
    }

    // stored as a String, converted with a Codec. Used for colours and the like
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

    // stored as a String, handed out as a JsonElement
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
        // NeoForge's ConfigValue.set() doesn't refresh its own cache for worldRestart/gameRestart values, so
        // original.get() would keep giving the old value and pollChanged() would undo the one we just wrote on the
        // next read, making the screen snap back after Save. Clearing the cache makes it re-read what we wrote, like
        // on Fabric where a set takes effect right away and the reload icon is only a hint
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

package net.mehvahdjukaar.moonlight.core.client.config;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import net.mehvahdjukaar.codecui.Schema;
import net.mehvahdjukaar.moonlight.api.client.gui.ConfigEditSession;
import net.mehvahdjukaar.moonlight.api.platform.configs.options.ConfigCategory;
import net.mehvahdjukaar.moonlight.api.platform.configs.options.ConfigOption;
import net.mehvahdjukaar.moonlight.api.resources.assets.LangBuilder;
import net.mehvahdjukaar.moonlight.api.util.math.ColorUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

/**
 * Turns a CodecUI {@link Schema} into the very same {@link ConfigCategory}/{@link ConfigOption} tree the native config
 * screen already renders, so {@link SchemaEditScreen} can reuse every existing widget (booleans, sliders, colour
 * pickers, dropdowns, the JSON editor) with no new UI code.
 *
 * <p>Each generated leaf is backed by a throwaway {@link MemoryConfigValue}, and every leaf also contributes a
 * {@link Reader} that reads its current working value back out of the {@link ConfigEditSession} and re-serialises it to
 * JSON. The {@link Reader}s compose up the record tree, so the root reader reproduces the whole value's JSON on Done,
 * which is then decoded through the codec.</p>
 *
 * <p>Structural kinds map to rich rows: records become navigable sub-categories, primitives/enums/colours/ids become
 * their matching controls. Anything the form can't render structurally — lists, maps, pairs, alternatives, opaque or
 * recursive codecs — degrades to a raw-JSON row (the existing {@link ConfigOption.JsonValue} editor) for that node, so
 * the form never fails to represent a value; at worst a sub-tree is edited as JSON text.</p>
 *
 * <p>Seeding always prefers the current value, then the encoded default (so an <em>absent</em> optional field is
 * seeded with its real default rather than a neutral zero — writing it back can't silently change the value), then a
 * neutral fallback.</p>
 */
final class SchemaForm {

    /** Reconstructs a node's JSON from the editor session's working values. */
    @FunctionalInterface
    interface Reader {
        JsonElement read(ConfigEditSession session);
    }

    final ConfigCategory root;
    final Reader reader;

    private SchemaForm(ConfigCategory root, Reader reader) {
        this.root = root;
        this.reader = reader;
    }

    /**
     * Builds the form for a whole config value. {@code current} is the value's JSON, {@code defaults} the encoded
     * default value's JSON (used to seed absent optional fields). A top-level record populates the root category
     * directly; any other kind becomes a single row named "value".
     */
    static SchemaForm build(Component title, Schema<?> schema, JsonElement current, @Nullable JsonElement defaults) {
        ConfigCategory root = new ConfigCategory(title);
        Reader reader;
        if (schema instanceof Schema.Record<?> rec) {
            reader = populateRecord(root, rec, current, defaults);
        } else {
            reader = buildField(root, "value", readable("value"), schema, current, defaults);
        }
        return new SchemaForm(root, reader);
    }

    /** Adds one row (or a sub-category) for a field to {@code parent} and returns the reader producing its JSON. */
    private static Reader buildField(ConfigCategory parent, String name, Component title, Schema<?> schema,
                                     @Nullable JsonElement current, @Nullable JsonElement def) {
        JsonElement seed = current != null && !current.isJsonNull() ? current : def;
        return switch (schema) {
            case Schema.Bool ignored -> {
                boolean v = asBool(seed, false);
                var opt = new ConfigOption.BooleanValue(title, null, new MemoryConfigValue<>(v), v);
                parent.add(opt);
                yield s -> new JsonPrimitive((Boolean) s.current(opt));
            }
            case Schema.IntRange r -> {
                int v = Math.clamp(asInt(seed, neutralInt(r.min(), r.max())), r.min(), r.max());
                var opt = new ConfigOption.IntValue(title, null, new MemoryConfigValue<>(v), v, r.min(), r.max());
                parent.add(opt);
                yield s -> new JsonPrimitive((Integer) s.current(opt));
            }
            case Schema.FloatRange r -> {
                float v = Math.clamp(asFloat(seed, neutralFloat(r.min(), r.max())), r.min(), r.max());
                var opt = new ConfigOption.FloatValue(title, null, new MemoryConfigValue<>(v), v, r.min(), r.max());
                parent.add(opt);
                yield s -> new JsonPrimitive((Float) s.current(opt));
            }
            case Schema.DoubleRange r -> {
                double v = Math.clamp(asDouble(seed, neutralDouble(r.min(), r.max())), r.min(), r.max());
                var opt = new ConfigOption.DoubleValue(title, null, new MemoryConfigValue<>(v), v, r.min(), r.max());
                parent.add(opt);
                yield s -> new JsonPrimitive((Double) s.current(opt));
            }
            case Schema.LongRange r -> {
                // no dedicated long control: a numeric text field whose reader parses back to a JSON long
                long v = Math.clamp(asLong(seed, 0L), r.min(), r.max());
                String sv = Long.toString(v);
                Predicate<Object> valid = o -> o instanceof String str && isLongInRange(str, r.min(), r.max());
                var opt = new ConfigOption.StringValue(title, null, new MemoryConfigValue<>(sv), sv, valid);
                parent.add(opt);
                yield s -> new JsonPrimitive(parseLongOr( s.current(opt), v));
            }
            case Schema.Color c -> {
                int rgb = asColor(seed, 0xFFFFFFFF);
                var opt = new ConfigOption.ColorValue(title, null, new MemoryConfigValue<>(rgb), rgb);
                parent.add(opt);
                yield s -> {
                    int col = (Integer) s.current(opt);
                    return c.hexString() ? new JsonPrimitive(ColorUtils.toHexString(col, c.hasAlpha())) : new JsonPrimitive(col);
                };
            }
            case Schema.Str str -> {
                String v = asString(seed, "");
                Predicate<Object> valid = o -> o instanceof String x
                        && x.length() >= str.minLen() && x.length() <= str.maxLen()
                        && (str.pattern() == null || str.pattern().matcher(x).matches());
                var opt = new ConfigOption.StringValue(title, null, new MemoryConfigValue<>(v), v, valid);
                parent.add(opt);
                yield s -> new JsonPrimitive((String) s.current(opt));
            }
            case Schema.ResourceId ignored -> {
                String v = asString(seed, "");
                Predicate<Object> valid = o -> o instanceof String x && ResourceLocation.tryParse(x) != null;
                var opt = new ConfigOption.StringValue(title, null, new MemoryConfigValue<>(v), v, valid);
                parent.add(opt);
                yield s -> new JsonPrimitive((String) s.current(opt));
            }
            case Schema.Enum<?> en -> enumField(parent, title, en, seed);
            case Schema.Record<?> rec -> {
                ConfigCategory sub = new ConfigCategory(title);
                parent.add(sub);
                yield populateRecord(sub, rec, current, def);
            }
            // everything structural we don't render natively (lists, maps, pairs, alternatives, opaque/custom/recursive)
            // degrades to a raw-JSON editor for that node — the form is always representable, never a dead end
            default -> rawJsonField(parent, title, schema, seed);
        };
    }

    /** Populates {@code cat} with one entry per record field and returns a reader assembling their JSON object. */
    private static Reader populateRecord(ConfigCategory cat, Schema.Record<?> rec,
                                         @Nullable JsonElement current, @Nullable JsonElement def) {
        JsonObject cur = current instanceof JsonObject o ? o : null;
        JsonObject dfl = def instanceof JsonObject o ? o : null;
        List<FieldReader> fields = new ArrayList<>(rec.fields().size());
        for (Schema.Field<?, ?> f : rec.fields()) {
            JsonElement fc = cur != null ? cur.get(f.name()) : null;
            JsonElement fd = dfl != null ? dfl.get(f.name()) : null;
            Reader r = buildField(cat, f.name(), readable(f.name()), f.schema(), fc, fd);
            fields.add(new FieldReader(f.name(), r));
        }
        return s -> {
            JsonObject o = new JsonObject();
            for (FieldReader fr : fields) {
                JsonElement v = fr.reader.read(s);
                if (v != null) o.add(fr.name, v);
            }
            return o;
        };
    }

    private record FieldReader(String name, Reader reader) {}

    private static Reader enumField(ConfigCategory parent, Component title, Schema.Enum<?> en, @Nullable JsonElement seed) {
        List<String> labels = labelsOf(en);
        String initial = asString(seed, labels.isEmpty() ? "" : labels.getFirst());
        if (!labels.contains(initial)) initial = labels.isEmpty() ? "" : labels.getFirst();
        var opt = new ConfigOption.DropdownValue(title, null, new MemoryConfigValue<>(initial), initial, () -> labels, null);
        parent.add(opt);
        return s -> new JsonPrimitive(s.current(opt));
    }

    @SuppressWarnings("unchecked")
    private static List<String> labelsOf(Schema.Enum<?> en) {
        Schema.Enum<Object> e = (Schema.Enum<Object>) en;
        List<String> out = new ArrayList<>(e.options().size());
        for (Object o : e.options()) out.add(e.label().apply(o));
        return out;
    }

    private static Reader rawJsonField(ConfigCategory parent, Component title, Schema<?> schema, @Nullable JsonElement seed) {
        JsonElement node = seed != null ? seed : emptyFor(schema);
        var opt = new ConfigOption.JsonValue(title, null, () -> node);
        parent.add(opt);
        // JsonValue is edited as a pretty-printed JSON string; parse it back, falling back to the seed if somehow invalid
        return s -> {
            Object cur = s.current(opt);
            if (!(cur instanceof String str)) return node;
            try {
                return JsonParser.parseString(str);
            } catch (Exception e) {
                return node;
            }
        };
    }

    // ===== seeding / conversion helpers =====

    private static Component readable(String name) {
        return Component.literal(LangBuilder.getReadableName(name));
    }

    private static boolean asBool(@Nullable JsonElement e, boolean fallback) {
        return isPrim(e) ? e.getAsBoolean() : fallback;
    }

    private static int asInt(@Nullable JsonElement e, int fallback) {
        try {
            return isNumber(e) ? e.getAsInt() : fallback;
        } catch (Exception ex) {
            return fallback;
        }
    }

    private static long asLong(@Nullable JsonElement e, long fallback) {
        try {
            return isNumber(e) ? e.getAsLong() : fallback;
        } catch (Exception ex) {
            return fallback;
        }
    }

    private static float asFloat(@Nullable JsonElement e, float fallback) {
        try {
            return isNumber(e) ? e.getAsFloat() : fallback;
        } catch (Exception ex) {
            return fallback;
        }
    }

    private static double asDouble(@Nullable JsonElement e, double fallback) {
        try {
            return isNumber(e) ? e.getAsDouble() : fallback;
        } catch (Exception ex) {
            return fallback;
        }
    }

    private static String asString(@Nullable JsonElement e, String fallback) {
        return isPrim(e) ? e.getAsString() : fallback;
    }

    private static int asColor(@Nullable JsonElement e, int fallback) {
        if (!isPrim(e)) return fallback;
        JsonPrimitive p = e.getAsJsonPrimitive();
        if (p.isNumber()) return p.getAsInt();
        if (p.isString()) {
            try {
                return ColorUtils.parseHex(p.getAsString().trim());
            } catch (Exception ex) {
                return fallback;
            }
        }
        return fallback;
    }

    private static boolean isPrim(@Nullable JsonElement e) {
        return e != null && e.isJsonPrimitive();
    }

    private static boolean isNumber(@Nullable JsonElement e) {
        return e != null && e.isJsonPrimitive() && e.getAsJsonPrimitive().isNumber();
    }

    private static JsonElement emptyFor(Schema<?> schema) {
        return switch (schema) {
            case Schema.ListOf<?> ignored -> new JsonArray();
            case Schema.MapOf<?, ?> ignored -> new JsonObject();
            case Schema.OneOf<?> ignored -> new JsonObject();
            case Schema.Record<?> ignored -> new JsonObject();
            default -> JsonNull.INSTANCE;
        };
    }

    private static int neutralInt(int min, int max) {
        return Math.clamp(0, min, max);
    }

    private static float neutralFloat(float min, float max) {
        return Math.clamp(0f, min, max);
    }

    private static double neutralDouble(double min, double max) {
        return Math.clamp(0d, min, max);
    }

    private static boolean isLongInRange(String s, long min, long max) {
        try {
            long v = Long.parseLong(s.trim());
            return v >= min && v <= max;
        } catch (Exception e) {
            return false;
        }
    }

    private static long parseLongOr(String s, long fallback) {
        try {
            return Long.parseLong(s.trim());
        } catch (Exception e) {
            return fallback;
        }
    }
}

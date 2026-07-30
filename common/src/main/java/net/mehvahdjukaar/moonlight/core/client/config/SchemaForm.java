package net.mehvahdjukaar.moonlight.core.client.config;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import net.mehvahdjukaar.codecui.Schema;
import net.mehvahdjukaar.codecui.SchemaCodecs;
import net.mehvahdjukaar.moonlight.api.client.gui.ConfigEditSession;
import net.mehvahdjukaar.moonlight.api.platform.configs.options.ConfigCategory;
import net.mehvahdjukaar.moonlight.api.platform.configs.options.ConfigOption;
import net.mehvahdjukaar.moonlight.api.resources.assets.LangBuilder;
import net.mehvahdjukaar.moonlight.api.util.Utils;
import net.mehvahdjukaar.moonlight.api.util.math.ColorUtils;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

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
 * <p>Structural kinds map to rich rows: records become navigable sub-categories, lists become sub-categories with one
 * entry per element and add/remove controls, primitives/enums/colours/ids become their matching controls. Anything the
 * form can't render structurally — maps, pairs, alternatives, opaque or recursive codecs — degrades to a raw-JSON row
 * (the existing {@link ConfigOption.JsonValue} editor) for that node, so the form never fails to represent a value; at
 * worst a sub-tree is edited as JSON text.</p>
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

    // one sorted id list per built-in registry, shared by every field naming it: a list config with many entries would
    // otherwise re-enumerate and re-sort a whole registry once per row. Dynamic ones aren't cached, they follow the world
    private static final Map<ResourceKey<? extends Registry<?>>, List<String>> ID_CACHE = new HashMap<>();

    final ConfigCategory root;
    final Reader reader;

    private SchemaForm(ConfigCategory root, Reader reader) {
        this.root = root;
        this.reader = reader;
    }

    /**
     * Builds the form for a whole config value. {@code current} is the value's JSON, {@code defaults} the encoded
     * default value's JSON (used to seed absent optional fields). A top-level record or list <em>is</em> the root
     * category; any other kind becomes a single row named "value" on an otherwise empty one.
     */
    static SchemaForm build(Component title, Schema<?> schema, JsonElement current, @Nullable JsonElement defaults) {
        // a record or a list becomes the root page itself; anything else gets a single "value" row on an empty page
        if (schema instanceof Schema.Record<?> rec) {
            ConfigCategory root = new ConfigCategory(title);
            return new SchemaForm(root, populateRecord(root, rec, current, defaults));
        }
        if (schema instanceof Schema.ListOf<?> list) {
            ListCategory root = listCategory(title, list, current, defaults);
            return new SchemaForm(root, root.reader());
        }
        ConfigCategory root = new ConfigCategory(title);
        return new SchemaForm(root, buildField(root, "value", readable("value"), schema, current, defaults));
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
                yield s -> new JsonPrimitive(s.current(opt));
            }
            case Schema.IntRange r -> {
                int v = Math.clamp(asInt(seed, neutralInt(r.min(), r.max())), r.min(), r.max());
                var opt = new ConfigOption.IntValue(title, null, new MemoryConfigValue<>(v), v, r.min(), r.max());
                parent.add(opt);
                yield s -> new JsonPrimitive(s.current(opt));
            }
            case Schema.FloatRange r -> {
                float v = Math.clamp(asFloat(seed, neutralFloat(r.min(), r.max())), r.min(), r.max());
                var opt = new ConfigOption.FloatValue(title, null, new MemoryConfigValue<>(v), v, r.min(), r.max());
                parent.add(opt);
                yield s -> new JsonPrimitive(s.current(opt));
            }
            case Schema.DoubleRange r -> {
                double v = Math.clamp(asDouble(seed, neutralDouble(r.min(), r.max())), r.min(), r.max());
                var opt = new ConfigOption.DoubleValue(title, null, new MemoryConfigValue<>(v), v, r.min(), r.max());
                parent.add(opt);
                yield s -> new JsonPrimitive(s.current(opt));
            }
            case Schema.LongRange r -> {
                // no dedicated long control: a numeric text field whose reader parses back to a JSON long
                long v = Math.clamp(asLong(seed, 0L), r.min(), r.max());
                String sv = Long.toString(v);
                Predicate<Object> valid = o -> o instanceof String str && isLongInRange(str, r.min(), r.max());
                var opt = new ConfigOption.StringValue(title, null, new MemoryConfigValue<>(sv), sv, valid);
                parent.add(opt);
                yield s -> new JsonPrimitive(parseLongOr(s.current(opt), v));
            }
            case Schema.Color c -> {
                int rgb = asColor(seed, 0xFFFFFFFF);
                var opt = new ConfigOption.ColorValue(title, null, new MemoryConfigValue<>(rgb), rgb, c.hasAlpha());
                parent.add(opt);
                yield s -> {
                    int col = s.current(opt);
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
                yield s -> new JsonPrimitive(s.current(opt));
            }
            case Schema.ResourceId id -> idField(parent, title, registryIds(id.registry()), asString(seed, ""),
                    o -> o instanceof String x && ResourceLocation.tryParse(x) != null, UnaryOperator.identity(),
                    iconsFor(id.registry()));
            case Schema.TagId tag -> {
                // "#namespace:path" when hashed, a bare id otherwise; accept either so a pasted id still works
                List<String> tags = SchemaCodecs.availableTagIds(tag.registry()).stream()
                        .map(t -> normalizeTagId(t.toString(), tag.hashed())).toList();
                // no icons: a tag id names a set, never a single item
                yield idField(parent, title, tags, asString(seed, ""),
                        o -> o instanceof String x && isTagId(x), s -> normalizeTagId(s, tag.hashed()), null);
            }
            case Schema.Enum<?> en -> enumField(parent, title, en, seed);
            case Schema.Record<?> rec -> {
                ConfigCategory sub = new ConfigCategory(title);
                parent.add(sub);
                yield populateRecord(sub, rec, current, def);
            }
            case Schema.ListOf<?> list -> {
                ListCategory sub = listCategory(title, list, seed, def);
                parent.add(sub);
                yield sub.reader();
            }
            // everything structural we don't render natively (maps, pairs, alternatives, opaque/custom/recursive)
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

    /**
     * A list node: a sub page holding one entry per element, each built through {@link #buildField} (so a record
     * element becomes its own page and a scalar element an inline row). Unlike a record the entry set is mutable, so
     * the page owns its readers and is rebuilt wholesale from a list of JSON values whenever an entry is added or
     * removed — {@link SchemaEditScreen} drives that and re-populates its rows.
     */
    static final class ListCategory extends ConfigCategory {

        private final Schema<?> element;
        private final JsonElement template; // seed for a newly added entry
        private final int min;
        private final int max;
        private final List<Reader> readers = new ArrayList<>();

        private ListCategory(Component title, Schema<?> element, JsonElement template, int min, int max) {
            super(title);
            this.element = element;
            this.template = template;
            this.min = min;
            this.max = max;
        }

        Reader reader() {
            return s -> {
                JsonArray array = new JsonArray();
                for (Reader r : readers) array.add(r.read(s));
                return array;
            };
        }

        /** Current JSON of every entry, as edited so far. The basis for any structural change. */
        List<JsonElement> snapshot(ConfigEditSession session) {
            List<JsonElement> out = new ArrayList<>(readers.size());
            for (Reader r : readers) out.add(r.read(session));
            return out;
        }

        /** Discards the existing entry rows and rebuilds them (and their readers) from {@code values}. */
        void setEntries(List<JsonElement> values) {
            clear();
            readers.clear();
            for (int i = 0; i < values.size(); i++) {
                readers.add(buildField(this, "entry" + i, entryTitle(i), element, values.get(i), template));
            }
        }

        JsonElement newEntry() {
            return template.deepCopy();
        }

        boolean canAdd() {
            return readers.size() < max;
        }

        boolean canRemove() {
            return readers.size() > min;
        }

        private static Component entryTitle(int index) {
            return Component.translatable("gui.moonlight.config.list_entry", index + 1);
        }
    }

    private static ListCategory listCategory(Component title, Schema.ListOf<?> list,
                                             @Nullable JsonElement current, @Nullable JsonElement def) {
        // a new entry is seeded from the default list's first element when there is one: far more useful than a
        // blank object, since it already has every required field filled in with something the codec accepts
        JsonElement template = def instanceof JsonArray a && !a.isEmpty() ? a.get(0) : emptyFor(list.element());
        ListCategory cat = new ListCategory(title, list.element(), template, list.min(), list.max());
        List<JsonElement> values = new ArrayList<>();
        if (current instanceof JsonArray a) a.forEach(values::add);
        cat.setEntries(values);
        return cat;
    }

    /**
     * A registry id or tag id: a searchable dropdown over the ids we can enumerate, degrading to a validated text
     * field when we can't (an unknown registry, or tags/dynamic registries with no world loaded). A current value
     * that isn't among them is kept as an option rather than dropped, so opening the screen while the mod that
     * defines it is absent can't silently rewrite the config.
     */
    private static Reader idField(ConfigCategory parent, Component title, List<String> known, String current,
                                  Predicate<Object> valid, UnaryOperator<String> normalize,
                                  @Nullable Function<String, ItemStack> icon) {
        if (known.isEmpty()) {
            var opt = new ConfigOption.StringValue(title, null, new MemoryConfigValue<>(current), current, valid);
            parent.add(opt);
            return s -> new JsonPrimitive(normalize.apply(s.current(opt)));
        }
        // the current value stays selectable even when it isn't one we can enumerate, empty included: picking the
        // first id for an absent field would quietly commit a real, wrong id where the old text box left it invalid
        List<String> options = known.contains(current) ? known
                : Stream.concat(Stream.of(current), known.stream()).toList();
        var opt = new ConfigOption.DropdownValue(title, null, new MemoryConfigValue<>(current), current,
                () -> options, icon);
        parent.add(opt);
        return s -> new JsonPrimitive(normalize.apply(s.current(opt)));
    }

    /**
     * The icon column, only for the two registries whose ids <em>are</em> the icon. Anything else (entity types,
     * effects, tags) would leave most rows blank while still paying for the taller row.
     */
    @Nullable
    private static Function<String, ItemStack> iconsFor(@Nullable ResourceKey<? extends Registry<?>> registry) {
        if (!Registries.ITEM.equals(registry) && !Registries.BLOCK.equals(registry)) return null;
        return id -> ConfigScreenIcons.resolve(ResourceLocation.tryParse(id));
    }

    /** Every id in a registry, sorted. Empty when it can't be reached (unknown, or dynamic with no world loaded). */
    private static List<String> registryIds(@Nullable ResourceKey<? extends Registry<?>> key) {
        if (key == null) return List.of();
        List<String> cached = ID_CACHE.get(key);
        if (cached != null) return cached;
        Registry<?> builtIn = BuiltInRegistries.REGISTRY.get(key.location());
        Registry<?> registry = builtIn != null ? builtIn : dynamicRegistry(key);
        if (registry == null) return List.of();
        List<String> ids = registry.keySet().stream().map(ResourceLocation::toString).sorted().toList();
        if (builtIn != null) ID_CACHE.put(key, ids);
        return ids;
    }

    @Nullable
    private static Registry<?> dynamicRegistry(ResourceKey<? extends Registry<?>> key) {
        // biomes, structures, ... only reachable with a world loaded, and the accessor throws outright when there
        // isn't one: a config screen opened from the main menu must survive that
        try {
            return Utils.hackyGetRegistryAccess().<Object>registry(key).orElse(null);
        } catch (Exception noWorld) {
            return null;
        }
    }

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

    private static String stripHash(String s) {
        return s.startsWith("#") ? s.substring(1) : s;
    }

    private static boolean isTagId(String s) {
        return ResourceLocation.tryParse(stripHash(s)) != null;
    }

    // The on-disk form is fixed by the codec (hashedCodec writes "#ns:path", codec writes "ns:path"), so whichever
    // way it was typed, write back the one the codec will accept.
    private static String normalizeTagId(String s, boolean hashed) {
        return hashed ? "#" + stripHash(s) : stripHash(s);
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

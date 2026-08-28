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
import net.mehvahdjukaar.moonlight.api.util.TextHelper;
import net.mehvahdjukaar.moonlight.api.util.Utils;
import net.mehvahdjukaar.moonlight.api.util.math.ColorUtils;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
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

final class SchemaForm {

    @FunctionalInterface
    interface Reader {
        JsonElement read(ConfigEditSession session);
    }

    // enumerating and sorting a registry per row is slow. Dynamic registries aren't cached
    private static final Map<ResourceKey<? extends Registry<?>>, List<String>> ID_CACHE = new HashMap<>();

    final ConfigCategory root;
    final Reader reader;

    private SchemaForm(ConfigCategory root, Reader reader) {
        this.root = root;
        this.reader = reader;
    }

    // current is the value's JSON, defaults the encoded default's (used to seed absent optional fields)
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

    // adds one row (or sub-category) for a field and returns the reader producing its JSON
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
                    o -> o instanceof String x && Identifier.tryParse(x) != null, UnaryOperator.identity(),
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
            // maps, pairs, alternatives, opaque/custom/recursive: edited as raw JSON
            default -> rawJsonField(parent, title, schema, seed);
        };
    }

    // one entry per record field; the returned reader assembles their JSON object
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

    // the entry set is mutable, so the page owns its readers and is rebuilt from the JSON list on add/remove
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

        // the JSON of every entry as edited so far. Adding or removing an entry starts from this
        List<JsonElement> snapshot(ConfigEditSession session) {
            List<JsonElement> out = new ArrayList<>(readers.size());
            for (Reader r : readers) out.add(r.read(session));
            return out;
        }

        // discards the existing entry rows and rebuilds them (and their readers) from the given values
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
        // the default list's first element already has every required field filled in
        JsonElement template = def instanceof JsonArray a && !a.isEmpty() ? a.get(0) : emptyFor(list.element());
        ListCategory cat = new ListCategory(title, list.element(), template, list.min(), list.max());
        List<JsonElement> values = new ArrayList<>();
        if (current instanceof JsonArray a) a.forEach(values::add);
        cat.setEntries(values);
        return cat;
    }

    // dropdown when the ids can be enumerated, validated text field otherwise
    private static Reader idField(ConfigCategory parent, Component title, List<String> known, String current,
                                  Predicate<Object> valid, UnaryOperator<String> normalize,
                                  @Nullable Function<String, ItemStack> icon) {
        if (known.isEmpty()) {
            var opt = new ConfigOption.StringValue(title, null, new MemoryConfigValue<>(current), current, valid);
            parent.add(opt);
            return s -> new JsonPrimitive(normalize.apply(s.current(opt)));
        }
        // keep the current value selectable, picking the first id for an absent field would commit a wrong id
        List<String> options = known.contains(current) ? known
                : Stream.concat(Stream.of(current), known.stream()).toList();
        var opt = new ConfigOption.DropdownValue(title, null, new MemoryConfigValue<>(current), current,
                () -> options, icon);
        parent.add(opt);
        return s -> new JsonPrimitive(normalize.apply(s.current(opt)));
    }

    // only item and block ids are their own icon
    @Nullable
    private static Function<String, ItemStack> iconsFor(@Nullable ResourceKey<? extends Registry<?>> registry) {
        if (!Registries.ITEM.equals(registry) && !Registries.BLOCK.equals(registry)) return null;
        return id -> ConfigScreenIcons.resolve(Identifier.tryParse(id));
    }

    // empty when the registry can't be reached (unknown, or dynamic with no world loaded)
    private static List<String> registryIds(@Nullable ResourceKey<? extends Registry<?>> key) {
        if (key == null) return List.of();
        List<String> cached = ID_CACHE.get(key);
        if (cached != null) return cached;
        Registry<?> builtIn = BuiltInRegistries.REGISTRY.getValue(key.identifier());
        Registry<?> registry = builtIn != null ? builtIn : dynamicRegistry(key);
        if (registry == null) return List.of();
        List<String> ids = registry.keySet().stream().map(Identifier::toString).sorted().toList();
        if (builtIn != null) ID_CACHE.put(key, ids);
        return ids;
    }

    @Nullable
    private static Registry<?> dynamicRegistry(ResourceKey<? extends Registry<?>> key) {
        // dynamic registries throw without a world loaded and the screen can be opened from the main menu
        try {
            return Utils.hackyGetRegistryAccess().<Object>lookup(key).orElse(null);
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
        // JsonValue is edited as a pretty-printed string, so parse it back and fall back to the seed if invalid
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

    private static Component readable(String name) {
        return Component.literal(TextHelper.getReadableName(name));
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
        return Identifier.tryParse(stripHash(s)) != null;
    }

    // write back the form the codec accepts (hashedCodec wants "#ns:path")
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

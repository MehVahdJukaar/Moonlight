package net.mehvahdjukaar.moonlight.core.client.config.schema;

import com.google.common.primitives.Longs;
import com.google.gson.*;
import net.mehvahdjukaar.codecui.Schema;
import net.mehvahdjukaar.codecui.SchemaCodecs;
import net.mehvahdjukaar.moonlight.api.client.gui.ConfigEditSession;
import net.mehvahdjukaar.moonlight.api.platform.configs.options.ConfigCategory;
import net.mehvahdjukaar.moonlight.api.platform.configs.options.ConfigNode;
import net.mehvahdjukaar.moonlight.api.platform.configs.options.ConfigOption;
import net.mehvahdjukaar.moonlight.api.util.TextHelper;
import net.mehvahdjukaar.moonlight.api.util.Utils;
import net.mehvahdjukaar.moonlight.api.util.math.ColorUtils;
import net.mehvahdjukaar.moonlight.core.client.config.ConfigScreenIcons;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.CommonColors;
import net.minecraft.util.GsonHelper;
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

record SchemaForm(ConfigCategory rootPage, FormPart editedValue) {

    @FunctionalInterface
    interface FormPart {
        JsonElement toJson(ConfigEditSession session);

        default JsonElement initialJson() {
            return toJson(NO_EDITS);
        }
    }

    record FormField(ConfigNode node, FormPart part) {

        FormField(SchemaCategory page) {
            this(page, page);
        }
    }

    private static final ConfigEditSession NO_EDITS = ConfigEditSession.scratch(null);

    private static final Map<ResourceKey<? extends Registry<?>>, List<String>> SORTED_BUILTIN_REGISTRY_IDS = new HashMap<>();

    static SchemaForm build(Component title, Schema<?> schema, JsonElement current, @Nullable JsonElement defaults) {
        FormField root = field(title, schema, current, defaults);
        if (root.node() instanceof ConfigCategory page) return new SchemaForm(page, root.part());

        // lone value gets page of its own
        ConfigCategory holder = new ConfigCategory(title);
        holder.add(root.node());

        return new SchemaForm(holder, root.part());
    }

    static FormPart addField(ConfigCategory parent, Component title, Schema<?> schema,
                             @Nullable JsonElement current, @Nullable JsonElement fallback) {
        return addTo(parent, field(title, schema, current, fallback));
    }

    static FormPart addTo(ConfigCategory parent, FormField field) {
        parent.add(field.node());
        return field.part();
    }

    static FormField field(Component title, Schema<?> schema, @Nullable JsonElement current, @Nullable JsonElement fallback) {
        JsonElement initial = hasValue(current) ? current : fallback;
        return switch (schema) {
            case Schema.Bool ignored -> {
                boolean v = isPrimitive(initial) && initial.getAsBoolean();
                yield option(new ConfigOption.BooleanOption(title, null, new InMemoryConfigValue<>(v), v), JsonPrimitive::new);
            }
            case Schema.IntRange r -> {
                int v = Math.clamp(asNumber(initial).intValue(), r.min(), r.max());
                yield option(new ConfigOption.IntOption(title, null, new InMemoryConfigValue<>(v), v, r.min(), r.max()), JsonPrimitive::new);
            }
            case Schema.FloatRange r -> {
                float v = Math.clamp(asNumber(initial).floatValue(), r.min(), r.max());
                yield option(new ConfigOption.FloatOption(title, null, new InMemoryConfigValue<>(v), v, r.min(), r.max()), JsonPrimitive::new);
            }
            case Schema.DoubleRange r -> {
                double v = Math.clamp(asNumber(initial).doubleValue(), r.min(), r.max());
                yield option(new ConfigOption.DoubleOption(title, null, new InMemoryConfigValue<>(v), v, r.min(), r.max()), JsonPrimitive::new);
            }
            case Schema.LongRange r -> {
                //no long control. text field it is
                long v = Math.clamp(asNumber(initial).longValue(), r.min(), r.max());
                Predicate<String> isLongInRange = s -> {
                    Long typed = Longs.tryParse(s.trim());
                    return typed != null && typed >= r.min() && typed <= r.max();
                };
                yield option(textOption(title, Long.toString(v), isLongInRange), s -> {
                    Long typed = Longs.tryParse(s.trim());
                    return new JsonPrimitive(typed != null ? typed : v);
                });
            }
            case Schema.Color c -> {
                int argb = asColor(initial, CommonColors.WHITE);
                yield option(new ConfigOption.ColorOption(title, null, new InMemoryConfigValue<>(argb), argb, c.hasAlpha()),
                        color -> c.hexString() ? new JsonPrimitive(ColorUtils.toHexString(color, c.hasAlpha())) : new JsonPrimitive(color));
            }
            case Schema.Str str -> {
                Predicate<String> fitsSchema = s -> s.length() >= str.minLen() && s.length() <= str.maxLen()
                        && (str.pattern() == null || str.pattern().matcher(s).matches());
                yield option(textOption(title, asString(initial, ""), fitsSchema), JsonPrimitive::new);
            }
            case Schema.Enum<?> en -> {
                List<String> labels = labelsOf(en);
                String first = labels.isEmpty() ? "" : labels.getFirst();
                String picked = asString(initial, first);
                yield option(dropdownOption(title, labels.contains(picked) ? picked : first, labels, null), JsonPrimitive::new);
            }
            case Schema.ResourceId id -> idField(title, registryIds(id.registry()), asString(initial, ""),
                    s -> ResourceLocation.tryParse(s) != null, UnaryOperator.identity(), itemIconsFor(id.registry()));
            case Schema.TagId tag -> {
                List<String> knownTags = SchemaCodecs.availableTagIds(tag.registry()).stream()
                        .map(t -> withCodecTagPrefix(t.toString(), tag.hashed())).toList();
                yield idField(title, knownTags, asString(initial, ""),
                        s -> ResourceLocation.tryParse(TextHelper.stripHash(s)) != null, s -> withCodecTagPrefix(s, tag.hashed()), null);
            }
            case Schema.Record<?> rec -> {
                ConfigCategory page = new ConfigCategory(title);
                yield new FormField(page, addRecordFields(page, rec, current, fallback));
            }
            case Schema.PairOf<?, ?> pair -> {
                ConfigCategory page = new ConfigCategory(title);
                yield new FormField(page, addPairFields(page, pair, initial));
            }
            case Schema.ListOf<?> list -> new FormField(SchemaCategory.ListEntries.of(title, list, initial, fallback));
            case Schema.MapOf<?, ?> map ->
                    new FormField(new SchemaCategory.MapEntries(title, map.key(), key -> map.value(), false, initial, fallback));
            case Schema.DispatchedMapOf<?, ?> map ->
                    new FormField(new SchemaCategory.MapEntries(title, map.key(), map.valueForKey(), true, initial, fallback));
            case Schema.OneOf<?> oneOf -> new FormField(SchemaCategory.Variant.oneOf(title, oneOf, initial));
            case Schema.AnyOf<?> anyOf -> new FormField(SchemaCategory.Variant.anyOf(title, anyOf, initial));
            // only follow a recursive schema while there is data, else it never ends
            case Schema.Ref<?> ref when ref.target() != null && hasValue(initial) ->
                    field(title, ref.target(), current, fallback);
            default -> rawJsonField(title, initial != null ? initial : emptyFor(schema));
        };
    }

    // a plain string list edited on the same page a schema list gets
    static SchemaCategory.ListEntries listOptionEntries(ConfigOption.ListOption option, List<String> current) {
        List<JsonElement> initial = current.stream().<JsonElement>map(JsonPrimitive::new).toList();
        if (option instanceof ConfigOption.ColorListOption colors) {
            Schema.Color hex = new Schema.Color(colors.hasAlpha, true);
            return new SchemaCategory.ListEntries(option.title(), (t, json) -> field(t, hex, json, null),
                    new JsonPrimitive(ColorUtils.toHexString(-1, colors.hasAlpha)), 0, Integer.MAX_VALUE, initial);
        }
        List<String> options = option.options == null ? null : option.options.get();
        SchemaCategory.ListEntries.EntryField entry = options == null
                ? (t, json) -> option(textOption(t, asString(json, ""), option::isValidEntry), JsonPrimitive::new)
                : (t, json) -> idField(t, options, asString(json, ""), option::isValidEntry, UnaryOperator.identity(), option.icon);
        String template = options != null && !options.isEmpty() ? options.getFirst() : "";
        return new SchemaCategory.ListEntries(option.title(), entry, new JsonPrimitive(template), 0, Integer.MAX_VALUE, initial);
    }


    static boolean opensPage(Schema<?> schema, @Nullable JsonElement initial) {
        if (schema instanceof Schema.Ref<?> ref) {
            return ref.target() != null && hasValue(initial) && opensPage(ref.target(), initial);
        }
        return schema instanceof Schema.Record<?> || schema instanceof Schema.PairOf<?, ?>
                || schema instanceof Schema.ListOf<?> || schema instanceof Schema.MapOf<?, ?>
                || schema instanceof Schema.DispatchedMapOf<?, ?>
                || schema instanceof Schema.OneOf<?> || schema instanceof Schema.AnyOf<?>;
    }

    private static <T> FormField option(ConfigOption<T> option, Function<T, JsonElement> toJson) {
        return new FormField(option, session -> toJson.apply(session.valueOrPendingValue(option)));
    }

    private static ConfigOption.StringOption textOption(Component title, String initial, Predicate<String> isValid) {
        return new ConfigOption.StringOption(title, null, new InMemoryConfigValue<>(initial), initial,
                o -> o instanceof String s && isValid.test(s));
    }

    static ConfigOption.DropdownOption dropdownOption(Component title, String initial, List<String> options,
                                                      @Nullable Function<String, ItemStack> icon) {
        return new ConfigOption.DropdownOption(title, null, new InMemoryConfigValue<>(initial), initial, () -> options, icon);
    }

    static FormPart addRecordFields(ConfigCategory page, Schema.Record<?> rec,
                                    @Nullable JsonElement current, @Nullable JsonElement fallback) {
        JsonObject currentObject = current instanceof JsonObject o ? o : null;
        JsonObject fallbackObject = fallback instanceof JsonObject o ? o : null;
        List<RecordField> fields = new ArrayList<>(rec.fields().size());

        for (Schema.Field<?, ?> f : rec.fields()) {
            JsonElement fieldCurrent = f.inline() ? currentObject : currentObject != null ? currentObject.get(f.name()) : null;
            JsonElement fieldFallback = f.inline() ? fallbackObject : fallbackObject != null ? fallbackObject.get(f.name()) : null;
            JsonElement declaredDefault = primitiveToJson(f.defaultValue());
            if (fieldFallback == null) {
                fieldFallback = declaredDefault;
            }

            FormPart value = addField(page, readable(f.name()), f.schema(), fieldCurrent, fieldFallback);
            boolean wasAbsent = f.optional() && fieldCurrent == null;
            fields.add(new RecordField(f, value, wasAbsent ? value.initialJson() : null, declaredDefault));
        }
        return session -> {
            JsonObject jo = new JsonObject();
            for (RecordField field : fields) {
                JsonElement json = field.value.toJson(session);
                if (field.isLeftOut(json)) continue;
                if (field.schema.inline() && json instanceof JsonObject inlined) {
                    jo.asMap().putAll(inlined.asMap());
                } else {
                    jo.add(field.schema.name(), json);
                }
            }
            return jo;
        };
    }

    private record RecordField(Schema.Field<?, ?> schema, FormPart value,
                               @Nullable JsonElement jsonWhenAbsentAndUntouched,
                               @Nullable JsonElement declaredDefault) {

        private boolean isLeftOut(@Nullable JsonElement json) {
            if (json == null) return true;
            if (!schema.optional()) return false;
            return json.isJsonNull() || json.equals(jsonWhenAbsentAndUntouched) || json.equals(declaredDefault);
        }
    }

    private static FormPart addPairFields(ConfigCategory page, Schema.PairOf<?, ?> pair, @Nullable JsonElement initial) {
        FormPart first = addPairHalf(page, "first", pair.first(), initial);
        FormPart second = addPairHalf(page, "second", pair.second(), initial);
        return session -> {
            JsonElement firstJson = first.toJson(session);
            JsonElement secondJson = second.toJson(session);

            if (firstJson instanceof JsonObject a && secondJson instanceof JsonObject b) {
                JsonObject merged = new JsonObject();
                merged.asMap().putAll(a.asMap());
                merged.asMap().putAll(b.asMap());
                return merged;
            }
            return secondJson.isJsonNull() ? firstJson : secondJson;
        };
    }

    private static FormPart addPairHalf(ConfigCategory page, String name, Schema<?> half, @Nullable JsonElement initial) {
        if (half instanceof Schema.Record<?> rec) return addRecordFields(page, rec, initial, null);
        return addField(page, readable(name), half, initial, null);
    }

    private static FormField idField(Component title, List<String> knownIds, String current,
                                     Predicate<String> isValidId, UnaryOperator<String> toCodecForm,
                                     @Nullable Function<String, ItemStack> icon) {
        if (knownIds.isEmpty()) {
            return option(textOption(title, current, isValidId), s -> new JsonPrimitive(toCodecForm.apply(s)));
        }
        //never swap an unknown or empty id for the first known one
        List<String> idsKeepingCurrent = knownIds.contains(current) ? knownIds
                : Stream.concat(Stream.of(current), knownIds.stream()).toList();
        return option(dropdownOption(title, current, idsKeepingCurrent, icon), s -> new JsonPrimitive(toCodecForm.apply(s)));
    }

    @Nullable
    private static Function<String, ItemStack> itemIconsFor(@Nullable ResourceKey<? extends Registry<?>> registry) {
        if (!Registries.ITEM.equals(registry) && !Registries.BLOCK.equals(registry)) return null;
        return id -> ConfigScreenIcons.resolve(ResourceLocation.tryParse(id));
    }

    private static List<String> registryIds(@Nullable ResourceKey<? extends Registry<?>> key) {
        if (key == null) return List.of();
        List<String> cached = SORTED_BUILTIN_REGISTRY_IDS.get(key);
        if (cached != null) return cached;

        Registry<?> builtIn = BuiltInRegistries.REGISTRY.get(key.location());
        Registry<?> registry = builtIn != null ? builtIn : dynamicRegistry(key);

        if (registry == null) return List.of();

        List<String> ids = registry.keySet().stream().map(ResourceLocation::toString).sorted().toList();
        if (builtIn != null) SORTED_BUILTIN_REGISTRY_IDS.put(key, ids);
        return ids;
    }

    @Nullable
    private static Registry<?> dynamicRegistry(ResourceKey<? extends Registry<?>> key) {
        try {
            return Utils.hackyGetRegistryAccess().registry(key).orElse(null);
        } catch (Exception openedFromMainMenu) {
            return null;
        }
    }

    private static <A> List<String> labelsOf(Schema.Enum<A> en) {
        return en.options().stream().map(en.label()).toList();
    }

    static FormField rawJsonField(Component title, JsonElement initial) {
        return option(new ConfigOption.JsonOption(title, null, () -> initial), text -> {
            try {
                return JsonParser.parseString(text);
            } catch (Exception invalidJson) {
                return initial;
            }
        });
    }

    static Component readable(String name) {
        return Component.literal(TextHelper.getReadableName(name));
    }

    static boolean hasValue(@Nullable JsonElement json) {
        return json != null && !json.isJsonNull();
    }

    static boolean isPrimitive(@Nullable JsonElement json) {
        return json != null && json.isJsonPrimitive();
    }

    private static Number asNumber(@Nullable JsonElement json) {
        return json != null && GsonHelper.isNumberValue(json) ?
                json.getAsNumber() : 0;
    }

    static String asString(@Nullable JsonElement json, String fallback) {
        return isPrimitive(json) ? json.getAsString() : fallback;
    }

    private static int asColor(@Nullable JsonElement json, int fallback) {
        if (!isPrimitive(json)) return fallback;
        JsonPrimitive p = json.getAsJsonPrimitive();
        if (p.isNumber()) return p.getAsInt();

        try {
            return ColorUtils.parseHex(p.getAsString().trim());
        } catch (Exception notHex) {
            return fallback;
        }
    }

    private static String withCodecTagPrefix(String tagId, boolean hashed) {
        return hashed ? "#" + TextHelper.stripHash(tagId) : TextHelper.stripHash(tagId);
    }

    @Nullable
    private static JsonElement primitiveToJson(@Nullable Object value) {
        if (value instanceof Boolean b) return new JsonPrimitive(b);
        if (value instanceof Number n) return new JsonPrimitive(n);
        if (value instanceof String str) return new JsonPrimitive(str);
        return null;
    }


    static JsonElement emptyFor(Schema<?> schema) {
        return switch (schema) {
            case Schema.ListOf<?> ignored -> new JsonArray();
            case Schema.MapOf<?, ?> ignored -> new JsonObject();
            case Schema.DispatchedMapOf<?, ?> ignored -> new JsonObject();
            case Schema.OneOf<?> ignored -> new JsonObject();
            case Schema.Record<?> ignored -> new JsonObject();
            default -> JsonNull.INSTANCE;
        };
    }
}

package net.mehvahdjukaar.moonlight.core.client.config.schema;

import com.google.common.primitives.Longs;
import com.google.gson.*;
import net.mehvahdjukaar.codecui.Schema;
import net.mehvahdjukaar.codecui.SchemaCodecs;
import net.mehvahdjukaar.moonlight.api.client.gui.ConfigEditSession;
import net.mehvahdjukaar.moonlight.api.platform.configs.options.ConfigCategory;
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

    private static final ConfigEditSession NO_EDITS = ConfigEditSession.scratch(null);

    private static final Map<ResourceKey<? extends Registry<?>>, List<String>> SORTED_BUILTIN_REGISTRY_IDS = new HashMap<>();

    static SchemaForm build(Component title, Schema<?> schema, JsonElement current, @Nullable JsonElement defaults) {
        ConfigCategory holder = new ConfigCategory(title);
        FormPart value = addField(holder, title, schema, current, defaults);
        if (holder.entries().getFirst() instanceof ConfigCategory onlyPage) return new SchemaForm(onlyPage, value);
        return new SchemaForm(holder, value);
    }

    static FormPart addField(ConfigCategory parent, Component title, Schema<?> schema,
                             @Nullable JsonElement current, @Nullable JsonElement fallback) {
        JsonElement initial = hasValue(current) ? current : fallback;
        return switch (schema) {
            case Schema.Bool ignored -> {
                boolean v = isPrimitive(initial) && initial.getAsBoolean();
                yield addOption(parent, new ConfigOption.BooleanValue(title, null, new InMemoryConfigValue<>(v), v), JsonPrimitive::new);
            }
            case Schema.IntRange r -> {
                int v = Math.clamp(asNumber(initial).intValue(), r.min(), r.max());
                yield addOption(parent, new ConfigOption.IntValue(title, null, new InMemoryConfigValue<>(v), v, r.min(), r.max()), JsonPrimitive::new);
            }
            case Schema.FloatRange r -> {
                float v = Math.clamp(asNumber(initial).floatValue(), r.min(), r.max());
                yield addOption(parent, new ConfigOption.FloatValue(title, null, new InMemoryConfigValue<>(v), v, r.min(), r.max()), JsonPrimitive::new);
            }
            case Schema.DoubleRange r -> {
                double v = Math.clamp(asNumber(initial).doubleValue(), r.min(), r.max());
                yield addOption(parent, new ConfigOption.DoubleValue(title, null, new InMemoryConfigValue<>(v), v, r.min(), r.max()), JsonPrimitive::new);
            }
            case Schema.LongRange r -> {
                //no long control. text field it is
                long v = Math.clamp(asNumber(initial).longValue(), r.min(), r.max());
                Predicate<String> isLongInRange = s -> {
                    Long typed = Longs.tryParse(s.trim());
                    return typed != null && typed >= r.min() && typed <= r.max();
                };
                yield addOption(parent, textOption(title, Long.toString(v), isLongInRange), s -> {
                    Long typed = Longs.tryParse(s.trim());
                    return new JsonPrimitive(typed != null ? typed : v);
                });
            }
            case Schema.Color c -> {
                int argb = asColor(initial, CommonColors.WHITE);
                yield addOption(parent, new ConfigOption.ColorValue(title, null, new InMemoryConfigValue<>(argb), argb, c.hasAlpha()),
                        color -> c.hexString() ? new JsonPrimitive(ColorUtils.toHexString(color, c.hasAlpha())) : new JsonPrimitive(color));
            }
            case Schema.Str str -> {
                Predicate<String> fitsSchema = s -> s.length() >= str.minLen() && s.length() <= str.maxLen()
                        && (str.pattern() == null || str.pattern().matcher(s).matches());
                yield addOption(parent, textOption(title, asString(initial, ""), fitsSchema), JsonPrimitive::new);
            }
            case Schema.Enum<?> en -> {
                List<String> labels = labelsOf(en);
                String first = labels.isEmpty() ? "" : labels.getFirst();
                String picked = asString(initial, first);
                yield addOption(parent, dropdownOption(title, labels.contains(picked) ? picked : first, labels, null), JsonPrimitive::new);
            }
            case Schema.ResourceId id -> addIdField(parent, title, registryIds(id.registry()), asString(initial, ""),
                    s -> ResourceLocation.tryParse(s) != null, UnaryOperator.identity(), itemIconsFor(id.registry()));
            case Schema.TagId tag -> {
                List<String> knownTags = SchemaCodecs.availableTagIds(tag.registry()).stream()
                        .map(t -> withCodecTagPrefix(t.toString(), tag.hashed())).toList();
                yield addIdField(parent, title, knownTags, asString(initial, ""),
                        s -> ResourceLocation.tryParse(TextHelper.stripHash(s)) != null, s -> withCodecTagPrefix(s, tag.hashed()), null);
            }
            case Schema.Record<?> rec ->
                    addRecordFields(addPage(parent, new ConfigCategory(title)), rec, current, fallback);
            case Schema.PairOf<?, ?> pair -> addPairFields(addPage(parent, new ConfigCategory(title)), pair, initial);
            case Schema.ListOf<?> list ->
                    addPage(parent, new SchemaCategory.ListEntries(title, list, initial, fallback));
            case Schema.MapOf<?, ?> map ->
                    addPage(parent, new SchemaCategory.MapEntries(title, map.key(), key -> map.value(), false, initial, fallback));
            case Schema.DispatchedMapOf<?, ?> map ->
                    addPage(parent, new SchemaCategory.MapEntries(title, map.key(), map.valueForKey(), true, initial, fallback));
            case Schema.OneOf<?> oneOf -> addPage(parent, SchemaCategory.Variant.oneOf(title, oneOf, initial));
            case Schema.AnyOf<?> anyOf -> addPage(parent, SchemaCategory.Variant.anyOf(title, anyOf, initial));
            // only follow a recursive schema while there is data, else it never ends
            case Schema.Ref<?> ref when ref.target() != null && hasValue(initial) ->
                    addField(parent, title, ref.target(), current, fallback);
            default -> addRawJsonField(parent, title, initial != null ? initial : emptyFor(schema));
        };
    }

    private static <T> FormPart addOption(ConfigCategory parent, ConfigOption<T> option, Function<T, JsonElement> toJson) {
        parent.add(option);
        return session -> toJson.apply(session.valueOrPendingValue(option));
    }

    private static <C extends ConfigCategory> C addPage(ConfigCategory parent, C page) {
        parent.add(page);
        return page;
    }

    private static ConfigOption.StringValue textOption(Component title, String initial, Predicate<String> isValid) {
        return new ConfigOption.StringValue(title, null, new InMemoryConfigValue<>(initial), initial,
                o -> o instanceof String s && isValid.test(s));
    }

    static ConfigOption.DropdownValue dropdownOption(Component title, String initial, List<String> options,
                                                     @Nullable Function<String, ItemStack> icon) {
        return new ConfigOption.DropdownValue(title, null, new InMemoryConfigValue<>(initial), initial, () -> options, icon);
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
            if (fieldFallback == null) fieldFallback = declaredDefault;
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

        boolean isLeftOut(@Nullable JsonElement json) {
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

    private static FormPart addIdField(ConfigCategory parent, Component title, List<String> knownIds, String current,
                                       Predicate<String> isValidId, UnaryOperator<String> toCodecForm,
                                       @Nullable Function<String, ItemStack> icon) {
        if (knownIds.isEmpty()) {
            return addOption(parent, textOption(title, current, isValidId), s -> new JsonPrimitive(toCodecForm.apply(s)));
        }
        // never swap an unknown or empty id for the first known one
        List<String> idsKeepingCurrent = knownIds.contains(current) ? knownIds
                : Stream.concat(Stream.of(current), knownIds.stream()).toList();
        return addOption(parent, dropdownOption(title, current, idsKeepingCurrent, icon), s -> new JsonPrimitive(toCodecForm.apply(s)));
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

    static FormPart addRawJsonField(ConfigCategory parent, Component title, JsonElement initial) {
        return addOption(parent, new ConfigOption.JsonValue(title, null, () -> initial), text -> {
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
            case Schema.OneOf<?> ignored -> new JsonObject();
            case Schema.Record<?> ignored -> new JsonObject();
            default -> JsonNull.INSTANCE;
        };
    }
}

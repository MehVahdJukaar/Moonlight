package net.mehvahdjukaar.moonlight.core.client.config.schema;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import net.mehvahdjukaar.codecui.Schema;
import net.mehvahdjukaar.moonlight.api.client.gui.ConfigEditSession;
import net.mehvahdjukaar.moonlight.api.platform.configs.options.ConfigCategory;
import net.mehvahdjukaar.moonlight.api.platform.configs.options.ConfigOption;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

import static net.mehvahdjukaar.moonlight.core.client.config.schema.SchemaForm.*;

// a page whose rows can change while it is being edited
abstract class SchemaCategory extends ConfigCategory implements FormPart {

    SchemaCategory(Component title) {
        super(title);
    }

    boolean rebuildRowsIfSchemaChanged(ConfigEditSession session) {
        return false;
    }

    abstract static class Entries<E> extends SchemaCategory {

        Entries(Component title) {
            super(title);
        }

        abstract List<E> editedEntries(ConfigEditSession session);

        abstract void rebuildRows(List<E> entries);

        abstract E newEntry();

        boolean canAdd() {
            return true;
        }

        boolean canRemove() {
            return true;
        }

        void addEntry(ConfigEditSession session) {
            List<E> entries = editedEntries(session);
            entries.add(newEntry());
            rebuildRows(entries);
        }

        void removeEntry(ConfigEditSession session, int index) {
            List<E> entries = editedEntries(session);
            if (index >= entries.size()) return;
            entries.remove(index);
            rebuildRows(entries);
        }
    }

    static final class ListEntries extends Entries<JsonElement> {

        private final Schema.ListOf<?> schema;
        private final JsonElement newEntryTemplate;
        private final List<FormPart> entryValues = new ArrayList<>();

        ListEntries(Component title, Schema.ListOf<?> schema, @Nullable JsonElement initial, @Nullable JsonElement fallback) {
            super(title);
            this.schema = schema;
            //first default entry already has every required field filled with something the codec accepts
            this.newEntryTemplate = fallback instanceof JsonArray a && !a.isEmpty() ? a.get(0) : emptyFor(schema.element());
            rebuildRows(initial instanceof JsonArray a ? a.asList() : List.of());
        }

        @Override
        public JsonElement toJson(ConfigEditSession session) {
            JsonArray array = new JsonArray();
            for (JsonElement entry : editedEntries(session)) array.add(entry);
            return array;
        }

        @Override
        List<JsonElement> editedEntries(ConfigEditSession session) {
            List<JsonElement> entries = new ArrayList<>(entryValues.size());
            for (FormPart v : entryValues) entries.add(v.toJson(session));
            return entries;
        }

        @Override
        void rebuildRows(List<JsonElement> entries) {
            clear();
            entryValues.clear();
            for (int i = 0; i < entries.size(); i++) {
                Component entryTitle = Component.translatable("gui.moonlight.config.list_entry", i + 1);
                entryValues.add(addField(this, entryTitle, schema.element(), entries.get(i), newEntryTemplate));
            }
        }

        @Override
        JsonElement newEntry() {
            return newEntryTemplate.deepCopy();
        }

        @Override
        boolean canAdd() {
            return entryValues.size() < schema.max();
        }

        @Override
        boolean canRemove() {
            return entryValues.size() > schema.min();
        }
    }

    static final class MapEntries extends Entries<Map.Entry<String, JsonElement>> {

        private static final Component KEY_TITLE = readable("key");
        private static final Component EDIT_TITLE = Component.translatable("gui.moonlight.config.edit");

        private final Schema<?> keyFieldSchema;
        private final Function<String, Schema<?>> valueSchemaForKey;
        private final boolean valueSchemaDependsOnKey;
        @Nullable
        private final JsonElement newValueTemplate;
        private final List<KeyedRow> rows = new ArrayList<>();

        private record KeyedRow(ConfigOption<?> keyOption, FormPart key, Schema<?> valueSchema, FormPart value) {
            String typedKey(ConfigEditSession session) {
                return key.toJson(session).getAsString();
            }
        }

        MapEntries(Component title, Schema<?> keySchema, Function<String, Schema<?>> valueSchemaForKey, boolean valueSchemaDependsOnKey,
                   @Nullable JsonElement initial, @Nullable JsonElement fallback) {
            super(title);
            this.keyFieldSchema = isTextLike(keySchema) ? keySchema : Schema.str();
            this.valueSchemaForKey = valueSchemaForKey;
            this.valueSchemaDependsOnKey = valueSchemaDependsOnKey;
            if (valueSchemaDependsOnKey) {
                this.newValueTemplate = null;
            } else {
                this.newValueTemplate = fallback instanceof JsonObject o && !o.isEmpty()
                        ? o.entrySet().iterator().next().getValue()
                        : emptyFor(valueSchemaForKey.apply(""));
            }
            rebuildRows(initial instanceof JsonObject o ? List.copyOf(o.entrySet()) : List.of());
        }

        @Override
        public JsonElement toJson(ConfigEditSession session) {
            JsonObject object = new JsonObject();
            for (var entry : editedEntries(session)) object.add(entry.getKey(), entry.getValue());
            return object;
        }

        ConfigOption<?> keyOption(int index) {
            return rows.get(index).keyOption;
        }

        boolean isDuplicateKey(ConfigEditSession session, int index) {
            if (index >= rows.size()) return false;
            String key = rows.get(index).typedKey(session);
            for (int i = 0; i < rows.size(); i++) {
                if (i != index && rows.get(i).typedKey(session).equals(key)) return true;
            }
            return false;
        }

        @Override
        List<Map.Entry<String, JsonElement>> editedEntries(ConfigEditSession session) {
            List<Map.Entry<String, JsonElement>> entries = new ArrayList<>(rows.size());
            for (KeyedRow row : rows) entries.add(Map.entry(row.typedKey(session), row.value.toJson(session)));
            return entries;
        }

        @Override
        void rebuildRows(List<Map.Entry<String, JsonElement>> entries) {
            clear();
            rows.clear();
            for (var entry : entries) {
                ConfigCategory keyHolder = new ConfigCategory(KEY_TITLE);
                FormPart key = addField(keyHolder, KEY_TITLE, keyFieldSchema, new JsonPrimitive(entry.getKey()), null);
                Schema<?> valueSchema = valueSchemaForKey.apply(entry.getKey());
                Component valueTitle = opensPage(valueSchema) ? EDIT_TITLE : Component.empty();
                FormPart value = addField(this, valueTitle, valueSchema, entry.getValue(), newValueTemplate);
                rows.add(new KeyedRow((ConfigOption<?>) keyHolder.entries().getFirst(), key, valueSchema, value));
            }
        }

        @Override
        Map.Entry<String, JsonElement> newEntry() {
            return Map.entry("", newValueTemplate != null ? newValueTemplate.deepCopy() : JsonNull.INSTANCE);
        }

        @Override
        boolean rebuildRowsIfSchemaChanged(ConfigEditSession session) {
            if (!valueSchemaDependsOnKey) return false;
            for (KeyedRow row : rows) {
                Schema<?> schemaForTypedKey = valueSchemaForKey.apply(row.typedKey(session));
                if (!Objects.equals(schemaForTypedKey, row.valueSchema)) {
                    rebuildRows(editedEntries(session));
                    return true;
                }
            }
            return false;
        }

        private static boolean isTextLike(Schema<?> schema) {
            return schema instanceof Schema.Str || schema instanceof Schema.ResourceId
                    || schema instanceof Schema.TagId || schema instanceof Schema.Enum<?>;
        }

        private static boolean opensPage(Schema<?> schema) {
            return schema instanceof Schema.Record<?> || schema instanceof Schema.PairOf<?, ?>
                    || schema instanceof Schema.ListOf<?> || schema instanceof Schema.MapOf<?, ?>
                    || schema instanceof Schema.DispatchedMapOf<?, ?>
                    || schema instanceof Schema.OneOf<?> || schema instanceof Schema.AnyOf<?>;
        }
    }

    static final class Variant extends SchemaCategory {

        private static final Component TYPE_TITLE = Component.translatable("gui.moonlight.config.schema_type");
        private static final int NO_FIT = 0, SAME_JSON_KIND = 1, EXACT_FIT = 2;

        private final List<String> labels;
        private final Function<String, Schema<?>> schemaForLabel;
        @Nullable
        private final String typeField;
        @Nullable
        private final String valueField;
        private ConfigOption.DropdownValue selector;
        private String selected;
        private FormPart body;
        private boolean bodyIsInline;

        private Variant(Component title, List<String> labels, Function<String, Schema<?>> schemaForLabel,
                        @Nullable String typeField, @Nullable String valueField) {
            super(title);
            this.labels = labels;
            this.schemaForLabel = schemaForLabel;
            this.typeField = typeField;
            this.valueField = valueField;
        }

        static Variant oneOf(Component title, Schema.OneOf<?> oneOf, @Nullable JsonElement initial) {
            List<String> labels = new ArrayList<>(oneOf.variants().keySet());
            String typeInJson = initial instanceof JsonObject o ? asString(o.get(oneOf.typeField()), "") : "";
            String type = matchingVariant(typeInJson, labels);
            if (!labels.contains(type)) {
                if (type.isEmpty() && !labels.isEmpty()) type = labels.getFirst();
                else labels.addFirst(type);
            }
            Map<String, ? extends Schema<?>> variants = oneOf.variants();
            var page = new Variant(title, labels, variants::get, oneOf.typeField(), oneOf.valueField());
            page.selectVariant(type, initial);
            return page;
        }

        static Variant anyOf(Component title, Schema.AnyOf<?> anyOf, @Nullable JsonElement initial) {
            Map<String, Schema<?>> schemaByLabel = new LinkedHashMap<>();
            for (Schema.AnyOf.Option o : anyOf.options()) {
                schemaByLabel.putIfAbsent(o.label() != null ? o.label() : Schema.kindName(o.schema()), o.schema());
            }
            List<String> labels = new ArrayList<>(schemaByLabel.keySet());
            String bestLabel = labels.getFirst();
            int bestFit = NO_FIT;
            for (var e : schemaByLabel.entrySet()) {
                int fit = howWellItFits(e.getValue(), initial);
                if (fit > bestFit) {
                    bestFit = fit;
                    bestLabel = e.getKey();
                }
                if (fit == EXACT_FIT) break;
            }
            var page = new Variant(title, labels, schemaByLabel::get, null, null);
            page.selectVariant(bestLabel, initial);
            return page;
        }

        @Override
        public JsonElement toJson(ConfigEditSession session) {
            JsonElement bodyJson = body.toJson(session);
            if (typeField == null) return bodyJson;
            JsonObject object = new JsonObject();
            object.addProperty(typeField, selected);
            if (bodyIsInline) {
                if (bodyJson instanceof JsonObject bodyFields) {
                    for (var e : bodyFields.entrySet()) {
                        if (!e.getKey().equals(typeField)) object.add(e.getKey(), e.getValue());
                    }
                }
            } else {
                boolean isEmptyOptionalBody = valueField != null && bodyJson instanceof JsonObject o && o.isEmpty();
                if (!isEmptyOptionalBody) object.add(bodyFieldName(), bodyJson);
            }
            return object;
        }

        private String bodyFieldName() {
            return valueField != null ? valueField : "value";
        }

        private void selectVariant(String label, @Nullable JsonElement initial) {
            clear();
            this.selected = label;
            this.selector = dropdownOption(TYPE_TITLE, label, labels, null);
            add(selector);

            Schema<?> schema = schemaForLabel.apply(label);
            boolean isRecordBesideTypeField = schema instanceof Schema.Record<?> && valueField == null;
            this.bodyIsInline = schema == null || typeField == null || isRecordBesideTypeField;
            if (!bodyIsInline) {
                JsonElement nestedInitial = initial instanceof JsonObject o ? o.get(bodyFieldName()) : null;
                this.body = addField(this, readable(bodyFieldName()), schema, nestedInitial, null);
            } else if (schema == null) {
                this.body = addRawJsonField(this, readable("value"), initial != null ? initial : JsonNull.INSTANCE);
            } else if (schema instanceof Schema.Record<?> rec) {
                this.body = addRecordFields(this, rec, initial, null);
            } else {
                this.body = addField(this, readable("value"), schema, initial, null);
            }
        }

        @Override
        boolean rebuildRowsIfSchemaChanged(ConfigEditSession session) {
            String picked = session.valueOrPendingValue(selector);
            if (picked.equals(selected)) return false;
            selectVariant(picked, toJson(session));
            return true;
        }

        //"stone" and "minecraft:stone" are the same variant
        private static String matchingVariant(String type, List<String> variants) {
            if (variants.contains(type)) return type;
            ResourceLocation typeId = ResourceLocation.tryParse(type);
            if (typeId == null) return type;
            for (String variant : variants) {
                if (typeId.equals(ResourceLocation.tryParse(variant))) return variant;
            }
            return type;
        }

        private static int howWellItFits(Schema<?> option, @Nullable JsonElement json) {
            if (!hasValue(json)) return NO_FIT;
            JsonElement roundTripped;
            try {
                roundTripped = addField(new ConfigCategory(Component.empty()), Component.empty(), option, json, null).initialJson();
            } catch (Exception e) {
                return NO_FIT;
            }
            if (json.equals(roundTripped)) return EXACT_FIT;
            return roundTripped != null && roundTripped.getClass() == json.getClass() ? SAME_JSON_KIND : NO_FIT;
        }
    }
}

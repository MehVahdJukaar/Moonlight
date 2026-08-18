package net.mehvahdjukaar.moonlight.api.resources.assets;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.mehvahdjukaar.moonlight.api.events.AfterLanguageLoadEvent;
import net.mehvahdjukaar.moonlight.api.set.BlockType;
import net.mehvahdjukaar.moonlight.api.util.TextHelper;
import net.mehvahdjukaar.moonlight.core.Moonlight;
import net.minecraft.Util;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

import java.util.LinkedHashMap;
import java.util.Map;

public class LangBuilder {

    private final Map<String, String> entries = new LinkedHashMap<>();

    public void addGenericEntry(String key, String translation) {
        entries.put(key, translation);
    }

    public void addEntry(Block block, String translation) {
        entries.put(block.getDescriptionId(), translation);
    }

    public <T> void addEntry(Registry<T> reg, T entry, String translation) {
        entries.put(Util.makeDescriptionId(reg.key().location().getPath(), reg.getKey(entry)), translation);
    }

    public <T> void addSimpleEntry(Registry<T> reg, T entry) {
        entries.put(Util.makeDescriptionId(reg.key().location().getPath(), reg.getKey(entry)),
                TextHelper.getReadableName(reg.getKey(entry).getPath()));
    }

    public void addEntry(Item item, String translation) {
        entries.put(item.getDescriptionId(), translation);
    }

    public void addEntry(EntityType<?> entityType, String translation) {
        entries.put(entityType.getDescriptionId(), translation);
    }

    public JsonElement build() {
        JsonObject json = new JsonObject();
        for (var e : entries.entrySet()) {
            json.addProperty(e.getKey(), e.getValue());
        }
        return json;
    }

    public Map<String, String> entries() {
        return entries;
    }


    //utils

    /**
     * @deprecated moved to TextHelper
     */
    @Deprecated(forRemoval = true)
    public static String getReadableName(String name) {
        return TextHelper.getReadableName(name);
    }

    /**
     * @deprecated moved to TextHelper
     */
    @Deprecated(forRemoval = true)
    public static Component getReadableComponent(String key, String... arguments) {
        return TextHelper.getReadableComponent(key, arguments);
    }


    public static void addDynamicEntry(AfterLanguageLoadEvent lang,
                                       String key, BlockType type, Item item) {
        String base = lang.getEntry(key);
        if (base != null) {
            String typeName = lang.getEntry(type.getTranslationKey());
            if (typeName != null) {
                lang.addEntry(item.getDescriptionId(), String.format(base, typeName));
            } else Moonlight.LOGGER.error("Could not find translation line {}", type.getTranslationKey());
        } else Moonlight.LOGGER.error("Could not find translation line {}", key);
    }

    public static void addDynamicEntry(AfterLanguageLoadEvent lang,
                                       String key, BlockType type, Block block) {
        String base = lang.getEntry(key);
        if (base != null) {
            String typeName = lang.getEntry(type.getTranslationKey());
            if (typeName != null) {
                lang.addEntry(block.getDescriptionId(), String.format(base, typeName));
            }
        }
    }

    public static void addDynamicEntry(AfterLanguageLoadEvent lang,
                                       String key, BlockType type, EntityType<?> entityType) {
        String base = lang.getEntry(key);
        if (base != null) {
            String typeName = lang.getEntry(type.getTranslationKey());
            if (typeName != null) {
                lang.addEntry(entityType.getDescriptionId(), String.format(base, typeName));
            }
        }
    }
}

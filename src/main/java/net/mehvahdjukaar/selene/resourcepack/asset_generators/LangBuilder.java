package net.mehvahdjukaar.selene.resourcepack.asset_generators;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class LangBuilder {
    private final Map<String, String> entries = new LinkedHashMap<>();

    //helper to make lang strings
    public static String getReadableName(String name) {
        return Arrays.stream((name).replace(":","_").split("_"))
                .map(StringUtils::capitalize).collect(Collectors.joining(" "));
    }

    public void addGenericEntry(String key, String translation) {
        entries.put(key, translation);
    }

    public void addEntry(Block block, String translation) {
        entries.put(block.getDescriptionId(), translation);
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

}

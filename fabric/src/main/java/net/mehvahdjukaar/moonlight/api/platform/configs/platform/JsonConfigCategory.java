package net.mehvahdjukaar.moonlight.api.platform.configs.platform;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.mehvahdjukaar.moonlight.core.Moonlight;

import java.util.ArrayList;
import java.util.List;

public class JsonConfigCategory extends JsonConfigEntry {

    private final List<JsonConfigEntry> entries = new ArrayList<>();

    public JsonConfigCategory(String name) {
        super(name);
    }

    public void addEntry(JsonConfigEntry entry){
        this.entries.add(entry);
    }

    public List<JsonConfigEntry> getEntries() {
        return entries;
    }

    @Override
    public boolean loadFromJson(JsonObject object) {
        if (object.has(this.name)) {
            JsonElement o = object.get(this.name);
            if (o instanceof JsonObject jo) {
                boolean changed = false;
                for (JsonConfigEntry entry : entries) {
                    changed |= entry.loadFromJson(jo);
                }
                return changed;
            }
            return false;
        }
        Moonlight.LOGGER.warn("Config file had missing category {}", this.name);
        return false;
    }

    @Override
    public void saveToJson(JsonObject object) {
        JsonObject category = new JsonObject();
        entries.forEach(l -> l.saveToJson(category));
        object.add(this.name, category);
    }


}

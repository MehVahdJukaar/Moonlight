package net.mehvahdjukaar.moonlight.api.platform.configs.platform;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.mehvahdjukaar.moonlight.core.Moonlight;

import java.util.ArrayList;
import java.util.List;

public class ConfigSubCategory extends ConfigEntry {

    private final List<ConfigEntry> entries = new ArrayList<>();

    public ConfigSubCategory(String name) {
        super(name);
    }

    public void addEntry(ConfigEntry entry){
        this.entries.add(entry);
    }

    public List<ConfigEntry> getEntries() {
        return entries;
    }

    @Override
    public boolean loadFromJson(JsonObject object) {
        if (object.has(this.name)) {
            JsonElement o = object.get(this.name);
            if (o instanceof JsonObject jo) {
                boolean changed = false;
                for (ConfigEntry entry : entries) {
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

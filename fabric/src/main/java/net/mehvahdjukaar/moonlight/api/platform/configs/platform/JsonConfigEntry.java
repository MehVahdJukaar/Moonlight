package net.mehvahdjukaar.moonlight.api.platform.configs.platform;

import com.google.gson.JsonObject;

public abstract class JsonConfigEntry {

    protected final String name;

    protected JsonConfigEntry(String name) {
        this.name = name;
    }

    public abstract boolean loadFromJson(JsonObject object);

    public abstract void saveToJson(JsonObject object);

    public String getName() {
        return name;
    }
}

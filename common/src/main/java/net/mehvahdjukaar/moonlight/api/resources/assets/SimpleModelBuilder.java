package net.mehvahdjukaar.moonlight.api.resources.assets;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.resources.Identifier;

import java.util.HashMap;
import java.util.Map;

public class SimpleModelBuilder {
    private final Identifier parent;
    private final Map<String, Identifier> textures = new HashMap<>();

    public SimpleModelBuilder(Identifier parent) {
        this.parent = parent;
    }

    public SimpleModelBuilder texture(String name, Identifier texture) {
        this.textures.put(name, texture);
        return this;
    }

    public JsonElement build() {
        JsonObject json = new JsonObject();
        json.addProperty("parent", this.parent.toString());
        JsonObject text = new JsonObject();

        textures.forEach((key, value) -> text.addProperty(key, value.toString()));
        json.add("textures", text);

        return json;
    }
}

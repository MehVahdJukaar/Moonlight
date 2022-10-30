package net.mehvahdjukaar.moonlight.api.client.model;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

public interface CustomModelLoader {

    CustomGeometry deserialize(JsonObject json, JsonDeserializationContext context) throws JsonParseException;

}

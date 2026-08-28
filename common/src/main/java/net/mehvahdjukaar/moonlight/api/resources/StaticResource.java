package net.mehvahdjukaar.moonlight.api.resources;

import com.google.gson.JsonObject;
import net.mehvahdjukaar.moonlight.core.Moonlight;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import org.jetbrains.annotations.Nullable;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.NoSuchElementException;

/**
 * Represents a generic resource that can be read multiple times. Consumes the original resource
 */
public class StaticResource {
    public final byte[] data;
    public final Identifier location;
    public final String sourceName;

    private String dataAsString = null;

    private StaticResource(byte[] data, Identifier location, String sourceName) {
        this.data = data;
        this.location = location;
        this.sourceName = sourceName;
    }

    /**
     * Converts and consume a resource to be used multiple time
     */
    public static StaticResource of(Resource original, Identifier location) {
        byte[] data1 = new byte[]{};
        try (var stream = original.open()) {
            try {
                data1 = stream.readAllBytes();
            } catch (IOException e) {
                Moonlight.LOGGER.error("Could not parse resource: {}", location);
            }
        } catch (Exception ignored) {}

        return new StaticResource(data1, location, original.sourcePackId());
    }

    /**
     * Just used as a record
     */
    public static StaticResource create(byte[] data, Identifier location) {
        return new StaticResource(data, location, location.toString());
    }

    @Nullable
    public static StaticResource getOrLog(ResourceManager manager, Identifier location) {
        try {
            return of(manager.getResource(location).get(),location);
        } catch (Exception var4) {
            Moonlight.LOGGER.error("Could not find resource {}", location);
            return null;
        }
    }

    public static StaticResource getOrThrow(ResourceManager manager, Identifier location) throws NoSuchElementException {
        return of(manager.getResource(location).orElseThrow(), location);
    }

    public JsonObject toJson(){
        try(var s = new ByteArrayInputStream(data)){
            return RPUtils.deserializeJson(s);
        }
        catch (Exception e){
            throw  new RuntimeException(e);
        }
    }

    public String asString() {
        if (this.dataAsString == null) {
            this.dataAsString = new String(this.data, StandardCharsets.UTF_8);
        }
        return this.dataAsString;
    }
}
package net.mehvahdjukaar.selene.resourcepack;

import net.mehvahdjukaar.selene.Selene;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import org.jetbrains.annotations.Nullable;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Represents a generic resource that can be read multiple times. Consumes the original resource
 */
public class StaticResource {
    public final byte[] data;
    public final ResourceLocation location;
    public final String sourceName;

    private StaticResource(byte[] data, ResourceLocation location, String sourceName){
        this.data = data;
        this.location = location;
        this.sourceName = sourceName;
    }

    public InputStream getInputStream(){
        return new ByteArrayInputStream(data);
    }

    /**
     * Converts and consume a resource to be used multiple time
     */
    public static StaticResource of(Resource original) {
        byte[] data1;
        try {
            data1 = original.getInputStream().readAllBytes();
        } catch (IOException e) {
            data1 = new byte[]{};
            Selene.LOGGER.error("Could not parse resource: {}", original.getLocation());
        }
        finally {
            try {
                original.close();
            } catch (IOException e) {
            }
        }

        return new StaticResource(data1,original.getLocation(), original.getSourceName());
    }

    /**
     * Just used as a record
     */
    public static StaticResource create(byte data[], ResourceLocation location){
        return new StaticResource(data, location, location.toString());
    }

    @Nullable
    public static StaticResource getOrLog(ResourceManager manager, ResourceLocation location) {
        try {
            return of(manager.getResource(location));
        } catch (Exception var4) {
            Selene.LOGGER.error("Could not find resource {}", location);
            return null;
        }
    }

    public static StaticResource getOrFail(ResourceManager manager, ResourceLocation location) throws IOException {
        return of(manager.getResource(location));
    }
}
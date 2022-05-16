package net.mehvahdjukaar.selene.resourcepack;

import net.mehvahdjukaar.selene.Selene;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;

/**
 * Represents a generic resource that can be read multiple times. Consumes the original resource
 */
public class StaticResource {
    public final byte[] data;
    public final ResourceLocation location;
    public final String sourceName;

    public StaticResource(Resource original) {
        byte[] data1;
        try {
            data1 = original.getInputStream().readAllBytes();
        } catch (IOException e) {
            data1 = new byte[]{};
            Selene.LOGGER.error("Could not parse resource: {}", original.getLocation());
        }

        this.data = data1;
        this.location = original.getLocation();
        this.sourceName = original.getSourceName();
    }

    @Nullable
    public static StaticResource getOrLog(ResourceManager manager, ResourceLocation location) {
        try {
            return new StaticResource(manager.getResource(location));
        } catch (Exception var4) {
            Selene.LOGGER.error("Could not find resource {} while generating dynamic resource pack", location);
            return null;
        }
    }

    public static StaticResource getOrFail(ResourceManager manager, ResourceLocation location) throws IOException {
        return new StaticResource(manager.getResource(location));
    }
}
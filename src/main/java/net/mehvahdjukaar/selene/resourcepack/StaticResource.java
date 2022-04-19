package net.mehvahdjukaar.selene.resourcepack;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;

import java.io.IOException;

/**
 * Represents a generic resource that can be read multiple times. Consumes the original resource
 */
public class StaticResource{
    public final byte[] data;
    public final ResourceLocation location;
    public final String sourceName;

    public StaticResource(Resource original) {
        byte[] data1;
        try {
            data1 = original.getInputStream().readAllBytes();
        } catch (IOException e) {
            data1 = new byte[]{};
        }

        this.data = data1;
        this.location = original.getLocation();
        this.sourceName = original.getSourceName();
    }
}
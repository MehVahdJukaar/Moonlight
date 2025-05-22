package net.mehvahdjukaar.moonlight.api.resources.pack;

import net.minecraft.server.packs.PackLocationInfo;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PathPackResources;

import java.nio.file.Path;

public class DynamicPackCache extends PathPackResources {
//TODO: finish

    public DynamicPackCache(PackLocationInfo location, Path root) {
        super(location, root);
    }
}

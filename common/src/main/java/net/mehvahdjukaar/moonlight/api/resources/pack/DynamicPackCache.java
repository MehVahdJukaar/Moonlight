package net.mehvahdjukaar.moonlight.api.resources.pack;

import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PathPackResources;

import java.nio.file.Path;

public class DynamicPackCache extends PathPackResources {

    public DynamicPackCache(String name, Path root, boolean isBuiltin) {
        super(name, root, isBuiltin);
    }
}

package net.mehvahdjukaar.moonlight.api.resources.pack;

import net.minecraft.server.packs.PackLocationInfo;
import net.minecraft.server.packs.PackType;

public class GlobalCachedZipStrategy extends GlobalCachedStrategy {

    @Override
    public IEditablePackResources createPackResources(PackLocationInfo info, PackType type) {
        return new CacheZipPackResources(info, type,
                getPath(type).resolve(info.id().replace(":", "-")));

    }

    @Override
    public String toString() {
        return "CACHED_ZIPPED";
    }
}

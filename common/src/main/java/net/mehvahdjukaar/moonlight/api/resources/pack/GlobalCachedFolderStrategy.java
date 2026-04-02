package net.mehvahdjukaar.moonlight.api.resources.pack;

import net.minecraft.server.packs.PackLocationInfo;
import net.minecraft.server.packs.PackType;

public class GlobalCachedFolderStrategy extends GlobalCachedStrategy {

    @Override
    public IEditablePackResources createPackResources(PackLocationInfo info, PackType type) {
        //this editable pack resources will save sutf to file whenver its added to it
        return new CachePathPackResources(info, type, getPath(type)
                .resolve(info.id().replace(":", "-")));
    }

    @Override
    public String toString() {
        return "CACHED_FOLDER";
    }
}

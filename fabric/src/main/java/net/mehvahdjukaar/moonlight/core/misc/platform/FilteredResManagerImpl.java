package net.mehvahdjukaar.moonlight.core.misc.platform;

import net.fabricmc.fabric.api.resource.v1.pack.ModPackResources;
import net.minecraft.server.packs.PackResources;

public class FilteredResManagerImpl {
    public static boolean isModResourcePack(PackResources pack) {
        String id = pack.packId();
        if (id.startsWith("generated")) return true;
        return pack instanceof ModPackResources || pack.packId().equals("fabric");
    }
}

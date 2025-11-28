package net.mehvahdjukaar.moonlight.core.misc.fabric;

import net.fabricmc.fabric.impl.resource.loader.ModNioResourcePack;
import net.minecraft.server.packs.PackResources;

public class FilteredResManagerImpl {
    public static boolean isModResourcePack(PackResources pack) {
        String id = pack.packId();
        if (id.startsWith("generated")) return true;
        return pack instanceof ModNioResourcePack || pack.packId().equals("fabric");
    }
}

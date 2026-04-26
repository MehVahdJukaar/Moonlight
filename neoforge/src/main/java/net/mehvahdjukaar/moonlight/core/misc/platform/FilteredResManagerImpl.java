package net.mehvahdjukaar.moonlight.core.misc.neoforge;

import net.minecraft.server.packs.PackResources;

public class FilteredResManagerImpl {
    public static boolean isModResourcePack(PackResources pack) {
        String packId = pack.packId();
        return packId.equals("mod_resources") || packId.startsWith("mod/") || packId.startsWith("generated");
    }
}

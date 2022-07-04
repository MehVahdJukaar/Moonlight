package net.mehvahdjukaar.moonlight.resources.pack.forge;

import net.mehvahdjukaar.moonlight.resources.pack.IEarlyPackReloadEvent;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.resources.CloseableResourceManager;
import net.minecraftforge.common.MinecraftForge;

import java.util.List;

public class IEarlyPackReloadEventImpl {

    public static IEarlyPackReloadEvent create(List<PackResources> packs, CloseableResourceManager manager) {
        return new EarlyPackReloadEvent(packs, manager);
    }
}

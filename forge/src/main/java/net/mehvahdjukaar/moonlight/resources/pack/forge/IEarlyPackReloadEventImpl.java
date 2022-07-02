package net.mehvahdjukaar.moonlight.resources.pack.forge;

import net.mehvahdjukaar.moonlight.resources.pack.IEarlyPackReloadEvent;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.resources.CloseableResourceManager;
import net.minecraftforge.common.MinecraftForge;

import java.util.List;

public interface IEarlyPackReloadEventImpl {

    public static void postEvent(List<PackResources> packs, CloseableResourceManager manager) {
        MinecraftForge.EVENT_BUS.post(new EarlyPackReloadEvent(packs, manager));
    }
}

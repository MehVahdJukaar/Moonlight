package net.mehvahdjukaar.moonlight.api.events.forge;

import net.mehvahdjukaar.moonlight.api.events.IEarlyPackReloadEvent;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.resources.CloseableResourceManager;

import java.util.List;

public class IEarlyPackReloadEventImpl {

    public static IEarlyPackReloadEvent create(List<PackResources> packs, CloseableResourceManager manager) {
        return new EarlyPackReloadEvent(packs, manager);
    }
}

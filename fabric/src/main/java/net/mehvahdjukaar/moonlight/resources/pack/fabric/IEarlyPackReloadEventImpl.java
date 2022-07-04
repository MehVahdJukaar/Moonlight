package net.mehvahdjukaar.moonlight.resources.pack.fabric;

import net.mehvahdjukaar.moonlight.resources.pack.IEarlyPackReloadEvent;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.resources.CloseableResourceManager;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class IEarlyPackReloadEventImpl {

    static IEarlyPackReloadEvent create(List<PackResources> packs, CloseableResourceManager manager) {
        return new EarlyPackReloadEvent(packs, manager);
    }
}

package net.mehvahdjukaar.moonlight.resources.pack.fabric;

import net.mehvahdjukaar.moonlight.resources.pack.IEarlyPackReloadEvent;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.resources.ResourceManager;

import java.util.List;

public class EarlyPackReloadEvent implements IEarlyPackReloadEvent {

    private final ResourceManager manager;
    private final List<PackResources> packs;

    public EarlyPackReloadEvent(List<PackResources> packs, ResourceManager manager) {
        this.packs = packs;
        this.manager = manager;
    }

    public ResourceManager getManager() {
        return manager;
    }

    public List<PackResources> getPacks() {
        return packs;
    }
}

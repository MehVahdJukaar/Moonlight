package net.mehvahdjukaar.moonlight.api.resources.pack.forge;

import net.mehvahdjukaar.moonlight.api.resources.pack.IEarlyPackReloadEvent;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraftforge.eventbus.api.Event;

import java.util.List;

public class EarlyPackReloadEvent extends Event implements IEarlyPackReloadEvent {

    private final ResourceManager manager;
    private final List<PackResources> packs;

    public EarlyPackReloadEvent(List<PackResources> packs, ResourceManager manager){
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

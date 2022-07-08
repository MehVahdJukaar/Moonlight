package net.mehvahdjukaar.moonlight.api.events;

import dev.architectury.injectables.annotations.ExpectPlatform;
import net.mehvahdjukaar.moonlight.api.platform.event.SimpleEvent;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.resources.CloseableResourceManager;
import net.minecraft.server.packs.resources.ResourceManager;

import java.util.List;

public interface IEarlyPackReloadEvent extends SimpleEvent {

    ResourceManager getManager();

    List<PackResources> getPacks();

    @ExpectPlatform
    static IEarlyPackReloadEvent create(List<PackResources> packs, CloseableResourceManager manager){
        throw new AssertionError();
    }
}

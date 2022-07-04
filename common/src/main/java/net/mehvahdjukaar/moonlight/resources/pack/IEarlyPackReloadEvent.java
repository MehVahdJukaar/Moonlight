package net.mehvahdjukaar.moonlight.resources.pack;

import dev.architectury.injectables.annotations.ExpectPlatform;
import net.mehvahdjukaar.moonlight.platform.event.SimpleEvent;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.repository.Pack;
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

package net.mehvahdjukaar.moonlight.resources.pack;

import dev.architectury.injectables.annotations.ExpectPlatform;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.resources.CloseableResourceManager;
import net.minecraft.server.packs.resources.ResourceManager;

import java.util.List;

public interface IEarlyPackReloadEvent {

    @ExpectPlatform
    public static void postEvent(List<PackResources> packs, CloseableResourceManager manager) {
        throw new AssertionError();
    }

    public ResourceManager getManager();

    public List<PackResources> getPacks();
}

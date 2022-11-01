package net.mehvahdjukaar.moonlight.api.resources.pack;

import net.mehvahdjukaar.moonlight.api.events.EarlyPackReloadEvent;
import net.mehvahdjukaar.moonlight.api.platform.PlatformHelper;
import net.mehvahdjukaar.moonlight.api.events.MoonlightEventsHelper;
import net.mehvahdjukaar.moonlight.core.Moonlight;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.PackRepository;
import org.jetbrains.annotations.ApiStatus;

/**
 * Class responsible to generate assets and manage your dynamic data pack (server)
 * Handles and registers your dynamic pack instance which needs to be provides
 */
public abstract class DynServerResourcesProvider extends DynResourceProvider<DynamicDataPack> {

    protected DynServerResourcesProvider(DynamicDataPack pack) {
        super(pack);
    }

    @Override
    protected PackRepository getRepository() {
        var s = PlatformHelper.getCurrentServer();
        if (s != null) return s.getPackRepository();
        return null;
    }
}

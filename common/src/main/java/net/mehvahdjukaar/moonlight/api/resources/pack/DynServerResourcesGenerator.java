package net.mehvahdjukaar.moonlight.api.resources.pack;

import net.mehvahdjukaar.moonlight.api.platform.PlatformHelper;
import net.minecraft.server.packs.repository.PackRepository;

/**
 * Class responsible to generate assets and manage your dynamic data pack (server)
 * Handles and registers your dynamic pack instance which needs to be provides
 */
public abstract class DynServerResourcesGenerator extends DynResourceGenerator<DynamicDataPack> {

    protected DynServerResourcesGenerator(DynamicDataPack pack) {
        super(pack);
    }

    @Override
    protected PackRepository getRepository() {
        var s = PlatformHelper.getCurrentServer();
        if (s != null) return s.getPackRepository();
        return null;
    }
}

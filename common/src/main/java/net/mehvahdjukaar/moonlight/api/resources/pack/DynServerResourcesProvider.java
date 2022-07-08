package net.mehvahdjukaar.moonlight.api.resources.pack;

import net.mehvahdjukaar.moonlight.api.events.IEarlyPackReloadEvent;
import net.mehvahdjukaar.moonlight.api.platform.PlatformHelper;
import net.mehvahdjukaar.moonlight.api.platform.event.EventHelper;
import net.minecraft.server.packs.repository.PackRepository;

/**
 * Class responsible to generate assets and manage your dynamic data pack (server)
 * Handles and registers your dynamic pack instance which needs to be provides
 */
public abstract class DynServerResourcesProvider extends DynResourceProvider<DynamicDataPack> {

    protected DynServerResourcesProvider(DynamicDataPack pack) {
        super(pack);
    }

    /**
     * Remember to call this during mod init
     */
    @Override
    public void register() {
        super.register();
        //MinecraftForge.EVENT_BUS.addListener(this::onAddReloadListeners);
        EventHelper.addListener(this::onEarlyReload, IEarlyPackReloadEvent.class);
    }

    @Override
    protected PackRepository getRepository() {
        var s = PlatformHelper.getCurrentServer();
        if (s != null) return s.getPackRepository();
        return null;
    }

    public void onEarlyReload(final IEarlyPackReloadEvent event) {
        this.reloadResources(event.getManager());
    }

    //public void onAddReloadListeners(final AddReloadListenerEvent event) {
    //event.addListener(this);
    // }
}

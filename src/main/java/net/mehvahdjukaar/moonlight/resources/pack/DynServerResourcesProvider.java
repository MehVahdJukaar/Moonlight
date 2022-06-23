package net.mehvahdjukaar.moonlight.resources.pack;

import net.minecraft.server.packs.repository.PackRepository;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.server.ServerLifecycleHooks;

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
    public void register(IEventBus bus) {
        super.register(bus);
        //MinecraftForge.EVENT_BUS.addListener(this::onAddReloadListeners);
        MinecraftForge.EVENT_BUS.addListener(this::onEarlyReload);
    }

    @Override
    protected PackRepository getRepository() {
        var s = ServerLifecycleHooks.getCurrentServer();
        if (s != null) return s.getPackRepository();
        return null;
    }

    public void onEarlyReload(final EarlyPackReloadEvent event) {
        this.reloadResources(event.getManager());
    }

    public void onAddReloadListeners(final AddReloadListenerEvent event) {
        //event.addListener(this);

    }
}

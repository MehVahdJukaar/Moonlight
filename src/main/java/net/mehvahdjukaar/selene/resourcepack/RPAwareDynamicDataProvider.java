package net.mehvahdjukaar.selene.resourcepack;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/**
 * Class responsible to generate assets and manage your dynamic data pack (server)
 * Handles and registers your dynamic pack instance which needs to be provides
 */
public abstract class RPAwareDynamicDataProvider extends RPAwareDynamicResourceProvider {

    protected RPAwareDynamicDataProvider(DynamicDataPack pack) {
        super(pack);
    }

    /**
     * Remember to call this during mod init
     */
    @Override
    public void register(IEventBus bus) {
        super.register(bus);
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    private void onAddReloadListeners(final AddReloadListenerEvent event) {
        event.addListener(this);
    }
}

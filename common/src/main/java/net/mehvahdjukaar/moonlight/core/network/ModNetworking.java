package net.mehvahdjukaar.moonlight.core.network;

import dev.architectury.injectables.annotations.ExpectPlatform;
import net.mehvahdjukaar.moonlight.api.platform.network.NetworkHelper;


public class ModNetworking {

    public static void init() {
        NetworkHelper.addNetworkRegistration(ModNetworking::registerMessages, 9);
    }

    private static void registerMessages(NetworkHelper.RegisterMessagesEvent event) {
        event.registerClientBound(ClientBoundFinalizeFluidsMessage.TYPE);
        event.registerClientBound(ClientBoundOpenScreenPacket.TYPE);
        event.registerClientBound(ClientBoundSendLoginPacket.TYPE);
        event.registerClientBound(ClientBoundOnPistonMovedBlockPacket.TYPE);
        event.registerClientBound(ClientBoundParticleAroundBlockPacket.TYPE);
        event.registerClientBound(ClientBoundSyncWorldDataMessage.TYPE);
        event.registerServerBound(ServerBoundItemLeftClickPacket.TYPE);
        event.registerBidirectional(SyncConfigsMessage.TYPE);

        ModNetworking.loaderDependent(event);
    }

    @ExpectPlatform
    public static void loaderDependent(NetworkHelper.RegisterMessagesEvent event) {
        throw new AssertionError();
    }

}
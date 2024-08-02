package net.mehvahdjukaar.moonlight.core.network;

import dev.architectury.injectables.annotations.ExpectPlatform;
import net.mehvahdjukaar.moonlight.api.platform.network.NetworkHelper;


public class ModNetworking {

    public static void init() {
        NetworkHelper.addNetworkRegistration(ModNetworking::registerMessages, 5);
    }

    private static void registerMessages(NetworkHelper.RegisterMessagesEvent event) {
        event.registerClientBound(ClientBoundFinalizeFluidsMessage.TYPE);
        event.registerClientBound(ClientBoundSyncConfigsMessage.TYPE);
        event.registerClientBound(ClientBoundOpenScreenPacket.TYPE);
        event.registerClientBound(ClientBoundSendLoginPacket.TYPE);
        event.registerClientBound(ClientBoundOnPistonMovedBlockPacket.TYPE);
        event.registerServerBound(ServerBoundItemLeftClickPacket.TYPE);

        ModNetworking.loaderDependent(event);
    }

    @ExpectPlatform
    public static void loaderDependent(NetworkHelper.RegisterMessagesEvent event) {
        throw new AssertionError();
    }

}
package net.mehvahdjukaar.moonlight.core.network.platform;

import net.mehvahdjukaar.moonlight.api.platform.network.NetworkHelper;

public class ModNetworkingImpl {

    public static void loaderDependent(NetworkHelper.RegisterMessagesEvent event) {
        event.registerClientBound(ClientBoundSpawnCustomEntityMessage.TYPE);
        event.registerClientBound(ClientBoundOpenCustomMenuMessage.TYPE);
        event.registerClientBound(ClientBoundSyncDataMapsPacket.TYPE);
    }

}

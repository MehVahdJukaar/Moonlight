package net.mehvahdjukaar.moonlight.core.network.fabric;

import net.mehvahdjukaar.moonlight.api.platform.network.NetworkHelper;
import net.mehvahdjukaar.moonlight.api.platform.network.NetworkDir;

public class ModMessagesImpl {

    public static void loaderDependant(NetworkHelper.RegEvent builder) {
        builder.register(NetworkDir.CLIENTBOUND, //id = "moonlight:0"
                ClientBoundSpawnCustomEntityMessage.class, ClientBoundSpawnCustomEntityMessage::new);

        builder.register(NetworkDir.CLIENTBOUND,
                ClientBoundOpenScreenMessage.class, ClientBoundOpenScreenMessage::new);
    }
}

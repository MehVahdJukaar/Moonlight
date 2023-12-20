package net.mehvahdjukaar.moonlight.core.network.fabric;

import net.mehvahdjukaar.moonlight.api.platform.network.ChannelHandler;
import net.mehvahdjukaar.moonlight.api.platform.network.NetworkDir;

public class ModMessagesImpl {
    public static void loaderDependant(ChannelHandler.Builder builder) {
        builder.register(NetworkDir.PLAY_TO_CLIENT, //id = "moonlight:0"
                ClientBoundSpawnCustomEntityMessage.class, ClientBoundSpawnCustomEntityMessage::new);

        builder.register(NetworkDir.PLAY_TO_CLIENT,
                ClientBoundOpenScreenMessage.class, ClientBoundOpenScreenMessage::new);
    }
}

package net.mehvahdjukaar.moonlight.core.network;

import net.mehvahdjukaar.moonlight.api.platform.network.NetworkDir;
import net.mehvahdjukaar.moonlight.core.Moonlight;
import net.mehvahdjukaar.moonlight.api.platform.network.ChannelHandler;
import net.minecraft.resources.ResourceLocation;


public class ModMessages {

    public static final ResourceLocation SPAWN_PACKET_ID = Moonlight.res("0");
    public static final ChannelHandler CHANNEL = ChannelHandler.createChannel(Moonlight.res("channel"));

    public static void registerMessages() {

        CHANNEL.register(NetworkDir.PLAY_TO_CLIENT, //id = "moonlight:0"
                ClientBoundSpawnCustomEntityMessage.class, ClientBoundSpawnCustomEntityMessage::new);

        CHANNEL.register(NetworkDir.PLAY_TO_CLIENT,
                ClientBoundFinalizeFluidsMessage.class, ClientBoundFinalizeFluidsMessage::new);

        CHANNEL.register(NetworkDir.PLAY_TO_CLIENT,
                ClientBoundSyncConfigsMessage.class, ClientBoundSyncConfigsMessage::new);

        CHANNEL.register(NetworkDir.PLAY_TO_CLIENT,
                ClientBoundOpenScreenPacket.class, ClientBoundOpenScreenPacket::new);

        CHANNEL.register(NetworkDir.PLAY_TO_CLIENT,
                ClientBoundSendLoginPacket.class, ClientBoundSendLoginPacket::new);
    }

}
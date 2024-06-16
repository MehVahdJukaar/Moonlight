package net.mehvahdjukaar.moonlight.core.network;

import net.mehvahdjukaar.moonlight.api.platform.network.ChannelHandler;
import net.mehvahdjukaar.moonlight.api.platform.network.NetworkDir;
import net.mehvahdjukaar.moonlight.core.Moonlight;
import net.minecraft.resources.ResourceLocation;


public class ModMessages {

    public static final ResourceLocation SPAWN_PACKET_ID = Moonlight.res("0");
    public static final ChannelHandler CHANNEL = ChannelHandler.builder(Moonlight.MOD_ID)
            .version(5)
            .register(NetworkDir.PLAY_TO_CLIENT, //id = "moonlight:0"
                    ClientBoundSpawnCustomEntityMessage.class, ClientBoundSpawnCustomEntityMessage::new)

            .register(NetworkDir.PLAY_TO_CLIENT,
                    ClientBoundFinalizeFluidsMessage.class, ClientBoundFinalizeFluidsMessage::new)

            .register(NetworkDir.PLAY_TO_CLIENT,
                    ClientBoundSyncConfigsMessage.class, ClientBoundSyncConfigsMessage::new)

            .register(NetworkDir.PLAY_TO_CLIENT,
                    ClientBoundOpenScreenPacket.class, ClientBoundOpenScreenPacket::new)

            .register(NetworkDir.PLAY_TO_CLIENT,
                    ClientBoundSendLoginPacket.class, ClientBoundSendLoginPacket::new)

            .register(NetworkDir.PLAY_TO_CLIENT,
                    ClientBoundOnPistonMovedBlockPacket.class, ClientBoundOnPistonMovedBlockPacket::new)

            .register(NetworkDir.PLAY_TO_SERVER,
                    ServerBoundItemLeftClickPacket.class, ServerBoundItemLeftClickPacket::new)


            .build();

    public static void init() {
    }

}
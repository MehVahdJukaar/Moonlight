package net.mehvahdjukaar.moonlight.network;

import net.mehvahdjukaar.moonlight.Moonlight;
import net.mehvahdjukaar.moonlight.platform.network.ChannelHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;


public class ModMessages {

    public static final ResourceLocation SPAWN_PACKET_ID = Moonlight.res("0");
    public static ChannelHandler CHANNEL;

    public static void registerMessages() {
        CHANNEL = ChannelHandler.createChannel(Moonlight.res("channel"));

        CHANNEL.register(ChannelHandler.NetworkDir.PLAY_TO_CLIENT, //id = "moonlight:0"
                ClientBoundSpawnCustomEntityPacket.class, ClientBoundSpawnCustomEntityPacket::new);

        CHANNEL.register(ChannelHandler.NetworkDir.PLAY_TO_CLIENT,
                ClientBoundSyncCustomMapDecorationMessage.class, ClientBoundSyncCustomMapDecorationMessage::new);

        /*
        INSTANCE.registerMessage(nextID(), ClientBoundSyncFluidsPacket.class, ClientBoundSyncFluidsPacket::buffer,
                ClientBoundSyncFluidsPacket::new, ClientBoundSyncFluidsPacket::handler);

        INSTANCE.registerMessage(nextID(), ClientBoundSyncMapDecorationTypesPacket.class, ClientBoundSyncMapDecorationTypesPacket::buffer,
                ClientBoundSyncMapDecorationTypesPacket::new, ClientBoundSyncMapDecorationTypesPacket::handler);

        INSTANCE.registerMessage(nextID(), SyncModConfigsPacket.class, SyncModConfigsPacket::buffer,
                SyncModConfigsPacket::new, SyncModConfigsPacket::handler);

         */
    }
}
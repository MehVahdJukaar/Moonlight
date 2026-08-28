package net.mehvahdjukaar.moonlight.api.platform.network.platform;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.mehvahdjukaar.moonlight.api.platform.network.Message;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;


public class NetworkHelperImplClient {

    public static <M extends Message> void register(CustomPacketPayload.TypeAndCodec<RegistryFriendlyByteBuf, M> messageType) {

        ClientPlayNetworking.registerGlobalReceiver(messageType.type(),
                (message, context) -> message.handle(new ContextWrapper(context)));
    }

    public static boolean serverHasChannel(CustomPacketPayload.Type<?> type) {
        // see PresenceMarker
        return ClientPlayNetworking.canSend(PresenceMarker.idOf(type));
    }

    public record ContextWrapper(ClientPlayNetworking.Context c) implements Message.Context {

        @Override
        public Message.NetworkDir getDirection() {
            return Message.NetworkDir.CLIENT_BOUND;
        }

        @Override
        public Player getPlayer() {
            return c.player();
        }

        @Override
        public void disconnect(Component reason) {
            c.responseSender().disconnect(reason);
        }

        @Override
        public void reply(CustomPacketPayload message) {
            c.responseSender().sendPacket(message);
        }
    }

}

package net.mehvahdjukaar.moonlight.api.platform.network.fabric;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.mehvahdjukaar.moonlight.api.platform.network.Message;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;


public class NetworkHelperImplClient {

    public static <M extends Message> void register(CustomPacketPayload.TypeAndCodec<RegistryFriendlyByteBuf, M> messageType) {
        PayloadTypeRegistry.playS2C().register(messageType.type(), messageType.codec());

        ClientPlayNetworking.registerGlobalReceiver(messageType.type(),
                (message, context) -> {
                    context.client().execute(() -> {
                        message.handle(new ContextWrapper(context));
                    });
                });
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

package net.mehvahdjukaar.moonlight.api.platform.network.fabric;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.mehvahdjukaar.moonlight.api.platform.network.Context;
import net.mehvahdjukaar.moonlight.api.platform.network.Message;
import net.mehvahdjukaar.moonlight.api.platform.network.NetworkDir;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

import java.util.function.Function;

public class NetworkHelperImplClient {

    public static <M extends Message> void register(CustomPacketPayload.TypeAndCodec<FriendlyByteBuf, M> messageType) {
        ClientPlayNetworking.registerGlobalReceiver(messageType.type(),
                (message, context) -> {
                    context.client().execute(() -> {
                        message.handle(new ContextWrapper(context));
                    });
                });
    }

    public record ContextWrapper(ClientPlayNetworking.Context c) implements Context {

        @Override
        public NetworkDir getDirection() {
            return NetworkDir.CLIENT_BOUND;
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

package net.mehvahdjukaar.moonlight.api.platform.network.fabric;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.mehvahdjukaar.moonlight.api.platform.PlatHelper;
import net.mehvahdjukaar.moonlight.api.platform.network.Message;
import net.mehvahdjukaar.moonlight.api.platform.network.NetworkHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

public class NetworkHelperImpl {

    public static void addNetworkRegistration(Consumer<NetworkHelper.RegisterMessagesEvent> eventListener, int version) {
        eventListener.accept(new NetworkHelper.RegisterMessagesEvent() {

            @Override
            public <M extends Message> void registerServerBound(CustomPacketPayload.TypeAndCodec<RegistryFriendlyByteBuf, M> messageType) {
                PayloadTypeRegistry.playC2S().register(messageType.type(), messageType.codec());

                ServerPlayNetworking.registerGlobalReceiver(messageType.type(),
                        (message, context) -> {
                            context.server().execute(() -> {
                                message.handle(new ContextWrapper(context));
                            });
                        });
            }


            @Override
            public <M extends Message> void registerClientBound(CustomPacketPayload.TypeAndCodec<RegistryFriendlyByteBuf, M> messageType) {
                if (!PlatHelper.getPhysicalSide().isClient()) return;

                NetworkHelperImplClient.register(messageType);
            }

            @Override
            public <M extends Message> void registerBidirectional(CustomPacketPayload.TypeAndCodec<RegistryFriendlyByteBuf, M> messageType) {
                this.registerServerBound(messageType);
                this.registerClientBound(messageType);
            }
        });

    }

    public record ContextWrapper(ServerPlayNetworking.Context c) implements Message.Context {

        @Override
        public Message.NetworkDir getDirection() {
            return Message.NetworkDir.SERVER_BOUND;
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


    public static void sendToClientPlayer(ServerPlayer serverPlayer, CustomPacketPayload message) {
        ServerPlayNetworking.send(serverPlayer, message);
    }

    public void sendToAllClientPlayers(CustomPacketPayload message) {
        for (var p : PlatHelper.getCurrentServer().getPlayerList().getPlayers()) {
            sendToClientPlayer(p, message);
        }
    }

    public static void sendToAllClientPlayersInRange(ServerLevel level, BlockPos pos, double radius, CustomPacketPayload message) {
        MinecraftServer currentServer = PlatHelper.getCurrentServer();
        if (!level.isClientSide && currentServer != null) {
            PlayerList players = currentServer.getPlayerList();
            var dimension = level.dimension();

            players.broadcast(null, pos.getX(), pos.getY(), pos.getZ(),
                    radius, dimension, ServerPlayNetworking.createS2CPacket(message));
        } else throw makeAssertionError();

    }

    private static @NotNull AssertionError makeAssertionError() {
        return new AssertionError("Cant send message to clients from client side!");
    }

    public static void sentToAllClientPlayersTrackingEntity(Entity target, CustomPacketPayload message) {
        if (target.level() instanceof ServerLevel serverLevel) {
            serverLevel.getChunkSource().broadcast(target, ServerPlayNetworking.createS2CPacket(message));
        } else throw makeAssertionError();
    }

    public void sentToAllClientPlayersTrackingEntityAndSelf(Entity target, Message message) {
        if (target.level() instanceof ServerLevel serverLevel) {
            var p = ServerPlayNetworking.createS2CPacket(message);
            serverLevel.getChunkSource().broadcast(target, p);
            if (target instanceof ServerPlayer player) {
                sendToClientPlayer(player, message);
            }
        } else throw makeAssertionError();
    }

    public static void sendToServer(CustomPacketPayload message) {
        ClientPlayNetworking.send(message);
    }
}

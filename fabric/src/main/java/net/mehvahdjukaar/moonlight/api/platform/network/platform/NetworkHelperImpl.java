package net.mehvahdjukaar.moonlight.api.platform.network.platform;

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
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

public class NetworkHelperImpl {

    public static void addNetworkRegistration(Consumer<NetworkHelper.RegisterMessagesEvent> eventListener, int version) {
        eventListener.accept(new NetworkHelper.RegisterMessagesEvent() {

            @Override
            public <M extends Message> void registerServerBound(CustomPacketPayload.TypeAndCodec<RegistryFriendlyByteBuf, M> messageType) {
                PayloadTypeRegistry.serverboundPlay().register(messageType.type(), messageType.codec());

                ServerPlayNetworking.registerGlobalReceiver(messageType.type(),
                        (message, context) -> {
                            context.server().execute(() -> {
                                message.handle(new ContextWrapper(context));
                            });
                        });
            }


            @Override
            public <M extends Message> void registerClientBound(CustomPacketPayload.TypeAndCodec<RegistryFriendlyByteBuf, M> messageType) {
                PayloadTypeRegistry.clientboundPlay().register(messageType.type(), messageType.codec());

                if (!PlatHelper.getPhysicalSide().isClient()) return;

                NetworkHelperImplClient.register(messageType);
            }

            @Override
            public <M extends Message> void registerClientBoundOptional(CustomPacketPayload.TypeAndCodec<RegistryFriendlyByteBuf, M> messageType) {
                this.registerClientBound(messageType);

                NetworkHelper.markOptional(messageType.type());
                // only so clients can see the server has the channel, fabric never rejects a missing one
                PresenceMarker.register(messageType.type());
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


    public static boolean canSendToPlayer(ServerPlayer player, CustomPacketPayload.Type<?> type) {
        return ServerPlayNetworking.canSend(player, type);
    }

    public static boolean serverHasChannel(CustomPacketPayload.Type<?> type) {
        if (!PlatHelper.getPhysicalSide().isClient()) return false;
        return NetworkHelperImplClient.serverHasChannel(type);
    }

    public static void sendToClientPlayer(ServerPlayer serverPlayer, CustomPacketPayload message) {
        // only optional payloads can be missing on a properly connected receiver
        if (NetworkHelper.isOptional(message.type()) && !canSendToPlayer(serverPlayer, message.type())) return;

        ServerPlayNetworking.send(serverPlayer, message);
    }

    public static void sendToAllClientPlayers(CustomPacketPayload message) {
        for (var p : PlatHelper.getCurrentServer().getPlayerList().getPlayers()) {
            sendToClientPlayer(p, message);
        }
    }

    public static void sendToAllClientPlayersInRange(ServerLevel level, BlockPos pos, double radius, CustomPacketPayload message) {
        MinecraftServer currentServer = PlatHelper.getCurrentServer();
        if (!level.isClientSide() && currentServer != null) {
            PlayerList players = currentServer.getPlayerList();
            var dimension = level.dimension();

            players.broadcast(null, pos.getX(), pos.getY(), pos.getZ(),
                    radius, dimension, ServerPlayNetworking.createClientboundPacket(message));
        } else throw makeAssertionError();

    }

    private static @NotNull AssertionError makeAssertionError() {
        return new AssertionError("Cant send message to clients from client side!");
    }

    public static void sendToAllClientPlayersTrackingEntity(Entity target, CustomPacketPayload message) {
        Level level = target.level();
        if (level.isClientSide()) throw makeAssertionError();
        if (level instanceof ServerLevel serverLevel) {
            serverLevel.getChunkSource().sendToTrackingPlayers(target, ServerPlayNetworking.createClientboundPacket(message));
        }
    }

    public static void sendToAllClientPlayersTrackingEntityAndSelf(Entity target, Message message) {
        Level level = target.level();
        if (level.isClientSide()) throw makeAssertionError();
        if (level instanceof ServerLevel serverLevel) {
            serverLevel.getChunkSource().sendToTrackingPlayersAndSelf(target,
                    ServerPlayNetworking.createClientboundPacket(message));
        }
    }

    public static void sendToServer(CustomPacketPayload message) {
        ClientPlayNetworking.send(message);
    }

    public static void sendToAllClientPlayersTrackingChunk(ServerLevel level, ChunkPos pos, CustomPacketPayload message) {
        for (Player player : level.getChunkSource().chunkMap.getPlayers(pos, false)) {
            if (player instanceof ServerPlayer serverPlayer) {
                sendToClientPlayer(serverPlayer, message);
            }
        }
    }
}

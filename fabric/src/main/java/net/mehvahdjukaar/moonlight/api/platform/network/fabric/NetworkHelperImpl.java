package net.mehvahdjukaar.moonlight.api.platform.network.fabric;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.mehvahdjukaar.moonlight.api.platform.PlatHelper;
import net.mehvahdjukaar.moonlight.api.platform.network.NetworkHelper;
import net.mehvahdjukaar.moonlight.api.platform.network.Message;
import net.mehvahdjukaar.moonlight.api.platform.network.NetworkDir;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Function;

public class NetworkHelperImpl  {

    protected static final Map<Class<?>, ResourceLocation> ID_MAP = new ConcurrentHashMap<>();

    public static void addRegistration(String modId, Consumer<NetworkHelper.RegEvent> eventConsumer) {

        eventConsumer.accept(new NetworkHelper.RegEvent() {
            int idCount = 0;
            @Override
            public NetworkHelper.RegEvent setVersion(int version) {
                return this;
            }

            @Override
            public <M extends Message> NetworkHelper.RegEvent register(NetworkDir direction, Class<M> messageClass, Function<FriendlyByteBuf, M> decoder) {
                ResourceLocation res = new ResourceLocation(modId, String.valueOf(idCount++));
                registerId(res, messageClass);

                if (direction != NetworkDir.CLIENTBOUND) {
                    ServerPlayNetworking.registerGlobalReceiver(
                            res, (server, player, h, buf, r) -> {
                                M message = decoder.apply(buf);
                                server.execute(() -> message.handle(new Wrapper(player, NetworkDir.SERVERBOUND, h)));
                            });
                }

                if (direction != NetworkDir.SERVERBOUND) {
                    if (PlatHelper.getPhysicalSide().isClient()) FabricClientNetwork.register(res, decoder);
                }
                return this;
            }
        });
    }

    private static <M extends Message> void registerId(ResourceLocation id, Class<M> clazz) {
        try {
            ID_MAP.put(clazz, id);
        } catch (Exception e) {
            throw new IllegalStateException("Can't register multiple payloads with same class " + clazz);
        }
    }

    private static Packet<?> toVanilla(Message message) {
        FriendlyByteBuf buf = PacketByteBufs.create();
        message.write(buf);
        return ServerPlayNetworking.createS2CPacket(ID_MAP.get(message.getClass()), buf);
    }

    public static void sendToClientPlayer(ServerPlayer serverPlayer, Message message) {
        //for (ServerPlayer player : PlayerLookup.tracking(entity)) {
        FriendlyByteBuf buf = PacketByteBufs.create();
        message.write(buf);
        ServerPlayNetworking.send(serverPlayer, ID_MAP.get(message.getClass()), buf);
        // }
    }

    public static void sendToAllClientPlayers(Message message) {
        for (var p : PlatHelper.getCurrentServer().getPlayerList().getPlayers()) {
            sendToClientPlayer(p, message);
        }
    }

    public static void sendToServer(Message message) {
        FriendlyByteBuf buf = PacketByteBufs.create();
        message.write(buf);
        ClientPlayNetworking.send(ID_MAP.get(message.getClass()), buf);
    }

    public static void sendToAllClientPlayersInRange(Level level, BlockPos pos, double radius, Message message) {

        MinecraftServer currentServer = PlatHelper.getCurrentServer();
        if (!level.isClientSide && currentServer != null) {
            PlayerList players = currentServer.getPlayerList();
            var dimension = level.dimension();

            players.broadcast(null, pos.getX(), pos.getY(), pos.getZ(),
                    radius, dimension, toVanilla(message));
        } else if (PlatHelper.isDev()) throw new AssertionError("Cant send message to clients from client side");
    }

    public static void sentToAllClientPlayersTrackingEntity(Entity target, Message message) {
        if (target.level() instanceof ServerLevel serverLevel) {
            serverLevel.getChunkSource().broadcast(target, toVanilla(message));
        } else if (PlatHelper.isDev()) throw new AssertionError("Cant send message to clients from client side");
    }

    public static void sentToAllClientPlayersTrackingEntityAndSelf(Entity target, Message message) {
        if (target.level() instanceof ServerLevel serverLevel) {
            serverLevel.getChunkSource().broadcast(target, toVanilla(message));
            if (target instanceof ServerPlayer player) {
                sendToClientPlayer(player, message);
            }
        } else if (PlatHelper.isDev()) throw new AssertionError("Cant send message to clients from client side");
    }


    static class Wrapper implements NetworkHelper.Context {

        private final Player player;
        private final NetworkDir dir;
        @Nullable
        private final ServerGamePacketListenerImpl packetListener;

        public Wrapper(Player player, NetworkDir dir, ServerGamePacketListenerImpl packetListener) {
            this.player = player;
            this.dir = dir;
            this.packetListener = packetListener;
        }

        @Override
        public NetworkDir getDirection() {
            return dir;
        }

        @Override
        public Player getSender() {
            return player;
        }

        @Override
        public void disconnect(Component reason) {
            if (packetListener != null) packetListener.disconnect(reason);
            else if (PlatHelper.isDev()) throw new AssertionError("Cant disconnect on client");
        }
    }

}

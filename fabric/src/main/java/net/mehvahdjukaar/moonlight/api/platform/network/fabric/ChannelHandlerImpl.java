package net.mehvahdjukaar.moonlight.api.platform.network.fabric;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.mehvahdjukaar.moonlight.api.platform.PlatHelper;
import net.mehvahdjukaar.moonlight.api.platform.network.ChannelHandler;
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

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.function.IntSupplier;

public class ChannelHandlerImpl extends ChannelHandler {

    private static final Map<Class<?>, ResourceLocation> ID_MAP = new HashMap<>();

    public static ChannelHandler createChannel(String modID, IntSupplier version) {
        return new ChannelHandlerImpl(modID);
    }

    private int id = 0;

    public ChannelHandlerImpl(String modId) {
        super(modId);
    }

    @Override
    public <M extends Message> void register(
            NetworkDir direction,
            Class<M> messageClass,
            Function<FriendlyByteBuf, M> decoder) {

        ResourceLocation res = new ResourceLocation(name, String.valueOf(id++));
        ID_MAP.put(messageClass, res);

        if (direction != NetworkDir.PLAY_TO_CLIENT) {
            ServerPlayNetworking.registerGlobalReceiver(
                    res, (server, player, h, buf, r) -> {
                        M message = decoder.apply(buf);
                        server.execute(() -> message.handle(new Wrapper(player, NetworkDir.PLAY_TO_SERVER, h)));
                    });
        }

        if (direction != NetworkDir.PLAY_TO_SERVER) {
            if (PlatHelper.getPhysicalSide().isClient()) FabricClientNetwork.register(res, decoder);
        }
    }


    static class Wrapper implements Context {

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

    @Override
    public void sendToClientPlayer(ServerPlayer serverPlayer, Message message) {
        //for (ServerPlayer player : PlayerLookup.tracking(entity)) {
        FriendlyByteBuf buf = PacketByteBufs.create();
        message.writeToBuffer(buf);
        ServerPlayNetworking.send(serverPlayer, ID_MAP.get(message.getClass()), buf);
        // }
    }

    @Override
    public void sendToAllClientPlayers(Message message) {
        for (var p : PlatHelper.getCurrentServer().getPlayerList().getPlayers()) {
            sendToClientPlayer(p, message);
        }
    }

    @Override
    public void sendToServer(Message message) {
        FriendlyByteBuf buf = PacketByteBufs.create();
        message.writeToBuffer(buf);
        ClientPlayNetworking.send(ID_MAP.get(message.getClass()), buf);
    }

    @Override
    public void sendToAllClientPlayersInRange(Level level, BlockPos pos, double radius, Message message) {

        MinecraftServer currentServer = PlatHelper.getCurrentServer();
        if (!level.isClientSide && currentServer != null) {
            PlayerList players = currentServer.getPlayerList();
            var dimension = level.dimension();

            players.broadcast(null, pos.getX(), pos.getY(), pos.getZ(),
                    radius, dimension, toVanillaPacket(message));
        } else if (PlatHelper.isDev()) throw new AssertionError("Cant send message to clients from client side");
    }

    @Override
    public void sentToAllClientPlayersTrackingEntity(Entity target, Message message) {
        if (target.level() instanceof ServerLevel serverLevel) {
            serverLevel.getChunkSource().broadcast(target, toVanillaPacket(message));
        } else if (PlatHelper.isDev()) throw new AssertionError("Cant send message to clients from client side");
    }

    @Override
    public void sentToAllClientPlayersTrackingEntityAndSelf(Entity target, Message message) {
        if (target.level() instanceof ServerLevel serverLevel) {
            var p = toVanillaPacket(message);
            serverLevel.getChunkSource().broadcast(target, p);
            if (target instanceof ServerPlayer player) {
                sendToClientPlayer(player, message);
            }
        } else if (PlatHelper.isDev()) throw new AssertionError("Cant send message to clients from client side");
    }

    private Packet<?> toVanillaPacket(Message message) {
        FriendlyByteBuf buf = PacketByteBufs.create();
        message.writeToBuffer(buf);
        return ServerPlayNetworking.createS2CPacket(ID_MAP.get(message.getClass()), buf);
    }

}

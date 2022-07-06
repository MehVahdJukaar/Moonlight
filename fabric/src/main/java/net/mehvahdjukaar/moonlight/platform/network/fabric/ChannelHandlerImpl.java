package net.mehvahdjukaar.moonlight.platform.network.fabric;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.mehvahdjukaar.moonlight.platform.PlatformHelper;
import net.mehvahdjukaar.moonlight.platform.network.ChannelHandler;
import net.mehvahdjukaar.moonlight.platform.network.Message;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class ChannelHandlerImpl extends ChannelHandler {

    public static ChannelHandler createChannel(ResourceLocation channelMame) {
        return new ChannelHandlerImpl(channelMame);
    }

    private int id = 0;

    public ChannelHandlerImpl(ResourceLocation channelName) {
        super(channelName);
    }

    public static Map<Class<?>, ResourceLocation> ID_MAP = new HashMap<>();

    @Override
    public <M extends Message> void register(
            NetworkDir direction,
            Class<M> messageClass,
            Function<FriendlyByteBuf, M> decoder) {

        ResourceLocation res = new ResourceLocation(this.channelName.getNamespace(), "" + id++);
        ID_MAP.put(messageClass, res);

        if (direction == NetworkDir.PLAY_TO_SERVER) {
            ServerPlayNetworking.registerGlobalReceiver(
                    res, (server, player, h, buf, r) -> server.execute(() ->
                            decoder.apply(buf).handle(new Wrapper(player, direction))));
        } else {
            if (PlatformHelper.getEnv().isClient()) FabricClientNetwork.register(res, decoder);
        }
    }


    static class Wrapper implements Context {

        private final Player player;
        private final NetworkDir dir;

        public Wrapper(Player player, NetworkDir dir) {
            this.player = player;
            this.dir = dir;
        }

        @Override
        public NetworkDir getDirection() {
            return dir;
        }

        @Override
        public Player getSender() {
            return player;
        }
    }

    @Override
    public void sendToPlayerClient(ServerPlayer serverPlayer, Message message) {
        //for (ServerPlayer player : PlayerLookup.tracking(entity)) {
        FriendlyByteBuf buf = PacketByteBufs.create();
        message.writeToBuffer(buf);
        ServerPlayNetworking.send(serverPlayer, ID_MAP.get(message.getClass()), buf);
        // }
    }

    @Override
    public void sendToServer(Message message) {
        FriendlyByteBuf buf = PacketByteBufs.create();
        message.writeToBuffer(buf);
        ClientPlayNetworking.send(ID_MAP.get(message.getClass()), buf);
    }
}

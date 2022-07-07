package net.mehvahdjukaar.moonlight.api.platform.network.forge;

import net.mehvahdjukaar.moonlight.api.platform.network.ChannelHandler;
import net.mehvahdjukaar.moonlight.api.platform.network.Message;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

public class ChannelHandlerImpl extends ChannelHandler{

    static Map<ResourceLocation, ChannelHandler> CHANNELS = new HashMap<>();

    public static ChannelHandler createChannel(ResourceLocation channelMame) {
        return CHANNELS.computeIfAbsent(channelMame, c->new ChannelHandlerImpl(channelMame));
    }

    public final SimpleChannel channel;
    public int id = 0;

    public ChannelHandlerImpl(ResourceLocation channelName) {
        super(channelName);
        String version = "1";
        this.channel = NetworkRegistry.newSimpleChannel(channelName, () -> version,
                version::equals, version::equals);
    }

    @Override
    public <M extends Message> void register(
            NetworkDir dir,
            Class<M> messageClass,
            Function<FriendlyByteBuf, M> decoder) {

        NetworkDirection d = dir == NetworkDir.PLAY_TO_SERVER ? NetworkDirection.PLAY_TO_SERVER : NetworkDirection.PLAY_TO_CLIENT;
        channel.registerMessage(id++, messageClass, Message::writeToBuffer, decoder, this::consumer, Optional.of(d));
    }

    private <M extends Message> void consumer(M message, Supplier<NetworkEvent.Context> context) {
        var c = context.get();
        c.enqueueWork(() -> message.handle(new Wrapper(c)));
        c.setPacketHandled(true);
    }

    static class Wrapper implements Context {


        private final NetworkEvent.Context context;

        public Wrapper(NetworkEvent.Context ctx) {
            this.context = ctx;
        }

        @Override
        public NetworkDir getDirection() {
            return switch (context.getDirection()) {
                case PLAY_TO_CLIENT -> NetworkDir.PLAY_TO_CLIENT;
                default -> NetworkDir.PLAY_TO_SERVER;
            };
        }

        @Override
        public Player getSender() {
            return context.getSender();
        }
    }


    public void sendToPlayerClient(ServerPlayer serverPlayer, Message message){
        channel.send(PacketDistributor.PLAYER.with(() -> serverPlayer),message);
    }

    @Override
    public void sendToServer(Message message) {
        channel.sendToServer(message);
    }
}

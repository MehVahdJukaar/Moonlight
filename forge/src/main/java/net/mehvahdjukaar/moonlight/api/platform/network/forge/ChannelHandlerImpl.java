package net.mehvahdjukaar.moonlight.api.platform.network.forge;

import net.mehvahdjukaar.moonlight.api.platform.PlatHelper;
import net.mehvahdjukaar.moonlight.api.platform.network.ChannelHandler;
import net.mehvahdjukaar.moonlight.api.platform.network.Message;
import net.mehvahdjukaar.moonlight.api.platform.network.NetworkDir;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.*;
import net.neoforged.neoforge.network.simple.SimpleChannel;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

import java.util.Optional;
import java.util.function.Function;
import java.util.function.IntSupplier;
import java.util.function.Supplier;

public class ChannelHandlerImpl extends ChannelHandler {


    public static ChannelHandler createChannel(String channelMame, IntSupplier version) {
        return new ChannelHandlerImpl(channelMame, version);
    }

    public final SimpleChannel channel;
    protected int id = 0;

    public ChannelHandlerImpl(String modId, IntSupplier v) {
        super(modId);
        Supplier<String> ver = () -> String.valueOf(v.getAsInt());
        this.channel = NetworkRegistry.newSimpleChannel(ResourceLocation.parse(modId, "channel"), ver,
                ver.get()::equals, ver.get()::equals);
    }

    @Override
    public <M extends Message> void register(
            NetworkDir dir,
            Class<M> messageClass,
            Function<FriendlyByteBuf, M> decoder) {
        Optional<INetworkDirection<?>> d = switch (dir) {
            case BOTH -> Optional.empty();
            case PLAY_TO_CLIENT -> Optional.of(PlayNetworkDirection.PLAY_TO_CLIENT);
            case PLAY_TO_SERVER -> Optional.of(PlayNetworkDirection.PLAY_TO_SERVER);
        };

        channel.registerMessage(id++, messageClass, Message::writeToBuffer, decoder::apply, this::consumer, d);
    }

    private <M extends Message> void consumer(M message, NetworkEvent.Context context) {
        context.enqueueWork(() -> message.handle(new Wrapper(context)));
        context.setPacketHandled(true);
    }

    static class Wrapper implements Context {

        private final NetworkEvent.Context context;

        public Wrapper(NetworkEvent.Context ctx) {
            this.context = ctx;
        }

        @Override
        public NetworkDir getDirection() {
            return switch (context.getDirection().getOriginationSide()) {
                case CLIENT -> NetworkDir.PLAY_TO_CLIENT;
                default -> NetworkDir.PLAY_TO_SERVER;
            };
        }


        @Override
        public Player getSender() {
            return context.getSender();
        }

        @Override
        public void disconnect(Component message){
            context.getNetworkManager().disconnect(message);
        }
    }


    public void sendToClientPlayer(ServerPlayer serverPlayer, Message message) {
        channel.send(PacketDistributor.PLAYER.with(() -> serverPlayer), message);
    }

    @Override
    public void sendToAllClientPlayers(Message message) {
        channel.send(PacketDistributor.ALL.noArg(), message);
    }

    @Override
    public void sendToServer(Message message) {
        channel.sendToServer(message);
    }

    @Override
    public void sendToAllClientPlayersInRange(Level level, BlockPos pos, double radius, Message message) {
        MinecraftServer currentServer = ServerLifecycleHooks.getCurrentServer();
        if (currentServer != null && !level.isClientSide) {
            var distributor = PacketDistributor.NEAR.with(() ->
                    new PacketDistributor.TargetPoint(pos.getX(), pos.getY(), pos.getZ(), radius, level.dimension()));
            channel.send(distributor, message);
        }else if(PlatHelper.isDev())throw new AssertionError("Cant send message to clients from client side");
    }

    @Override
    public void sentToAllClientPlayersTrackingEntity(Entity target, Message message) {
        if(!target.level().isClientSide) {
            channel.send(PacketDistributor.TRACKING_ENTITY.with(() -> target), message);
        }else if(PlatHelper.isDev())throw new AssertionError("Cant send message to clients from client side");
    }

    @Override
    public void sentToAllClientPlayersTrackingEntityAndSelf(Entity target, Message message) {
        if(!target.level().isClientSide) {
            channel.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> target), message);
        }else if(PlatHelper.isDev())throw new AssertionError("Cant send message to clients from client side");
    }


}


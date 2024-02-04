package net.mehvahdjukaar.moonlight.api.platform.network.forge;

import net.mehvahdjukaar.moonlight.api.platform.PlatHelper;
import net.mehvahdjukaar.moonlight.api.platform.network.Message;
import net.mehvahdjukaar.moonlight.api.platform.network.NetworkDir;
import net.mehvahdjukaar.moonlight.api.platform.network.NetworkHelper;
import net.mehvahdjukaar.moonlight.forge.MoonlightForge;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlerEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.IPayloadRegistrar;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Function;

public class NetworkHelperImpl {

    protected static final Map<Class<?>, ResourceLocation> ID_MAP = new ConcurrentHashMap<>();

    public static void addRegistration(String modId, Consumer<NetworkHelper.RegEvent> eventListener) {
        //cursed
        Consumer<RegisterPayloadHandlerEvent> eventConsumer = event -> {
            IPayloadRegistrar registrar = event.registrar(modId);

            eventListener.accept(new NetworkHelper.RegEvent() {
                private int idCounter = 0;

                @Override
                public NetworkHelper.RegEvent setVersion(int version) {
                    registrar.versioned(String.valueOf(version));
                    return this;
                }

                @Override
                public <M extends Message> NetworkHelper.RegEvent register(NetworkDir direction, Class<M> messageClass, Function<FriendlyByteBuf, M> decoder) {
                    ResourceLocation id = new ResourceLocation(modId, String.valueOf(idCounter++));
                    registerId(id, messageClass);
                    registrar.common(id, buf -> wrap(decoder.apply(buf)), (payload, playPayloadContext) -> {
                        playPayloadContext.workHandler().execute(() ->
                                //execute on the main thread
                                payload.message.handle(new ContextWrapper(playPayloadContext)));
                    });
                    return this;
                }
            });
        };
        MoonlightForge.getCurrentModBus().addListener(eventConsumer);
    }

    private static <M extends Message> void registerId(ResourceLocation id, Class<M> clazz) {
        try {
            ID_MAP.put(clazz, id);
        } catch (Exception e) {
            throw new IllegalStateException("Can't register multiple payloads with same class " + clazz);
        }
    }

    private static <M extends Message> MessagePayload wrap(M message) {
        ResourceLocation id = ID_MAP.get(message.getClass());
        if (id == null) {
            throw new IllegalStateException("Unknown message with class: " + message.getClass());
        }
        return new MessagePayload(message, id);
    }

    public static void sendToServer(Message message) {
        PacketDistributor.SERVER.noArg().send(wrap(message));
    }

    public static void sendToClientPlayer(ServerPlayer serverPlayer, Message message) {
        PacketDistributor.PLAYER.with(serverPlayer).send(wrap(message));
    }

    public static void sendToAllClientPlayers(Message message) {
        PacketDistributor.ALL.noArg().send(wrap(message));
    }

    public static void sendToAllClientPlayersInRange(Level level, BlockPos pos, double radius, Message message) {
        MinecraftServer currentServer = ServerLifecycleHooks.getCurrentServer();
        if (currentServer != null && !level.isClientSide) {
            var distributor = PacketDistributor.NEAR.with(
                    new PacketDistributor.TargetPoint(pos.getX(), pos.getY(), pos.getZ(), radius, level.dimension()));
            distributor.send(wrap(message));
        } else if (PlatHelper.isDev()) throw new AssertionError("Cant send message to clients from client side");
    }

    public static void sentToAllClientPlayersTrackingEntity(Entity target, Message message) {
        if (!target.level().isClientSide) {
            PacketDistributor.TRACKING_ENTITY.with(target).send(wrap(message));
        } else if (PlatHelper.isDev()) throw new AssertionError("Cant send message to clients from client side");
    }

    public static void sentToAllClientPlayersTrackingEntityAndSelf(Entity target, Message message) {
        if (!target.level().isClientSide) {
            PacketDistributor.TRACKING_ENTITY_AND_SELF.with(target).send(wrap(message));
        } else if (PlatHelper.isDev()) throw new AssertionError("Cant send message to clients from client side");
    }

    //wrapper with id
    private record MessagePayload(Message message, ResourceLocation id) implements CustomPacketPayload {
        @Override
        public void write(FriendlyByteBuf buffer) {
            message.write(buffer);
        }
    }

    record ContextWrapper(IPayloadContext context) implements NetworkHelper.Context {
        @Override
        public NetworkDir getDirection() {
            if (context.flow() == PacketFlow.CLIENTBOUND) {
                return NetworkDir.CLIENTBOUND;
            } else return NetworkDir.SERVERBOUND;
        }

        @Nullable
        @Override
        public Player getSender() {
            return context.player().orElse(null);
        }

        @Override
        public void disconnect(Component message) {
            context.packetHandler().disconnect(message);
        }
    }
}


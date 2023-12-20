package net.mehvahdjukaar.moonlight.api.platform.network;

import dev.architectury.injectables.annotations.ExpectPlatform;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.IntSupplier;

/**
 * Your main network channel instance.
 */
public abstract class ChannelHandler {

    public static Builder builder(String modId) {
        return new Builder(modId);
    }

    public static class Builder {
        private final ChannelHandler instance;
        private int version = 0;

        protected Builder(String modId) {
            instance = createChannel(modId, () -> version);
        }

        public <M extends Message> Builder register(
                NetworkDir direction,
                Class<M> messageClass,
                Function<FriendlyByteBuf, M> decoder) {
            instance.register(direction, messageClass, decoder);
            return this;
        }

        public Builder and(Consumer<Builder> consumer){
            consumer.accept(this);
            return this;
        }

        public Builder version(int version) {
            this.version = version;
            return this;
        }

        public ChannelHandler build() {
            return instance;
        }
    }

    @Deprecated(forRemoval = true)
    public static ChannelHandler createChannel(ResourceLocation channelMame, int version) {
        return createChannel(channelMame.getNamespace(), () -> version);
    }

    @Deprecated(forRemoval = true)
    public static ChannelHandler createChannel(ResourceLocation channelMame) {
        return createChannel(channelMame, 1);
    }

    public static ChannelHandler createChannel(String modId) {
        return createChannel(modId, () -> 0);
    }

    @ExpectPlatform
    public static ChannelHandler createChannel(String modId, IntSupplier version) {
        throw new AssertionError();
    }

    protected final String name;

    protected ChannelHandler(String modId) {
        this.name = modId;
    }

    protected abstract <M extends Message> void register(
            NetworkDir direction,
            Class<M> messageClass,
            Function<FriendlyByteBuf, M> decoder);


    public interface Context {
        NetworkDir getDirection();

        Player getSender();

        void disconnect(Component reason);
    }


    public abstract void sendToClientPlayer(ServerPlayer serverPlayer, Message message);

    public abstract void sendToAllClientPlayers(Message message);

    public abstract void sendToAllClientPlayersInRange(Level level, BlockPos pos, double radius, Message message);

    public void sendToAllClientPlayersInDefaultRange(Level level, BlockPos pos, Message message){
        sendToAllClientPlayersInRange(level, pos, 64, message);
    };

    public abstract void sentToAllClientPlayersTrackingEntity(Entity target, Message message);

    public abstract void sentToAllClientPlayersTrackingEntityAndSelf(Entity target, Message message);

    public abstract void sendToServer(Message message);

}

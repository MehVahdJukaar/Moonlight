package net.mehvahdjukaar.moonlight.api.platform.network;

import dev.architectury.injectables.annotations.ExpectPlatform;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Your main network channel instance.
 */
public class NetworkHelper {


    @ExpectPlatform
    public static void addRegistration(String modId, Consumer<RegEvent> eventConsumer) {
        throw new AssertionError();
    }

    public interface RegEvent {

        RegEvent setVersion(int version);

        <M extends Message> RegEvent register(
                NetworkDir direction,
                Class<M> messageClass,
                Function<FriendlyByteBuf, M> decoder);

        default RegEvent and(Consumer<RegEvent> eventConsumer){
            eventConsumer.accept(this);
            return this;
        }
    }

    protected final String name;

    protected NetworkHelper(String modId) {
        this.name = modId;
    }

    public interface Context {
        NetworkDir getDirection();

        @Nullable
        Player getSender();

        void disconnect(Component reason);
    }

    @ExpectPlatform
    public static void sendToClientPlayer(ServerPlayer serverPlayer, Message message) {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static void sendToAllClientPlayers(Message message) {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static void sendToAllClientPlayersInRange(Level level, BlockPos pos, double radius, Message message) {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static void sentToAllClientPlayersTrackingEntity(Entity target, Message message) {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static void sentToAllClientPlayersTrackingEntityAndSelf(Entity target, Message message) {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static void sendToServer(Message message) {
        throw new AssertionError();
    }

}

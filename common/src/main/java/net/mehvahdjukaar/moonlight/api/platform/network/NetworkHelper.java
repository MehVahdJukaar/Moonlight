package net.mehvahdjukaar.moonlight.api.platform.network;

import dev.architectury.injectables.annotations.ExpectPlatform;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;

import java.util.function.Consumer;

public class NetworkHelper {

    @ExpectPlatform
    public static void addNetworkRegistration(Consumer<RegisterMessagesEvent> eventListener, int version) {
        throw new AssertionError();
    }

    public interface RegisterMessagesEvent {
        <M extends Message> void registerServerBound(CustomPacketPayload.TypeAndCodec<RegistryFriendlyByteBuf, M> messageType);

        <M extends Message> void registerClientBound(CustomPacketPayload.TypeAndCodec<RegistryFriendlyByteBuf, M> messageType);

        <M extends Message> void registerBidirectional(CustomPacketPayload.TypeAndCodec<RegistryFriendlyByteBuf, M> messageType);
    }


    @ExpectPlatform
    public static void sendToClientPlayer(ServerPlayer serverPlayer, CustomPacketPayload message) {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static void sendToAllClientPlayers(CustomPacketPayload message) {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static void sendToAllClientPlayersInRange(ServerLevel level, BlockPos pos, double radius, CustomPacketPayload message) {
        throw new AssertionError();
    }

    public static void sendToAllClientPlayersInDefaultRange(ServerLevel level, BlockPos pos, CustomPacketPayload message) {
        sendToAllClientPlayersInRange(level, pos, 64, message);
    }

    // same distance as serverlevel send particles
    public static void sendToAllClientPlayersInParticleRange(Level level, BlockPos pos, CustomPacketPayload message){
        sendToAllClientPlayersInRange(level, pos, 32, message);
    }

    public static void sendToAllClientPlayersInDistantParticleRange(Level level, BlockPos pos, CustomPacketPayload message){
        sendToAllClientPlayersInRange(level, pos, 512, message);
    }

    @ExpectPlatform
    public static void sentToAllClientPlayersTrackingEntity(Entity target, CustomPacketPayload message) {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static void sentToAllClientPlayersTrackingEntityAndSelf(Entity target, Message message) {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static void sendToServer(CustomPacketPayload message) {
        throw new AssertionError();
    }


}

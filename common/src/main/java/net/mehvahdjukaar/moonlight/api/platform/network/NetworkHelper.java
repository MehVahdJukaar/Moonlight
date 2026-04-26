package net.mehvahdjukaar.moonlight.api.platform.network;

import net.mehvahdjukaar.candlelight.api.PlatformImpl;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;

import java.util.function.Consumer;

public class NetworkHelper {

    @PlatformImpl
    public static void addNetworkRegistration(Consumer<RegisterMessagesEvent> eventListener, int version) {
        throw new AssertionError();
    }

    public interface RegisterMessagesEvent {
        <M extends Message> void registerServerBound(CustomPacketPayload.TypeAndCodec<RegistryFriendlyByteBuf, M> messageType);

        <M extends Message> void registerClientBound(CustomPacketPayload.TypeAndCodec<RegistryFriendlyByteBuf, M> messageType);

        <M extends Message> void registerBidirectional(CustomPacketPayload.TypeAndCodec<RegistryFriendlyByteBuf, M> messageType);
    }


    @PlatformImpl
    public static void sendToClientPlayer(ServerPlayer serverPlayer, CustomPacketPayload message) {
        throw new AssertionError();
    }

    @PlatformImpl
    public static void sendToAllClientPlayers(CustomPacketPayload message) {
        throw new AssertionError();
    }

    @PlatformImpl
    public static void sendToAllClientPlayersInRange(ServerLevel level, BlockPos pos, double radius, CustomPacketPayload message) {
        throw new AssertionError();
    }

    public static void sendToAllClientPlayersInDefaultRange(ServerLevel level, BlockPos pos, CustomPacketPayload message) {
        sendToAllClientPlayersInRange(level, pos, 64, message);
    }

    // same distance as serverlevel send particles
    public static void sendToAllClientPlayersInParticleRange(ServerLevel level, BlockPos pos, CustomPacketPayload message) {
        sendToAllClientPlayersInRange(level, pos, 32, message);
    }

    public static void sendToAllClientPlayersInDistantParticleRange(ServerLevel level, BlockPos pos, CustomPacketPayload message) {
        sendToAllClientPlayersInRange(level, pos, 512, message);
    }

    @PlatformImpl
    public static void sendToAllClientPlayersTrackingEntity(Entity target, CustomPacketPayload message) {
        throw new AssertionError();
    }

    @PlatformImpl
    public static void sendToAllClientPlayersTrackingChunk(ServerLevel level, ChunkPos pos, CustomPacketPayload message) {
        throw new AssertionError();
    }

    @Deprecated(forRemoval = true)
    public static void sentToAllClientPlayersTrackingEntity(Entity target, CustomPacketPayload message) {
        sendToAllClientPlayersTrackingEntity(target, message);
    }

    @Deprecated(forRemoval = true)
    public static void sentToAllClientPlayersTrackingEntityAndSelf(Entity target, Message message) {
        sendToAllClientPlayersTrackingEntityAndSelf(target, message);
    }

    @PlatformImpl
    public static void sendToAllClientPlayersTrackingEntityAndSelf(Entity target, Message message) {
        throw new AssertionError();
    }

    @PlatformImpl
    public static void sendToServer(CustomPacketPayload message) {
        throw new AssertionError();
    }


}

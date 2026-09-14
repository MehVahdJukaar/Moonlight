package net.mehvahdjukaar.moonlight.api.platform.network.platform;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/**
 * Never sent, and dropped if it ever arrives. Fabric's handshake only tells each side which channels the other
 * can <em>receive</em>, which leaves a clientbound-only payload invisible to clients. Registering one of these
 * serverbound alongside an optional clientbound payload is what puts the mod on the list the server advertises,
 * and so what lets a client tell that the server has it.
 *
 * <p>It carries nothing, so a client pushing junk up this channel costs the server a set lookup.
 */
record PresenceMarker(CustomPacketPayload.Type<PresenceMarker> type) implements CustomPacketPayload {

    static ResourceLocation idOf(CustomPacketPayload.Type<?> clientBound) {
        return clientBound.id().withSuffix("_presence");
    }

    static void register(CustomPacketPayload.Type<?> clientBound) {
        Type<PresenceMarker> type = new Type<>(idOf(clientBound));
        StreamCodec<RegistryFriendlyByteBuf, PresenceMarker> codec = StreamCodec.unit(new PresenceMarker(type));

        PayloadTypeRegistry.playC2S().register(type, codec);
        ServerPlayNetworking.registerGlobalReceiver(type, (message, context) -> {});
    }
}

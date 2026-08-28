package net.mehvahdjukaar.moonlight.api.platform.network.platform;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

// never sent. Fabric's handshake only advertises channels a side can receive, so this serverbound marker is
// what lets a client see the server has the matching clientbound payload
record PresenceMarker(CustomPacketPayload.Type<PresenceMarker> type) implements CustomPacketPayload {

    static Identifier idOf(CustomPacketPayload.Type<?> clientBound) {
        return clientBound.id().withSuffix("_presence");
    }

    static void register(CustomPacketPayload.Type<?> clientBound) {
        Type<PresenceMarker> type = new Type<>(idOf(clientBound));
        StreamCodec<RegistryFriendlyByteBuf, PresenceMarker> codec = StreamCodec.unit(new PresenceMarker(type));

        PayloadTypeRegistry.serverboundPlay().register(type, codec);
        ServerPlayNetworking.registerGlobalReceiver(type, (message, context) -> {});
    }
}

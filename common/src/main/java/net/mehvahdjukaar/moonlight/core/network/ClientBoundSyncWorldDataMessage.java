package net.mehvahdjukaar.moonlight.core.network;

import com.google.common.base.Preconditions;
import net.mehvahdjukaar.moonlight.api.misc.WorldSavedData;
import net.mehvahdjukaar.moonlight.api.misc.WorldSavedDataType;
import net.mehvahdjukaar.moonlight.api.platform.network.Message;
import net.mehvahdjukaar.moonlight.core.Moonlight;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public class ClientBoundSyncWorldDataMessage<D extends WorldSavedData> implements Message {
    private final D data;

    public static final TypeAndCodec<RegistryFriendlyByteBuf, ClientBoundSyncWorldDataMessage<?>> TYPE = Message.makeType(
            Moonlight.res("s2c_sync_world_data"), ClientBoundSyncWorldDataMessage::fromNetwork);

    public ClientBoundSyncWorldDataMessage(D data) {
        this.data = data;
    }

    private static ClientBoundSyncWorldDataMessage<?> fromNetwork(RegistryFriendlyByteBuf buffer) {
        WorldSavedDataType<?> type = WorldSavedDataType.STREAM_CODEC.decode(buffer);
        return new ClientBoundSyncWorldDataMessage<>(
                Preconditions.checkNotNull(type.getStreamCodec()).decode(buffer));
    }

    @Override
    public void write(RegistryFriendlyByteBuf buf) {
        WorldSavedDataType<WorldSavedData> type = this.data.getType();
        WorldSavedDataType.STREAM_CODEC.encode(buf, type);
        Preconditions.checkNotNull(this.data.getType().getStreamCodec()).encode(buf, this.data);
    }

    @Override
    public void handle(Context context) {
        //assigns data to client
        this.data.getType().setClientInstance(this.data);
        Moonlight.LOGGER.info("Synced Custom World Data [{}]", this.data.getType().getName());
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE.type();
    }
}

package net.mehvahdjukaar.moonlight.core.network;

import net.mehvahdjukaar.moonlight.api.platform.network.Message;
import net.mehvahdjukaar.moonlight.core.Moonlight;
import net.mehvahdjukaar.moonlight.core.fluid.SoftFluidInternal;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

//after data load
public class ClientBoundFinalizeFluidsMessage implements Message {

    public static final TypeAndCodec<RegistryFriendlyByteBuf, ClientBoundFinalizeFluidsMessage> TYPE =
            Message.makeType(Moonlight.res("s2c_finalize_fluids"), ClientBoundFinalizeFluidsMessage::new);

    public ClientBoundFinalizeFluidsMessage() {
    }

    public ClientBoundFinalizeFluidsMessage(RegistryFriendlyByteBuf pBuffer) {
    }

    @Override
    public void write(RegistryFriendlyByteBuf buf) {

    }

    @Override
    public void handle(Context context) {
        SoftFluidInternal.postInitClient();
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE.type();
    }
}

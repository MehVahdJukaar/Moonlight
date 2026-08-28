package net.mehvahdjukaar.moonlight.core.network;

import net.mehvahdjukaar.moonlight.api.fluids.client.SoftFluidColors;
import net.mehvahdjukaar.moonlight.api.misc.HolderRef;
import net.mehvahdjukaar.moonlight.api.platform.network.Message;
import net.mehvahdjukaar.moonlight.core.Moonlight;
import net.mehvahdjukaar.moonlight.core.fluid.SoftFluidInternal;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import java.util.Objects;

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
        RegistryAccess registryAccess = Objects.requireNonNull(context.getPlayer().level()).registryAccess();
        SoftFluidInternal.postInitClient(registryAccess);
        SoftFluidColors.onFluidsSynced(registryAccess);
        //just incase
        HolderRef.clearCache();
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE.type();
    }
}

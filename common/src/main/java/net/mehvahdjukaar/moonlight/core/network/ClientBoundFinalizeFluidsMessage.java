package net.mehvahdjukaar.moonlight.core.network;

import net.mehvahdjukaar.moonlight.api.fluids.SoftFluidRegistry;
import net.mehvahdjukaar.moonlight.api.platform.network.NetworkDir;
import net.mehvahdjukaar.moonlight.api.platform.network.ChannelHandler;
import net.mehvahdjukaar.moonlight.api.platform.network.Message;
import net.mehvahdjukaar.moonlight.core.fluid.SoftFluidInternal;
import net.minecraft.network.FriendlyByteBuf;

//after data load
public class ClientBoundFinalizeFluidsMessage implements Message {

    public ClientBoundFinalizeFluidsMessage() {
    }

    public ClientBoundFinalizeFluidsMessage(FriendlyByteBuf pBuffer) {
    }

    @Override
    public void writeToBuffer(FriendlyByteBuf buffer) {
    }

    @Override
    public void handle(ChannelHandler.Context context) {
        if (context.getDirection() == NetworkDir.PLAY_TO_CLIENT) {
            SoftFluidInternal.postInitClient();
        }
    }

}

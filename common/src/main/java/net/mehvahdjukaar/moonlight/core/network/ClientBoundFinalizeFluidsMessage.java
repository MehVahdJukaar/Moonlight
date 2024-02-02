package net.mehvahdjukaar.moonlight.core.network;

import net.mehvahdjukaar.moonlight.api.fluids.SoftFluidRegistry;
import net.mehvahdjukaar.moonlight.api.platform.network.NetworkDir;
import net.mehvahdjukaar.moonlight.api.platform.network.NetworkHelper;
import net.mehvahdjukaar.moonlight.api.platform.network.Message;
import net.minecraft.network.FriendlyByteBuf;

//after data load
public class ClientBoundFinalizeFluidsMessage implements Message {

    public ClientBoundFinalizeFluidsMessage() {
    }

    public ClientBoundFinalizeFluidsMessage(FriendlyByteBuf pBuffer) {
    }

    @Override
    public void write(FriendlyByteBuf buffer) {
    }

    @Override
    public void handle(NetworkHelper.Context context) {
        if (context.getDirection() == NetworkDir.CLIENTBOUND) {
            SoftFluidRegistry.postInitClient();
        }
    }

}

package net.mehvahdjukaar.moonlight.core.network;


import net.mehvahdjukaar.moonlight.api.fluids.SoftFluidRegistry;
import net.mehvahdjukaar.moonlight.api.platform.network.ChannelHandler;
import net.mehvahdjukaar.moonlight.api.platform.network.Message;
import net.mehvahdjukaar.moonlight.core.map.MapDataInternal;
import net.mehvahdjukaar.moonlight.core.misc.AntiRepostWarning;
import net.minecraft.network.FriendlyByteBuf;

public class ClientBoundSendLoginPacket implements Message {

    public ClientBoundSendLoginPacket(FriendlyByteBuf buf) {
    }

    public ClientBoundSendLoginPacket() {
    }

    @Override
    public void writeToBuffer(FriendlyByteBuf buf) {
    }

    @Override
    public void handle(ChannelHandler.Context context) {
        AntiRepostWarning.run();

        try {
            //check on datapack registries on client
            SoftFluidRegistry.empty();
        } catch (Exception e) {
            throw new RuntimeException("Not all required entries were found in datapack registry. How did this happen?", e);
        }
    }
}
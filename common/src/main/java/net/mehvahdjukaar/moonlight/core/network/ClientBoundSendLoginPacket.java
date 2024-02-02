package net.mehvahdjukaar.moonlight.core.network;


import net.mehvahdjukaar.moonlight.api.fluids.SoftFluidRegistry;
import net.mehvahdjukaar.moonlight.api.platform.network.NetworkHelper;
import net.mehvahdjukaar.moonlight.api.platform.network.Message;
import net.mehvahdjukaar.moonlight.core.misc.AntiRepostWarning;
import net.minecraft.network.FriendlyByteBuf;

public class ClientBoundSendLoginPacket implements Message {

    public ClientBoundSendLoginPacket(FriendlyByteBuf buf) {
    }

    public ClientBoundSendLoginPacket() {
    }

    @Override
    public void write(FriendlyByteBuf buf) {
    }

    @Override
    public void handle(NetworkHelper.Context context) {
        AntiRepostWarning.run();

        try {
            //check on datapack registries on client
            SoftFluidRegistry.getEmpty();
        } catch (Exception e) {
            throw new RuntimeException("Not all required entries were found in datapack registry. How did this happen?", e);
        }
    }
}
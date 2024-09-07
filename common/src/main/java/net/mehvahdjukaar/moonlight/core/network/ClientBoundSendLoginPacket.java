package net.mehvahdjukaar.moonlight.core.network;


import net.mehvahdjukaar.moonlight.api.fluids.BuiltInSoftFluids;
import net.mehvahdjukaar.moonlight.api.fluids.SoftFluidRegistry;
import net.mehvahdjukaar.moonlight.api.platform.network.ChannelHandler;
import net.mehvahdjukaar.moonlight.api.platform.network.Message;
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
            BuiltInSoftFluids.WATER.value();
        } catch (Exception e) {
            throw new IllegalStateException("Not all required entries were found in datapack registry. How did this happen?" +
                    "This is NOT a Moonlight issue. Do not report there. This can only be caused by some other mod messing up mod added datapack registries."+
                    "Crashing now to prevent inevitable random crashes in-game.", e);
        }
    }
}
package net.mehvahdjukaar.moonlight.core.network;


import net.mehvahdjukaar.moonlight.api.fluids.BuiltInSoftFluids;
import net.mehvahdjukaar.moonlight.api.fluids.SoftFluidRegistry;
import net.mehvahdjukaar.moonlight.api.misc.DynamicHolder;
import net.mehvahdjukaar.moonlight.api.platform.network.Message;
import net.mehvahdjukaar.moonlight.core.Moonlight;
import net.mehvahdjukaar.moonlight.core.misc.AntiRepostWarning;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public class ClientBoundSendLoginPacket implements Message {

    public static final TypeAndCodec<RegistryFriendlyByteBuf, ClientBoundSendLoginPacket> TYPE = Message.makeType(
            Moonlight.res("s2c_send_login"), ClientBoundSendLoginPacket::new);

    public ClientBoundSendLoginPacket(RegistryFriendlyByteBuf buf) {
    }

    public ClientBoundSendLoginPacket() {
    }

    @Override
    public void write(RegistryFriendlyByteBuf buf) {
    }

    @Override
    public void handle(Context context) {
        AntiRepostWarning.run();
        DynamicHolder.clearCache();
        try {
            //check on datapack registries on client
            SoftFluidRegistry.empty();
            BuiltInSoftFluids.WATER.value();
        } catch (Exception e) {
            throw new IllegalStateException("Not all required entries were found in datapack registry. How did this happen?" +
                    "This is NOT a Moonlight issue. Do not report there. This can only be caused by some other mod messing up mod added datapack registries." +
                    "Crashing now to prevent inevitable random crashes in-game.", e);
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE.type();
    }
}
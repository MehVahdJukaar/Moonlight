package net.mehvahdjukaar.moonlight.core.network;


import net.mehvahdjukaar.moonlight.api.platform.network.Message;
import net.mehvahdjukaar.moonlight.core.Moonlight;
import net.mehvahdjukaar.moonlight.core.misc.AntiRepostWarning;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public class ClientBoundSendLoginMessage implements Message {

    public static final TypeAndCodec<RegistryFriendlyByteBuf, ClientBoundSendLoginMessage> TYPE = Message.makeType(
            Moonlight.res("s2c_send_login"), ClientBoundSendLoginMessage::new);

    public ClientBoundSendLoginMessage(RegistryFriendlyByteBuf buf) {
    }

    public ClientBoundSendLoginMessage() {
    }

    @Override
    public void write(RegistryFriendlyByteBuf buf) {
    }

    @Override
    public void handle(Context context) {
        AntiRepostWarning.run();
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE.type();
    }
}
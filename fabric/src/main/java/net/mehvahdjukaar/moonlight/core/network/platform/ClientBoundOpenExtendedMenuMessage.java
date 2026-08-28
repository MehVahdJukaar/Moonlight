package net.mehvahdjukaar.moonlight.core.network.platform;

import io.netty.buffer.Unpooled;
import net.mehvahdjukaar.moonlight.api.platform.network.Message;
import net.mehvahdjukaar.moonlight.core.Moonlight;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import org.jetbrains.annotations.Nullable;

// sent right before player.openMenu and stashed until the client menu factory consumes it
public class ClientBoundOpenExtendedMenuMessage implements Message {

    public static final TypeAndCodec<RegistryFriendlyByteBuf, ClientBoundOpenExtendedMenuMessage> TYPE = Message.makeType(
            Moonlight.res("s2c_open_extended_menu"), ClientBoundOpenExtendedMenuMessage::new);

    @Nullable
    private static FriendlyByteBuf pendingData;

    private final byte[] data;

    public ClientBoundOpenExtendedMenuMessage(byte[] data) {
        this.data = data;
    }

    public ClientBoundOpenExtendedMenuMessage(RegistryFriendlyByteBuf buf) {
        this.data = buf.readByteArray();
    }

    @Override
    public void write(RegistryFriendlyByteBuf buf) {
        buf.writeByteArray(data);
    }

    @Override
    public void handle(Context context) {
        pendingData = new FriendlyByteBuf(Unpooled.wrappedBuffer(data));
    }

    @Nullable
    public static FriendlyByteBuf consumePendingData() {
        var data = pendingData;
        pendingData = null;
        return data;
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE.type();
    }
}

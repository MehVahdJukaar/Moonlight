package net.mehvahdjukaar.moonlight.core.network;

import dev.architectury.injectables.annotations.ExpectPlatform;
import net.mehvahdjukaar.moonlight.api.platform.network.ChannelHandler;
import net.mehvahdjukaar.moonlight.api.platform.network.Message;
import net.minecraft.network.FriendlyByteBuf;

public class ClientBoundSyncConfigsPacket implements Message {

    private final String fineName;
    private final String modId;
    private final byte[] configData;

    public ClientBoundSyncConfigsPacket(FriendlyByteBuf buf) {
        this.modId = buf.readUtf();
        this.fineName = buf.readUtf();
        this.configData = buf.readByteArray();
    }

    public ClientBoundSyncConfigsPacket(final byte[] configFileData, final String fileName, String modId) {
        this.modId = modId;
        this.fineName = fileName;
        this.configData = configFileData;
    }

    @Override
    public void writeToBuffer(FriendlyByteBuf buf) {
        buf.writeUtf(this.modId);
        buf.writeUtf(this.fineName);
        buf.writeByteArray(this.configData);
    }

    @Override
    public void handle(ChannelHandler.Context context) {
        acceptConfigs(this, context);
    }

    @ExpectPlatform
    public static void acceptConfigs(ClientBoundSyncConfigsPacket packet, ChannelHandler.Context context) {
        throw new AssertionError();
    }


}

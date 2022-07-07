package net.mehvahdjukaar.moonlight.core.network;

import dev.architectury.injectables.annotations.ExpectPlatform;
import net.mehvahdjukaar.moonlight.api.platform.configs.ConfigSpec;
import net.mehvahdjukaar.moonlight.api.platform.configs.ConfigType;
import net.mehvahdjukaar.moonlight.api.platform.network.ChannelHandler;
import net.mehvahdjukaar.moonlight.api.platform.network.Message;
import net.mehvahdjukaar.moonlight.core.Moonlight;
import net.minecraft.network.FriendlyByteBuf;

import java.io.ByteArrayInputStream;

public class ClientBoundSyncConfigsPacket implements Message {

    public final String fineName;
    public final String modId;
    public final byte[] configData;

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
        var config = ConfigSpec.getSpec(this.modId, ConfigType.COMMON);
        if (config != null) {
            config.loadFromBytes( new ByteArrayInputStream(this.configData));
            Moonlight.LOGGER.info("Synced {} configs", this.fineName);
        } else {
            Moonlight.LOGGER.error("Failed to find config file with name {}", this.fineName);
        }
    }


}

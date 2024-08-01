package net.mehvahdjukaar.moonlight.core.network;

import net.mehvahdjukaar.moonlight.api.platform.configs.ConfigSpec;
import net.mehvahdjukaar.moonlight.api.platform.configs.ConfigType;
import net.mehvahdjukaar.moonlight.api.platform.network.ChannelHandler;
import net.mehvahdjukaar.moonlight.api.platform.network.Message;
import net.mehvahdjukaar.moonlight.core.Moonlight;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

import java.io.ByteArrayInputStream;

public class ClientBoundSyncConfigsMessage implements Message {

    public final ResourceLocation configId;
    public final byte[] configData;

    public ClientBoundSyncConfigsMessage(FriendlyByteBuf buf) {
        this.configId = buf.readResourceLocation();
        this.configData = buf.readByteArray();
    }

    public ClientBoundSyncConfigsMessage(final byte[] configFileData, final ResourceLocation configId) {
        this.configId = configId;
        this.configData = configFileData;
    }

    @Override
    public void writeToBuffer(FriendlyByteBuf buf) {
        buf.writeResourceLocation(this.configId);
        buf.writeByteArray(this.configData);
    }

    @Override
    public void handle(ChannelHandler.Context context) {
        var config = ConfigSpec.getConfigSpec(this.configId);
        if (config != null) {
            try(var stream =  new ByteArrayInputStream(this.configData)) {
                config.loadFromBytes(stream);
                Moonlight.LOGGER.info("Synced {} configs", config.getFileName());
            }catch (Exception ignored){}
        } else {
            Moonlight.LOGGER.error("Failed to find config file with id {}", this.configId);
        }
    }


}

package net.mehvahdjukaar.moonlight.core.network;

import net.mehvahdjukaar.moonlight.api.platform.configs.ModConfigHolder;
import net.mehvahdjukaar.moonlight.api.platform.network.Message;
import net.mehvahdjukaar.moonlight.core.Moonlight;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

import java.io.ByteArrayInputStream;

//bidirectional
public class SyncConfigsMessage implements Message {

    public static final TypeAndCodec<RegistryFriendlyByteBuf, SyncConfigsMessage> TYPE = Message.makeType(
            Moonlight.res("bidi_sync_configs"), SyncConfigsMessage::new);

    public final ResourceLocation configId;
    public final byte[] configData;

    public SyncConfigsMessage(RegistryFriendlyByteBuf buf) {
        this.configId = buf.readResourceLocation();
        this.configData = buf.readByteArray();
    }

    public SyncConfigsMessage(final byte[] configFileData, final ResourceLocation configId) {
        this.configId = configId;
        this.configData = configFileData;
    }

    @Override
    public void write(RegistryFriendlyByteBuf buf) {
        buf.writeResourceLocation(this.configId);
        buf.writeByteArray(this.configData);
    }

    @Override
    public void handle(Context context) {
        if (context.getDirection() == NetworkDir.SERVER_BOUND) {
            if (context.getPlayer() instanceof ServerPlayer sp && !sp.hasPermissions(3)) {
                Moonlight.LOGGER.warn("Player {} tried to sync their configs without permission", sp.getName().getString());
                return;
            }
        }
        ModConfigHolder config = ModConfigHolder.getConfigSpec(this.configId);
        if (config != null) {
            try (var stream = new ByteArrayInputStream(this.configData)) {
                config.loadFromBytes(stream, context.getDirection() == NetworkDir.CLIENT_BOUND);
                Moonlight.LOGGER.info("Synced {} configs", config.getFileName());
            } catch (Exception ignored) {
            }
        } else {
            Moonlight.LOGGER.error("Failed to find config file with id {}", this.configId);
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE.type();
    }
}

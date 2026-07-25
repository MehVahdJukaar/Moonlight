package net.mehvahdjukaar.moonlight.core.network;

import net.mehvahdjukaar.moonlight.api.platform.PlatHelper;
import net.mehvahdjukaar.moonlight.api.platform.network.Message;
import net.mehvahdjukaar.moonlight.core.Moonlight;
import net.mehvahdjukaar.moonlight.core.client.config.ModsTilesScreen;
import net.minecraft.ChatFormatting;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public class ClientBoundOpenConfigScreenMessage implements Message {

    public static final TypeAndCodec<RegistryFriendlyByteBuf, ClientBoundOpenConfigScreenMessage> TYPE = Message.makeType(
            Moonlight.res("s2c_open_config_screen"), ClientBoundOpenConfigScreenMessage::new);

    private final String modId;

    public ClientBoundOpenConfigScreenMessage(RegistryFriendlyByteBuf buffer) {
        this.modId = buffer.readUtf();
    }

    public ClientBoundOpenConfigScreenMessage(String modId) {
        this.modId = modId;
    }

    @Override
    public void write(RegistryFriendlyByteBuf buffer) {
        buffer.writeUtf(this.modId);
    }

    @Override
    public void handle(Context context) {
        if (!PlatHelper.getPhysicalSide().isClient()) return;
        boolean opened = ModsTilesScreen.openModScreenOrModsScreen(this.modId);
        if (!opened) {
            // only this side can tell: the command suggests every installed mod, not every mod has a screen
            context.getPlayer().sendSystemMessage(
                    Component.translatable("commands.moonlight.config.no_config", this.modId).withStyle(ChatFormatting.RED));
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE.type();
    }
}

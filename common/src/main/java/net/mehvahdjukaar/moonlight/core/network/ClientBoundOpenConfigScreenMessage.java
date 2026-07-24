package net.mehvahdjukaar.moonlight.core.network;

import net.mehvahdjukaar.moonlight.api.platform.network.Message;
import net.mehvahdjukaar.moonlight.core.Moonlight;
import net.mehvahdjukaar.moonlight.core.client.config.ModsTilesScreen;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
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
        Screen screen = this.modId.isEmpty()
                ? new ModsTilesScreen(null, null)
                : ModsTilesScreen.configScreenFor(this.modId, null, null);
        if (screen == null) {
            // only this side can tell: the command suggests every installed mod, not every mod has a screen
            context.getPlayer().sendSystemMessage(
                    Component.translatable("commands.moonlight.config.no_config", this.modId).withStyle(ChatFormatting.RED));
            return;
        }
        // tell() and not execute(): the packet is handled on the client thread, where execute() runs inline, and
        // ChatScreen closes itself right after the command is sent, which would wipe the screen we just set
        Minecraft mc = Minecraft.getInstance();
        mc.tell(() -> mc.setScreen(screen));
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE.type();
    }
}

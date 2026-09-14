package net.mehvahdjukaar.moonlight.api.platform.network.platform;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public class NetworkHelperImplClient {

    public static boolean serverHasChannel(CustomPacketPayload.Type<?> type) {
        // NeoForge negotiates both directions during configuration, so an optional channel only ends up in the
        // connection's setup when both sides registered it.
        ClientPacketListener connection = Minecraft.getInstance().getConnection();
        return connection != null && connection.hasChannel(type);
    }
}

package net.mehvahdjukaar.moonlight.api.platform.network.platform;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public class NetworkHelperImplClient {

    public static boolean serverHasChannel(CustomPacketPayload.Type<?> type) {
        // an optional channel is only set up when both sides registered it
        ClientPacketListener connection = Minecraft.getInstance().getConnection();
        return connection != null && connection.hasChannel(type);
    }
}

package net.mehvahdjukaar.moonlight.platform.network;

import net.minecraft.network.FriendlyByteBuf;

public interface Message {

    void writeToBuffer(FriendlyByteBuf buf);

    void handle(ChannelHandler.Context context);
}
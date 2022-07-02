package net.mehvahdjukaar.moonlight.platform.network;

import net.minecraft.network.FriendlyByteBuf;

public abstract interface Message {

    public abstract void writeToBuffer(FriendlyByteBuf buf);

    public abstract void handle(ChannelHandler.Context context);
}
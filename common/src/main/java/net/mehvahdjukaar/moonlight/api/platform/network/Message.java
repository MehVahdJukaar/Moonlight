package net.mehvahdjukaar.moonlight.api.platform.network;

import net.minecraft.network.FriendlyByteBuf;

public interface Message {

    void write(FriendlyByteBuf buf);

    void handle(NetworkHelper.Context context);
}
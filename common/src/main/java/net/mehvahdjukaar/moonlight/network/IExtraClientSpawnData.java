package net.mehvahdjukaar.moonlight.network;

import net.minecraft.network.FriendlyByteBuf;

public interface IExtraClientSpawnData {

    void writeSpawnData(FriendlyByteBuf arg);

    void readSpawnData(FriendlyByteBuf arg);
}

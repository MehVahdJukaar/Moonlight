package net.mehvahdjukaar.moonlight.api.entities;

import net.minecraft.network.FriendlyByteBuf;

public interface IExtraClientSpawnData {

    void writeSpawnData(FriendlyByteBuf arg);

    void readSpawnData(FriendlyByteBuf arg);
}

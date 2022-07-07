package net.mehvahdjukaar.moonlight.api.entity;

import net.minecraft.network.FriendlyByteBuf;

public interface IExtraClientSpawnData {

    void writeSpawnData(FriendlyByteBuf arg);

    void readSpawnData(FriendlyByteBuf arg);
}

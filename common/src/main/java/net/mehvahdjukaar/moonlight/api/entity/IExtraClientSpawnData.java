package net.mehvahdjukaar.moonlight.api.entity;

import net.minecraft.network.FriendlyByteBuf;

/**
 * Equivalent of IAdditionalSpawnData. actually implements that on forge
 */
public interface IExtraClientSpawnData {

    void writeSpawnData(FriendlyByteBuf arg);

    void readSpawnData(FriendlyByteBuf arg);
}

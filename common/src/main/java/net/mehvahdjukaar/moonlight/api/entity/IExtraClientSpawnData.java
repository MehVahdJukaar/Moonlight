package net.mehvahdjukaar.moonlight.api.entity;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;

/**
 * Equivalent of IAdditionalSpawnData. actually implements that on forge
 */
public interface IExtraClientSpawnData {

    void writeSpawnData(RegistryFriendlyByteBuf arg);

    void readSpawnData(RegistryFriendlyByteBuf arg);
}

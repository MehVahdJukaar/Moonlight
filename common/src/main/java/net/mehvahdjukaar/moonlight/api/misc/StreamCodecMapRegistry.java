package net.mehvahdjukaar.moonlight.api.misc;

import com.mojang.serialization.MapCodec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;

// simple registry of stream codecs
public class StreamCodecMapRegistry<T> extends MapRegistry<StreamCodec<? super RegistryFriendlyByteBuf, ? extends T>> {
    public StreamCodecMapRegistry(String name) {
        super(name);
    }
}
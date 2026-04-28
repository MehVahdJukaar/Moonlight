package net.mehvahdjukaar.moonlight.api.misc;

import com.mojang.serialization.MapCodec;
import net.minecraft.resources.ResourceLocation;

public class CodecMapRegistry<T> extends MapRegistry<MapCodec<? extends T>> {

    public CodecMapRegistry(String name) {
        super(name);
    }

    public <B extends T> MapCodec<B> register(ResourceLocation name, MapCodec<B> value) {
        super.register(name, value);
        return value;
    }

    public <B extends T> MapCodec<B> register(String name, MapCodec<B> value) {
        return this.register(ResourceLocation.tryParse(name), value);
    }
}
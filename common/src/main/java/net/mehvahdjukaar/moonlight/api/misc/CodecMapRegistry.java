package net.mehvahdjukaar.moonlight.api.misc;

import com.mojang.serialization.Codec;
import net.minecraft.resources.ResourceLocation;

public class CodecMapRegistry<T> extends MapRegistry<Codec<? extends T>> {

    public CodecMapRegistry(String name) {
        super(name);
    }

    public <B extends T> Codec<B> register(ResourceLocation name, Codec<B> value) {
        super.register(name, value);
        return value;
    }

    public <B extends T> Codec<B> register(String name, Codec<B> value) {
        return this.register(ResourceLocation.parse(name), value);
    }
}
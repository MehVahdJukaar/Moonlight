package net.mehvahdjukaar.moonlight.api.util.codec;

import com.google.common.base.Suppliers;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import net.minecraft.network.codec.StreamCodec;

import java.util.function.Function;
import java.util.function.Supplier;


public class RecursiveStreamCodec<A,T> implements StreamCodec<A,T> {
    private final String name;
    private final Supplier<StreamCodec<A,T>> wrapped;

    public RecursiveStreamCodec(final String name, final Function<StreamCodec<A,T>, StreamCodec<A,T>> wrapped) {
        this.name = name;
        this.wrapped = Suppliers.memoize(() -> wrapped.apply(this));
    }

    @Override
    public T decode(A object) {
        return wrapped.get().decode(object);
    }

    @Override
    public void encode(A object, T object2) {
        wrapped.get().encode(object, object2);
    }

    @Override
    public String toString() {
        return "RecursiveStreamCodec[" + name + ']';
    }
}


package net.mehvahdjukaar.moonlight.api.util;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Like listOf but doesn't fail if an element fails to decode
 */
public record LenientListCodec<E>(Codec<E> elementCodec) implements Codec<List<E>> {

    public static <A> LenientListCodec<A> of(final Codec<A> elementCodec) {
        return new LenientListCodec<>(elementCodec);
    }

    @Override
    public <T> DataResult<T> encode(final List<E> input, final DynamicOps<T> ops, final T prefix) {
        final ListBuilder<T> builder = ops.listBuilder();
        for (final E element : input) {
            builder.add(elementCodec.encodeStart(ops, element));
        }
        return builder.build(prefix);
    }

    @Override
    public <T> DataResult<Pair<List<E>, T>> decode(final DynamicOps<T> ops, final T input) {
        return ops.getList(input).setLifecycle(Lifecycle.stable())
                .flatMap(listOps -> {
                    List<E> elements = new ArrayList<>();

                    listOps.accept(value -> {
                        DataResult<Pair<E, T>> elementResult = elementCodec.decode(ops, value);
                        elementResult.map(p -> elements.add(p.getFirst()));
                    });
                    return DataResult.success(Pair.of(List.copyOf(elements), input), Lifecycle.stable());
                });
    }

    @Override
    public String toString() {
        return "LenientListCodec[" + elementCodec + ']';
    }
}
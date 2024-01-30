package net.mehvahdjukaar.moonlight.core;

import com.mojang.serialization.*;
import net.minecraft.util.ExtraCodecs;

import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

// String Optional codec
public class StrOpt {

    public static <A> MapCodec<Optional<A>> of(Codec<A> elementCodec, String name) {
        return ExtraCodecs.strictOptionalField(elementCodec, name);
    }

    public static <A> MapCodec<A> of(Codec<A> elementCodec, String name, A fallback) {
        return ExtraCodecs.strictOptionalField(elementCodec, name).xmap(
                (optional) -> optional.orElse(fallback),
                (object2) -> Objects.equals(object2, fallback) ? Optional.empty() : Optional.of(object2));
    }

}

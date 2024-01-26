package net.mehvahdjukaar.moonlight.api.misc;

import com.mojang.serialization.*;
import net.minecraft.util.ExtraCodecs;

import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

// String Optional codec. Just here for legacy reasons, faster to write at least
public class StrOpt {

    public static <A> MapCodec<Optional<A>> of(Codec<A> elementCodec, String name) {
        return ExtraCodecs.strictOptionalField(elementCodec, name);
    }

    public static <A> MapCodec<A> of(Codec<A> elementCodec, String name, A fallback) {
        return ExtraCodecs.strictOptionalField(elementCodec, name, fallback);
    }
}

package net.mehvahdjukaar.moonlight.api.util.codec.neoforge;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.BaseMapCodec;
import net.neoforged.neoforge.common.LenientUnboundedMapCodec;
import org.checkerframework.checker.units.qual.C;
import org.checkerframework.checker.units.qual.K;

import java.util.Map;

public class CodecUtilsImpl {
    public static <K, V, C extends BaseMapCodec<K, V> & Codec<Map<K, V>>> C optionalMapCodec(final Codec<K> keyCodec, final Codec<V> elementCodec) {
        return (C) new LenientUnboundedMapCodec<>(keyCodec, elementCodec);
    }
}

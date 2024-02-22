package net.mehvahdjukaar.moonlight.api.util.forge;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.BaseMapCodec;
import net.minecraftforge.common.LenientUnboundedMapCodec;
import org.checkerframework.checker.units.qual.K;

import java.util.Map;

public class UtilsImpl {

    public static <K, V> BaseMapCodec<K, V> optionalMapCodec(final Codec<K> keyCodec, final Codec<V> elementCodec){
        return new LenientUnboundedMapCodec<>(keyCodec, elementCodec);
    }
}

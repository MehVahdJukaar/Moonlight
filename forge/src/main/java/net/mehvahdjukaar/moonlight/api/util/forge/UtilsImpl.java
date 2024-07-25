package net.mehvahdjukaar.moonlight.api.util.forge;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.BaseMapCodec;
import net.minecraftforge.common.LenientUnboundedMapCodec;
import net.neoforged.neoforge.fluids.FluidStack;

import java.util.Map;

public class UtilsImpl {

    public static <K, V, C extends BaseMapCodec<K, V> & Codec<Map<K, V>>> C optionalMapCodec(final Codec<K> keyCodec, final Codec<V> elementCodec) {
        return (C) new LenientUnboundedMapCodec<>(keyCodec, elementCodec);
    }
}

package net.mehvahdjukaar.moonlight.api.trades;

import com.mojang.serialization.Codec;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.trading.MerchantOffer;
import org.jetbrains.annotations.Nullable;

public class NoOpListing implements ModItemListing {

    public static final Codec<NoOpListing> CODEC = Codec.unit(new NoOpListing());

    @Override
    public Codec<? extends ModItemListing> getCodec() {
        return CODEC;
    }

    @Nullable
    @Override
    public MerchantOffer getOffer(Entity trader, RandomSource random) {
        return null;
    }
}

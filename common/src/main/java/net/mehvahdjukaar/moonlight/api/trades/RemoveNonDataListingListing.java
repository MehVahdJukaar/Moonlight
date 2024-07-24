package net.mehvahdjukaar.moonlight.api.trades;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.trading.MerchantOffer;
import org.jetbrains.annotations.Nullable;

public record RemoveNonDataListingListing(int level) implements ModItemListing{
    public static final MapCodec<RemoveNonDataListingListing> CODEC = RecordCodecBuilder.mapCodec((i) -> i.group(
            Codec.intRange(1, 5).optionalFieldOf( "level", 1).forGetter(RemoveNonDataListingListing::level)
    ).apply(i, RemoveNonDataListingListing::new));

    @Override
    public MapCodec<? extends ModItemListing> getCodec() {
        return CODEC;
    }

    @Override
    public boolean isValid() {
        return false;
    }

    @Override
    public int getLevel() {
        return level;
    }

    @Nullable
    @Override
    public MerchantOffer getOffer(Entity trader, RandomSource random) {
        return null;
    }


}

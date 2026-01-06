package net.mehvahdjukaar.moonlight.api.trades;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.mehvahdjukaar.moonlight.api.misc.StrOpt;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.MerchantOffer;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public record RemoveNonDataListingListing(Optional<Integer> level) implements ModItemListing{
    public static final Codec<RemoveNonDataListingListing> CODEC = RecordCodecBuilder.create((i) -> i.group(
            Codec.intRange(1, 5).optionalFieldOf( "level").forGetter(RemoveNonDataListingListing::level)

    ).apply(i, RemoveNonDataListingListing::new));

    @Override
    public Codec<? extends ModItemListing> getCodec() {
        return CODEC;
    }

    @Nullable
    @Override
    public MerchantOffer getOffer(Entity trader, RandomSource random) {
        return null;
    }


    public boolean matches(int level, VillagerTrades.ItemListing listing){
        return this.level.isEmpty() || this.level.get() == level;
    }


}

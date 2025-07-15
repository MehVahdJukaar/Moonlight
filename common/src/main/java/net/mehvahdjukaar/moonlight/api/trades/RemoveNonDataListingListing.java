package net.mehvahdjukaar.moonlight.api.trades;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.ItemCost;
import net.minecraft.world.item.trading.MerchantOffer;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public record RemoveNonDataListingListing(Optional<Integer> level, Optional<ItemStack> forSale, Optional<ItemCost> price, Optional<ItemCost> price2) implements ModItemListing{
    public static final MapCodec<RemoveNonDataListingListing> CODEC = RecordCodecBuilder.mapCodec((i) -> i.group(
            Codec.intRange(1, 5).optionalFieldOf( "level").forGetter(RemoveNonDataListingListing::level),
            ItemStack.CODEC.optionalFieldOf("offer").forGetter(RemoveNonDataListingListing::forSale), //this dont work
            ItemCost.CODEC.optionalFieldOf("price").forGetter(RemoveNonDataListingListing::price),
            ItemCost.CODEC.optionalFieldOf("price_secondary").forGetter(RemoveNonDataListingListing::price2)

    ).apply(i, RemoveNonDataListingListing::new));

    @Override
    public MapCodec<? extends ModItemListing> getCodec() {
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

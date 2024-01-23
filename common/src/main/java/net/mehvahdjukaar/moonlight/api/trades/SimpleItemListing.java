package net.mehvahdjukaar.moonlight.api.trades;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.mehvahdjukaar.moonlight.api.misc.StrOpt;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.MerchantOffer;

import java.util.Optional;

/**
 * Simple item listing implementation
 */
public record SimpleItemListing(ItemStack price, ItemStack price2, ItemStack offer,
              int maxTrades, int xp, float priceMult,
              int level) implements ModItemListing {

    public static SimpleItemListing createDefault(ItemStack price, ItemStack price2, ItemStack offer,
                                       int maxTrades, Optional<Integer> xp, float priceMult,
                                       int level) {
        boolean buying = offer.is(Items.EMERALD);
        return new SimpleItemListing(price, price2, offer, maxTrades, xp.orElse(ModItemListing.defaultXp(buying, level)), priceMult, level);
    }

    public SimpleItemListing(ItemStack price, ItemStack forSale, int maxTrades, int xp, float priceMult) {
        this(price, ItemStack.EMPTY, forSale, maxTrades, xp, priceMult, 1);
    }

    public SimpleItemListing(int emeralds, ItemStack forSale, int maxTrades, int xp, float mult) {
        this(new ItemStack(Items.EMERALD, emeralds), forSale, maxTrades, xp, mult);
    }

    public SimpleItemListing(int emeralds, ItemStack forSale, int maxTrades, int xp) {
        this(new ItemStack(Items.EMERALD, emeralds), forSale, maxTrades, xp, 0.05f);
    }

    public static final Codec<SimpleItemListing> CODEC = RecordCodecBuilder.create((instance) -> instance.group(
            ItemStack.CODEC.fieldOf("price").forGetter(SimpleItemListing::price),
            StrOpt.of(ItemStack.CODEC, "price_secondary", ItemStack.EMPTY).forGetter(SimpleItemListing::price2),
            ItemStack.CODEC.fieldOf("offer").forGetter(SimpleItemListing::offer),
            StrOpt.of(ExtraCodecs.POSITIVE_INT, "max_trades", 16).forGetter(SimpleItemListing::maxTrades),
            StrOpt.of(ExtraCodecs.POSITIVE_INT, "xp").forGetter(s -> Optional.of(s.xp)),
            StrOpt.of(ExtraCodecs.POSITIVE_FLOAT, "price_multiplier", 0.05f).forGetter(SimpleItemListing::priceMult),
            StrOpt.of(Codec.intRange(1, 5), "level", 1).forGetter(SimpleItemListing::level)
    ).apply(instance, SimpleItemListing::createDefault));

    @Override
    public MerchantOffer getOffer(Entity entity, RandomSource randomSource) {
        return new MerchantOffer(price, price2, offer, maxTrades, xp, priceMult);
    }

    @Override
    public int getLevel() {
        return level;
    }

    @Override
    public Codec<SimpleItemListing> getCodec() {
        return CODEC;
    }
}

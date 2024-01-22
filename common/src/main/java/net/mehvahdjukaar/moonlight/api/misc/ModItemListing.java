package net.mehvahdjukaar.moonlight.api.misc;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.MerchantOffer;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

/**
 * Simple item listing implementation
 */
public record ModItemListing(ItemStack price, ItemStack price2, ItemStack offer,
                             int maxTrades, int xp, float priceMult,
                             int level) implements VillagerTrades.ItemListing {

    public static final BiMap<ResourceLocation, VillagerTrades.ItemListing> SPECIAL_TRADES = HashBiMap.create();

    public static final Codec<VillagerTrades.ItemListing> REFERENCE = ResourceLocation.CODEC
            .flatXmap((string) -> Optional.ofNullable(SPECIAL_TRADES.get(string))
                            .map(DataResult::success)
                            .orElseGet(() -> DataResult.error(() -> "Unknown element name:" + string)),
                    (object) -> Optional.ofNullable(SPECIAL_TRADES.inverse().get(object))
                            .map(DataResult::success).orElseGet(() ->
                                    DataResult.error(() -> "Element with unknown name: " + object)));

    public static final Codec<ModItemListing> DIRECT = RecordCodecBuilder.create((instance) -> instance.group(
            ItemStack.CODEC.fieldOf("price").forGetter(ModItemListing::price),
            StrOpt.of(ItemStack.CODEC, "price_secondary", ItemStack.EMPTY).forGetter(ModItemListing::price2),
            ItemStack.CODEC.fieldOf("offer").forGetter(ModItemListing::offer),
            StrOpt.of(ExtraCodecs.POSITIVE_INT, "max_trades", 16).forGetter(ModItemListing::maxTrades),
            StrOpt.of(ExtraCodecs.POSITIVE_INT, "xp", 2).forGetter(ModItemListing::xp),
            StrOpt.of(ExtraCodecs.POSITIVE_FLOAT, "price_multiplier", 0.05f).forGetter(ModItemListing::priceMult),
            StrOpt.of(Codec.intRange(1, 5), "level", 1).forGetter(ModItemListing::level)
    ).apply(instance, ModItemListing::new));

    public static final Codec<VillagerTrades.ItemListing> CODEC =
            Codec.either(DIRECT, REFERENCE.fieldOf("special").codec()).xmap(either -> either.map(s -> s, c -> c), itemListing ->
                    itemListing instanceof ModItemListing m ? Either.left(m) : Either.right(itemListing)
            );

    // Call on mod setup
    public static void registerSpecial(ResourceLocation id, VillagerTrades.ItemListing trade) {
        SPECIAL_TRADES.put(id, trade);
    }

    public static VillagerTrades.ItemListing getSpecial(ResourceLocation id) {
        return SPECIAL_TRADES.get(id);
    }

    public ModItemListing(ItemStack price, ItemStack forSale, int maxTrades, int xp, float priceMult) {
        this(price, ItemStack.EMPTY, forSale, maxTrades, xp, priceMult, 1);
    }

    public ModItemListing(int emeralds, ItemStack forSale, int maxTrades, int xp, float mult) {
        this(new ItemStack(Items.EMERALD, emeralds), forSale, maxTrades, xp, mult);
    }

    public ModItemListing(int emeralds, ItemStack forSale, int maxTrades, int xp) {
        this(new ItemStack(Items.EMERALD, emeralds), forSale, maxTrades, xp, 0.05f);
    }


    @Nullable
    @Override
    public MerchantOffer getOffer(Entity entity, RandomSource randomSource) {
        return new MerchantOffer(price, price2, offer, maxTrades, xp, priceMult);
    }
}
package net.mehvahdjukaar.moonlight.api.trades;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.mehvahdjukaar.moonlight.core.Moonlight;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.ItemCost;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Simple item listing implementation
 */
public record SimpleItemListing(ItemCost price, Optional<ItemCost> price2, ItemStack offer,
                                int maxTrades, int xp, float priceMult,
                                int level, LootItemFunction func) implements ModItemListing {

    public static SimpleItemListing createDefault(ItemCost price, Optional<ItemCost> price2, ItemStack offer,
                                                  int maxTrades, Optional<Integer> xp, float priceMult,
                                                  int level) {
        boolean buying = offer.is(Items.EMERALD);
        return new SimpleItemListing(price, price2, offer, maxTrades, xp.orElse(ModItemListing.defaultXp(buying, level)),
                priceMult, level, null);
    }

    public SimpleItemListing(ItemCost price, ItemStack forSale, int maxTrades, int xp, float priceMult) {
        this(price, Optional.empty(), forSale, maxTrades, xp, priceMult, 1, null);
    }

    public SimpleItemListing(int emeralds, ItemStack forSale, int maxTrades, int xp, float mult) {
        this(new ItemCost(Items.EMERALD, emeralds), forSale, maxTrades, xp, mult);
    }

    public SimpleItemListing(int emeralds, ItemStack forSale, int maxTrades, int xp) {
        this(new ItemCost(Items.EMERALD, emeralds), forSale, maxTrades, xp, 0.05f);
    }

    /*
   Codec<LootItemFunction> TYPED_CODEC = BuiltInRegistries.LOOT_FUNCTION_TYPE.byNameCodec()
           .dispatch("function",LootItemFunction::getType, LootItemFunctionType::codec);
*/
    //TODO
    public static final MapCodec<SimpleItemListing> CODEC = RecordCodecBuilder.mapCodec((instance) -> instance.group(
            ItemCost.CODEC.fieldOf("price").forGetter(SimpleItemListing::price),
            ItemCost.CODEC.optionalFieldOf("price_secondary").forGetter(SimpleItemListing::price2),
            ItemStack.CODEC.fieldOf("offer").forGetter(SimpleItemListing::offer),
            ExtraCodecs.POSITIVE_INT.optionalFieldOf("max_trades", 16).forGetter(SimpleItemListing::maxTrades),
            ExtraCodecs.POSITIVE_INT.optionalFieldOf("xp").forGetter(s -> Optional.of(s.xp)),
            ExtraCodecs.POSITIVE_FLOAT.optionalFieldOf("price_multiplier", 0.05f).forGetter(SimpleItemListing::priceMult),
            Codec.intRange(1, 5).optionalFieldOf("level", 1).forGetter(SimpleItemListing::level)
            //StrOpt.of(, "loot_function").forGetter(s->Optional.ofNullable(s.func))
    ).apply(instance, SimpleItemListing::createDefault));

    @Override
    public MerchantOffer getOffer(Entity entity, RandomSource randomSource) {
        AtomicReference<ItemStack> stack = new AtomicReference<>();
        stack.set(offer);
        if (func != null && entity.level() instanceof ServerLevel serverLevel) {
            LootParams lootParams = new LootParams.Builder(serverLevel)
                    .withParameter(LootContextParams.ORIGIN, entity.position())
                    .withParameter(LootContextParams.THIS_ENTITY, entity)
                    .create(LootContextParamSets.GIFT);

            LootContext context = new LootContext.Builder(lootParams)
                    .create(Optional.of(Moonlight.res("trading_sequence")));
            LootItemFunction.decorate(func, stack::set, context).accept(offer.copy());
        }
        return new MerchantOffer(price, price2, stack.get(), maxTrades, xp, priceMult);
    }

    @Override
    public int getLevel() {
        return level;
    }

    @Override
    public MapCodec<SimpleItemListing> getCodec() {
        return CODEC;
    }
}

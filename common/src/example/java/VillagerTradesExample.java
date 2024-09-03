import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.mehvahdjukaar.moonlight.api.trades.ItemListingManager;
import net.mehvahdjukaar.moonlight.api.trades.ModItemListing;
import net.mehvahdjukaar.moonlight.core.Moonlight;
import net.minecraft.network.chat.Component;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.MerchantOffer;

import java.util.Optional;

public class VillagerTradesExample {

    // here we can add a custom trade type. Normal trade types are already added by Moonlight
    // You can add them via datapack as normal. Check the wiki for more info
    // Once registered you'll be able to add this trade type via datapack

    // To remove a trade you can use the "no_op" type
    public static void init() {
        ItemListingManager.registerSerializer(Moonlight.res("example_custom_trade_type"), CustomTradeType.CODEC);
    }

    public record CustomTradeType(ItemStack emeralds, ItemStack priceSecondary, int rockets,
                                  int maxTrades, int xp, float priceMult, int level) implements ModItemListing {

        // Codec used to serialize your custom trade type. Note that again for most application you will do fine just using default builtin type ("simple" type)
        public static final MapCodec<CustomTradeType> CODEC = RecordCodecBuilder.mapCodec((instance) -> instance.group(
                ItemStack.CODEC.fieldOf("price").forGetter(CustomTradeType::emeralds),
                ItemStack.CODEC.optionalFieldOf( "price_secondary", ItemStack.EMPTY).forGetter(CustomTradeType::priceSecondary),
                Codec.INT.fieldOf("amount").forGetter(CustomTradeType::rockets),
                ExtraCodecs.POSITIVE_INT.optionalFieldOf( "max_trades", 16).forGetter(CustomTradeType::maxTrades),
                ExtraCodecs.POSITIVE_INT.optionalFieldOf( "xp").forGetter(s -> Optional.of(s.xp)),
                ExtraCodecs.POSITIVE_FLOAT.optionalFieldOf( "price_multiplier", 0.05f).forGetter(CustomTradeType::priceMult),
                Codec.intRange(1, 5).optionalFieldOf( "level", 1).forGetter(CustomTradeType::level)
        ).apply(instance, CustomTradeType::create));

        public static CustomTradeType create(ItemStack price, ItemStack price2, int rockets,
                                             int maxTrades, Optional<Integer> xp, float priceMult,
                                             int level) {
            return new CustomTradeType(price, price2, rockets, maxTrades, xp.orElse(ModItemListing.defaultXp(false, level)),
                    priceMult, level);
        }

        @Override
        public MerchantOffer getOffer(Entity entity, RandomSource random) {

            ItemStack itemstack = new ItemStack(Items.POTATO, rockets);
            itemstack.setHoverName(Component.literal("Potater"));
            return new MerchantOffer(emeralds, priceSecondary, itemstack, maxTrades, ModItemListing.defaultXp(true, level),
                    priceMult);
        }

        @Override
        public MapCodec<? extends ModItemListing> getCodec() {
            return CODEC;
        }

        @Override
        public int getLevel() {
            return level;
        }
    }

}

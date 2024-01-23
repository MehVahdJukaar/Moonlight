package net.mehvahdjukaar.moonlight.api.misc;

import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.MerchantOffer;
import org.jetbrains.annotations.Nullable;

@Deprecated(forRemoval = true)
public class ModItemListing implements VillagerTrades.ItemListing {

    protected final ItemStack price;
    protected final ItemStack price2;
    protected final ItemStack forSale;
    protected final int maxTrades;
    protected final int xp;
    protected final float priceMult;

    public ModItemListing(ItemStack price, ItemStack price2, ItemStack forSale, int maxTrades, int xp, float priceMult) {
        this.price = price;
        this.price2 = price2;
        this.forSale = forSale;
        this.maxTrades = maxTrades;
        this.xp = xp;
        this.priceMult = priceMult;
    }

    public ModItemListing(ItemStack price, ItemStack forSale, int maxTrades, int xp, float priceMult) {
        this(price, ItemStack.EMPTY, forSale, maxTrades, xp, priceMult);
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
        return new MerchantOffer(price, price2, forSale, maxTrades, xp, priceMult);
    }
}
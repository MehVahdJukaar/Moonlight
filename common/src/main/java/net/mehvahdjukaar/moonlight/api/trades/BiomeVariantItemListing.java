package net.mehvahdjukaar.moonlight.api.trades;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.VillagerType;
import net.minecraft.world.item.trading.MerchantOffer;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public record BiomeVariantItemListing(Map<VillagerType, ModItemListing> listingMap,
                                      ModItemListing defaultListing) implements ModItemListing {

    public static final MapCodec<BiomeVariantItemListing> CODEC = RecordCodecBuilder.<BiomeVariantItemListing>mapCodec(instance -> instance.group(
                    Codec.unboundedMap(
                            BuiltInRegistries.VILLAGER_TYPE.byNameCodec(),
                            ModItemListing.CODEC
                    ).fieldOf("trades_per_type").forGetter(BiomeVariantItemListing::listingMap),
                    ModItemListing.CODEC.fieldOf("default").forGetter(BiomeVariantItemListing::defaultListing)
            ).apply(instance, BiomeVariantItemListing::new))
            .validate(
                    listing -> {
                        //check level are the same
                        int originalLevel = listing.defaultListing.getLevel();
                        for (var entry : listing.listingMap.entrySet()) {
                            if (entry.getValue().getLevel() != originalLevel) {
                                return DataResult.error(() -> "All listings must have the same level");
                            }
                        }
                        return DataResult.success(listing);
                    }
            );


    @Override
    public MapCodec<? extends ModItemListing> getCodec() {
        return CODEC;
    }

    @Override
    public @Nullable MerchantOffer getOffer(Entity trader, RandomSource random) {
        if (trader instanceof Villager villager) {
            VillagerType villagerType = villager.getVillagerData().getType();
            ModItemListing listing = this.listingMap.get(villagerType);
            if (listing != null) {
                return listing.getOffer(trader, random);
            }
        }
        return this.defaultListing.getOffer(trader, random);
    }

    @Override
    public int getLevel() {
        return defaultListing.getLevel();
    }
}

package net.mehvahdjukaar.moonlight.api.trades;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.npc.VillagerTrades;

import java.util.Optional;


public interface ModItemListing extends VillagerTrades.ItemListing {

    Codec<ModItemListing> CODEC = ResourceLocation.CODEC
            .flatXmap((string) -> Optional.ofNullable(ItemListingRegistry.getSerializer(string))
                            .map(DataResult::success)
                            .orElseGet(() -> DataResult.error(() -> "Unknown element name:" + string)),
                    (object) -> Optional.ofNullable(ItemListingRegistry.getSerializerKey(object))
                            .map(DataResult::success).orElseGet(() ->
                                    DataResult.error(() -> "Element with unknown name: " + object)))
            .dispatch("type", o -> (Codec<ModItemListing>) o.getCodec(), o -> o);


    default int getLevel() {
        return 1;
    }

    Codec<? extends ModItemListing> getCodec();

    static int defaultXp(boolean buying, int villagerLevel) {
        return Math.min(1, 5 * villagerLevel - 1) * (buying ? 2 : 1);
    }

    //return false if this trade should be ignored
    default boolean isValid() {
        return true;
    }

}
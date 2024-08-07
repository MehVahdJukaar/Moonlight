package net.mehvahdjukaar.moonlight.api.trades;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.world.entity.npc.VillagerTrades;


public interface ModItemListing extends VillagerTrades.ItemListing {

    Codec<ModItemListing> CODEC = ItemListingManager.REGISTRY.dispatch(ModItemListing::getCodec);

    default int getLevel() {
        return 1;
    }

    MapCodec<? extends ModItemListing> getCodec();

    static int defaultXp(boolean buying, int villagerLevel) {
        return Math.max(1, 5 * (villagerLevel - 1)) * (buying ? 2 : 1);
    }

    //return false if this trade should be ignored
    default boolean isValid() {
        return true;
    }

}
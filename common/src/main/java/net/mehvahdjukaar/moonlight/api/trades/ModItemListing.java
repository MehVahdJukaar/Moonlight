package net.mehvahdjukaar.moonlight.api.trades;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.mehvahdjukaar.moonlight.api.MoonlightRegistry;
import net.mehvahdjukaar.moonlight.api.util.codec.CodecUtils;
import net.mehvahdjukaar.moonlight.core.Moonlight;
import net.minecraft.world.entity.npc.VillagerTrades;


public interface ModItemListing extends VillagerTrades.ItemListing {

    Codec<ModItemListing> CODEC = CodecUtils.remapNamespaceCodec(
                    MoonlightRegistry.VILLAGER_TRADES_REGISTRY, "minecraft", Moonlight.MOD_ID)
            .dispatch(
                    ModItemListing::getCodec, mapCodec -> mapCodec);

    default int getLevel() {
        return 1;
    }

    MapCodec<? extends ModItemListing> getCodec();

    static int defaultXp(boolean buying, int villagerLevel) {
        return Math.max(1, 5 * (villagerLevel - 1)) * (buying ? 2 : 1);
    }

}
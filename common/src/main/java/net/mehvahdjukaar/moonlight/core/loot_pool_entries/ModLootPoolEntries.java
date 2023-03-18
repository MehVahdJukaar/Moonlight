package net.mehvahdjukaar.moonlight.core.loot_pool_entries;

import net.mehvahdjukaar.moonlight.api.platform.RegHelper;
import net.mehvahdjukaar.moonlight.core.Moonlight;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryType;

import java.util.function.Supplier;

public class ModLootPoolEntries {

    public static void register() {
    }

    public static final Supplier<LootPoolEntryType> LAZY_ITEM = RegHelper.register(Moonlight.res("optional_item"), () ->
            new LootPoolEntryType(new OptionalItemPool.Serializer()), Registries.LOOT_POOL_ENTRY_TYPE);
    public static final Supplier<LootPoolEntryType> FILTERED_POOLS = RegHelper.register(Moonlight.res("filtered_pools"), () ->
            new LootPoolEntryType(new OptionalItemPool.Serializer()), Registries.LOOT_POOL_ENTRY_TYPE);


}

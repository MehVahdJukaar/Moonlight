package net.mehvahdjukaar.moonlight.api.trades;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.MapCodec;
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.mehvahdjukaar.candlelight.api.PlatformImpl;
import net.mehvahdjukaar.moonlight.api.MoonlightRegistry;
import net.mehvahdjukaar.moonlight.api.platform.ForgeHelper;
import net.mehvahdjukaar.moonlight.api.platform.RegHelper;
import net.mehvahdjukaar.moonlight.api.util.Utils;
import net.mehvahdjukaar.moonlight.core.Moonlight;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.RandomSource;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.item.trading.MerchantOffer;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class ItemListingManager extends SimpleJsonResourceReloadListener {

    @ApiStatus.Internal
    public static void init() {
        registerSerializer(Moonlight.res("simple"), SimpleItemListing.CODEC);
        registerSerializer(Moonlight.res("remove_all_non_data"), RemoveNonDataListingListing.CODEC);
        registerSerializer(Moonlight.res("no_op"), NoOpListing.CODEC);
        registerSerializer(Moonlight.res("villager_type_variant"), BiomeVariantItemListing.CODEC);
    }


    //careful here, we got to apply these properly as reload could  happen any time
    private static volatile TradeChanges currentChanges = TradeChanges.EMPTY;

    private final HolderLookup.Provider registryAccess;

    public ItemListingManager(HolderLookup.Provider provider) {
        super(new Gson(), "moonlight/villager_trade");
        this.registryAccess = provider;
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> jsons, ResourceManager resourceManager, ProfilerFiller profiler) {
        TradeChanges changes = new TradeChanges(new HashMap<>(), new HashMap<>(), new HashMap<>(), new HashMap<>());

        DynamicOps<JsonElement> ops = ForgeHelper.conditionalOps(JsonOps.INSTANCE, registryAccess, this);
        for (var e : jsons.entrySet()) {
            JsonElement json = e.getValue();
            ResourceLocation id = e.getKey();
            if (!id.getPath().contains("/")){
                Moonlight.LOGGER.error("Invalid villager trade id: {}. Must be in format <profession>/<trade>", id);
                continue;
            }
            ResourceLocation targetId = id.withPath(p -> p.substring(0, p.lastIndexOf('/')));
            var profession = BuiltInRegistries.VILLAGER_PROFESSION.getOptional(targetId);
            if (profession.isPresent()) {
                ModItemListing trade = parseOrThrow(json, id, ops).orElse(null);
                if (trade == null || (trade instanceof NoOpListing)) {
                    continue;
                } else if (trade instanceof RemoveNonDataListingListing rl) {
                    changes.tradesRemoved.computeIfAbsent(profession.get(), k -> new ArrayList<>()).add(rl);
                } else {
                    changes.tradesAdded.computeIfAbsent(profession.get(), k -> new ArrayList<>()).add(trade);
                }
                continue;
            }
            var entityType = BuiltInRegistries.ENTITY_TYPE.getOptional(targetId);
            if (entityType.isPresent()) {
                ModItemListing trade = parseOrThrow(json, id, ops).orElse(null);
                if (trade == null || (trade instanceof NoOpListing)) {
                } else if (trade instanceof RemoveNonDataListingListing rl) {
                    //if (entityType.get() != EntityType.WANDERING_TRADER) continue;
                    changes.specialTradesRemoved.computeIfAbsent(entityType.get(), k -> new ArrayList<>()).add(rl);
                } else {
                    changes.specialTradesAdded.computeIfAbsent(entityType.get(), k -> new ArrayList<>()).add(trade);
                }
            } else {
                Moonlight.LOGGER.warn("Unknown villager type: {}", targetId);
            }
        }

        currentChanges = changes;
        rebuildTrades();

        int added = changes.specialTradesAdded.values().stream()
                .mapToInt(List::size)
                .sum() + changes.tradesAdded.values().stream()
                .mapToInt(List::size)
                .sum();
        int removed = changes.tradesRemoved.values().stream().mapToInt(List::size).sum()
                + changes.specialTradesRemoved.values().stream().mapToInt(List::size).sum();

        if (added > 0) {
            Moonlight.LOGGER.info("Applied {} data villager trades", added);
        }
        if (removed > 0) {
            Moonlight.LOGGER.info("Applied {} data villager trade removals", removed);
        }
    }

    @PlatformImpl
    private static void rebuildTrades() {
        throw new AssertionError();
    }

    private static @NotNull Int2ObjectMap<VillagerTrades.ItemListing[]> getTradeMapForProfession(
            VillagerProfession profession) {
        return VillagerTrades.TRADES.computeIfAbsent(profession, k -> new Int2ObjectArrayMap<>());
    }

    @ApiStatus.Internal
    public static void modifyVillagerTrades(VillagerProfession profession, Int2ObjectMap<List<VillagerTrades.ItemListing>> trades) {
        TradeChanges changes = currentChanges;

        modifyTrades(trades, changes.tradesRemoved.get(profession), changes.tradesAdded.get(profession));
    }

    @ApiStatus.Internal
    public static void modifyWandererTrades(List<VillagerTrades.ItemListing> generic, List<VillagerTrades.ItemListing> rare) {
        TradeChanges changes = currentChanges;
        Int2ObjectMap<List<VillagerTrades.ItemListing>> trades = new Int2ObjectArrayMap<>();
        trades.put(1, generic);
        trades.put(2, rare);
        modifyTrades(trades, changes.specialTradesRemoved.get(EntityType.WANDERING_TRADER),
                changes.specialTradesAdded.get(EntityType.WANDERING_TRADER));

        //vanilla picks a random rare one without checking if there are any
        if (rare.isEmpty()) {
            rare.add((e, r) -> null);
        }
    }

    private static void modifyTrades(Int2ObjectMap<List<VillagerTrades.ItemListing>> trades,
                                     @Nullable List<RemoveNonDataListingListing> removals,
                                     @Nullable List<ModItemListing> additions) {
        if (removals != null) {
            for (var e : trades.int2ObjectEntrySet()) {
                int level = e.getIntKey();
                e.getValue().removeIf(listing -> {
                    for (var r : removals) {
                        if (r.matches(level, listing)) {
                            return true;
                        }
                    }
                    return false;
                });
            }
        }
        if (additions != null) {
            for (var listing : additions) {
                trades.computeIfAbsent(listing.getLevel(), k -> new ArrayList<>()).add(listing);
            }
        }
    }


    private static Optional<? extends ModItemListing> parseOrThrow(JsonElement j, ResourceLocation id, DynamicOps<JsonElement> ops) {
        return ForgeHelper.conditionalCodec(ModItemListing.CODEC)
                .parse(ops, j)
                .getOrThrow();
    }

    public static List<? extends VillagerTrades.ItemListing> getVillagerListings(VillagerProfession profession, int level) {
        VillagerTrades.ItemListing[] array = getTradeMapForProfession(profession).get(level);
        if (array == null) return List.of();
        return Arrays.stream(array).toList();
    }

    public static List<? extends VillagerTrades.ItemListing> getSpecialListings(EntityType<?> entityType, int level, HolderLookup.Provider provider) {
        if (entityType == EntityType.WANDERING_TRADER) {
            VillagerTrades.ItemListing[] array = VillagerTrades.WANDERING_TRADER_TRADES.get(level);
            if (array == null) return List.of();
            return Arrays.stream(array).toList();
        } else {
            var special = currentChanges.specialTradesAdded.get(entityType);
            if (special == null) return List.of();
            List<VillagerTrades.ItemListing> listings = new ArrayList<>();
            for (ModItemListing listing : special) {
                if (listing.getLevel() == level) {
                    listings.add(listing);
                }
            }
            return listings;
        }
    }

    @Deprecated(forRemoval = true)
    public static List<? extends VillagerTrades.ItemListing> getSpecialListings(EntityType<?> entityType, int level) {
        return getSpecialListings(entityType, level, Utils.hackyGetRegistryAccess());
    }

    /**
     * Call on mod setup. Register a new serializer for your trade
     */
    public static void registerSerializer(ResourceLocation id, MapCodec<? extends ModItemListing> trade) {
        RegHelper.register(id, () -> trade, MoonlightRegistry.VILLAGER_TRADES_REGISTRY.key());
    }

    /**
     * Registers a simple special trade
     */
    public static void registerSimple(ResourceLocation id, VillagerTrades.ItemListing instance, int level) {
        SpecialListing specialListing = new SpecialListing(instance, level);
        registerSerializer(id, specialListing.getCodec());
    }

    private static class SpecialListing implements ModItemListing {

        private final MapCodec<ModItemListing> codec = MapCodec.unit(this);
        private final VillagerTrades.ItemListing listing;
        private final int level;

        public SpecialListing(VillagerTrades.ItemListing listing, int level) {
            this.listing = listing;
            this.level = level;
        }

        @Override
        public MapCodec<? extends ModItemListing> getCodec() {
            return codec;
        }

        @Nullable
        @Override
        public MerchantOffer getOffer(Entity trader, RandomSource random) {
            return listing.getOffer(trader, random);
        }

        @Override
        public int getLevel() {
            return level;
        }
    }

    private record TradeChanges(
            Map<EntityType<?>, List<ModItemListing>> specialTradesAdded,
            Map<VillagerProfession, List<ModItemListing>> tradesAdded,
            Map<EntityType<?>, List<RemoveNonDataListingListing>> specialTradesRemoved,
            Map<VillagerProfession, List<RemoveNonDataListingListing>> tradesRemoved) {
        private static final TradeChanges EMPTY = new TradeChanges(Map.of(), Map.of(), Map.of(), Map.of());
    }

}

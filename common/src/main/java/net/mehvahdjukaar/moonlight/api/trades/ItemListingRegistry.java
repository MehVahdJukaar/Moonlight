package net.mehvahdjukaar.moonlight.api.trades;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.mehvahdjukaar.moonlight.api.misc.CodecMapRegistry;
import net.mehvahdjukaar.moonlight.api.misc.RegistryAccessJsonReloadListener;
import net.mehvahdjukaar.moonlight.api.platform.ForgeHelper;
import net.mehvahdjukaar.moonlight.api.util.Utils;
import net.mehvahdjukaar.moonlight.core.Moonlight;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.item.trading.MerchantOffer;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class ItemListingRegistry extends RegistryAccessJsonReloadListener {

    private static final ItemListingRegistry INSTANCE = new ItemListingRegistry();
    protected static final CodecMapRegistry<ModItemListing> REGISTRY = new CodecMapRegistry<>(); //no hassle

    @ApiStatus.Internal
    public static void init() {
        registerSerializer(new ResourceLocation("simple"), SimpleItemListing.CODEC);
        registerSerializer(new ResourceLocation("remove_all_non_data"), RemoveNonDataListingListing.CODEC);
        registerSerializer(new ResourceLocation("no_op"), NoOpListing.CODEC);
    }

    private final Map<EntityType<?>, Set<ModItemListing>> specialTradesAdded = new HashMap<>();
    private final Map<VillagerProfession, Set<ModItemListing>> tradesAdded = new HashMap<>();

    //removed trades
    private final Map<EntityType<?>, Int2ObjectArrayMap<Set<VillagerTrades.ItemListing>>> specialTradesRemoved = new HashMap<>();
    private final Map<VillagerProfession, Int2ObjectArrayMap<Set<VillagerTrades.ItemListing>>> tradesRemoved = new HashMap<>();


    public ItemListingRegistry() {
        super(new Gson(), "moonlight/villager_trades");
    }

    @Override
    public void parse(Map<ResourceLocation, JsonElement> jsons, RegistryAccess registryAccess) {
        //restore
        restoreVanillaState();

        List<Pair<ModItemListing, VillagerProfession>> toAdd = new ArrayList<>();
        List<Pair<ModItemListing, EntityType<?>>> toAddSpecial = new ArrayList<>();
        List<Pair<RemoveNonDataListingListing, VillagerProfession>> toRemove = new ArrayList<>();
        List<Pair<RemoveNonDataListingListing, EntityType<?>>> toRemoveSpecial = new ArrayList<>();

        DynamicOps<JsonElement> ops = ForgeHelper.addConditionOps(RegistryOps.create(JsonOps.INSTANCE, registryAccess));
        for (var e : jsons.entrySet()) {
            JsonElement json = e.getValue();
            ResourceLocation id = e.getKey();
            if (!id.getPath().contains("/")) continue;
            ResourceLocation targetId = id.withPath(p -> p.substring(0, p.lastIndexOf('/')));
            var profession = BuiltInRegistries.VILLAGER_PROFESSION.getOptional(targetId);
            if (profession.isPresent()) {
                ModItemListing trade = parseOrThrow(json, id, ops);
                if (trade == null || (trade instanceof NoOpListing)) {
                    continue;
                } else if (trade instanceof RemoveNonDataListingListing rl) {
                    toRemove.add(Pair.of(rl, profession.get()));
                } else {
                    toAdd.add(Pair.of(trade, profession.get()));
                }
                continue;
            }
            var entityType = BuiltInRegistries.ENTITY_TYPE.getOptional(targetId);
            if (entityType.isPresent()) {
                ModItemListing trade = parseOrThrow(json, id, ops);
                if (trade == null || (trade instanceof NoOpListing)) {
                    continue;
                } else if (trade instanceof RemoveNonDataListingListing rl) {
                    toRemoveSpecial.add(Pair.of(rl, entityType.get()));
                } else {
                    toAddSpecial.add(Pair.of(trade, entityType.get()));
                }
            } else {
                Moonlight.LOGGER.warn("Unknown villager type: {}", targetId);
            }
        }


        // Apply removals for profession-based trades
        for (var pair : toRemove) {
            VillagerProfession profession = pair.getSecond();
            RemoveNonDataListingListing listing = pair.getFirst();
            Int2ObjectMap<VillagerTrades.ItemListing[]> tradeMap = getTradeMapForProfession(profession);
            var removed = removeMatchingTrades(listing, tradeMap);
            if (!removed.isEmpty()) {
                tradesRemoved.computeIfAbsent(profession, k -> new Int2ObjectArrayMap<>())
                        .putAll(removed);
            }
        }

        // Apply removals for entity-based trades
        for (var pair : toRemoveSpecial) {
            EntityType<?> entity = pair.getSecond();
            if (entity == EntityType.WANDERING_TRADER) {
                RemoveNonDataListingListing listing = pair.getFirst();
                Int2ObjectMap<VillagerTrades.ItemListing[]> wanderingTraderTrades = VillagerTrades.WANDERING_TRADER_TRADES;
                var removed = removeMatchingTrades(listing, wanderingTraderTrades);
                if (!removed.isEmpty()) {
                    specialTradesRemoved.computeIfAbsent(entity, k -> new Int2ObjectArrayMap<>())
                            .putAll(removed);
                }
            }
        }


        // Apply profession-based additions
        for (var pair : toAdd) {
            ModItemListing listing = pair.getFirst();
            VillagerProfession profession = pair.getSecond();
            Int2ObjectMap<VillagerTrades.ItemListing[]> tradeMap = getTradeMapForProfession(profession);
            addTrade(tradeMap, listing, true);
            tradesAdded.computeIfAbsent(profession, k -> new HashSet<>()).add(listing);
        }

        // Apply entity-based additions
        for (var pair : toAddSpecial) {
            ModItemListing listing = pair.getFirst();
            EntityType<?> entity = pair.getSecond();
            if (entity == EntityType.WANDERING_TRADER) {
                Int2ObjectMap<VillagerTrades.ItemListing[]> wanderingTraderTrades = VillagerTrades.WANDERING_TRADER_TRADES;
                addTrade(wanderingTraderTrades, listing, true);
            }
            specialTradesAdded.computeIfAbsent(entity, k -> new HashSet<>()).add(listing);
        }

        int added = specialTradesAdded.values().stream()
                .mapToInt(Set::size)
                .sum() + tradesAdded.values().stream()
                .mapToInt(Set::size)
                .sum();
        int removed = tradesRemoved.values().stream().mapToInt(map -> map.values().stream().mapToInt(Set::size).sum()).sum()
                + specialTradesRemoved.values().stream().mapToInt(map -> map.values().stream().mapToInt(Set::size).sum()).sum();

        if (added > 0) {
            Moonlight.LOGGER.info("Applied {} data villager trades", added);
        }
        if (removed > 0) {
            Moonlight.LOGGER.info("Removed {} data villager trades", removed);
        }
    }

    private static @NotNull Int2ObjectMap<VillagerTrades.ItemListing[]> getTradeMapForProfession(
            VillagerProfession profession) {
        return VillagerTrades.TRADES.computeIfAbsent(profession, k -> new Int2ObjectArrayMap<>());
    }

    private static void addTrade(Int2ObjectMap<VillagerTrades.ItemListing[]> tradeMap, @NotNull ModItemListing listing, boolean add) {
        var level = listing.getLevel();
        // Ensure an array exists for this level, or create a new empty array if absent
        var existing = tradeMap.computeIfAbsent(level, k -> new VillagerTrades.ItemListing[0]);
        tradeMap.put(listing.getLevel(), mergeArrays(existing, add, listing));
    }

    private static VillagerTrades.ItemListing[] mergeArrays(VillagerTrades.ItemListing[] existing, boolean add,
                                                            VillagerTrades.ItemListing... toAdd) {
        var list = new ArrayList<>(List.of(existing));
        if (add) list.addAll(List.of(toAdd));
        else list.removeAll(List.of(toAdd));
        return list.toArray(VillagerTrades.ItemListing[]::new);
    }

    private Int2ObjectArrayMap<Set<VillagerTrades.ItemListing>> removeMatchingTrades(
            RemoveNonDataListingListing removal,
            Int2ObjectMap<VillagerTrades.ItemListing[]> originalTrades
    ) {
        Int2ObjectArrayMap<Set<VillagerTrades.ItemListing>> removedTrades = new Int2ObjectArrayMap<>();

        // Temporary map to hold updated trade arrays after removals
        Map<Integer, VillagerTrades.ItemListing[]> updatedTrades = new HashMap<>();

        for (var entry : originalTrades.int2ObjectEntrySet()) {
            int level = entry.getIntKey();
            VillagerTrades.ItemListing[] trades = entry.getValue();

            List<VillagerTrades.ItemListing> remaining = new ArrayList<>();
            Set<VillagerTrades.ItemListing> removedAtLevel = new HashSet<>();

            for (VillagerTrades.ItemListing trade : trades) {
                if (removal.matches(level, trade)) {
                    removedAtLevel.add(trade);
                } else {
                    remaining.add(trade);
                }
            }

            if (!removedAtLevel.isEmpty()) {
                removedTrades.put(level, removedAtLevel);
                updatedTrades.put(level, remaining.toArray(VillagerTrades.ItemListing[]::new));
            }
        }

        // Apply updates after iteration
        originalTrades.putAll(updatedTrades);

        return removedTrades;
    }

    private void restoreVanillaState() {
        // Undo added profession-based trades
        for (var entry : tradesAdded.entrySet()) {
            VillagerProfession profession = entry.getKey();
            Set<ModItemListing> listings = entry.getValue();
            Int2ObjectMap<VillagerTrades.ItemListing[]> tradeMap = getTradeMapForProfession(profession);

            for (ModItemListing listing : listings) {
                int level = listing.getLevel();
                VillagerTrades.ItemListing[] array = tradeMap.get(level);
                if (array == null) continue;
                addTrade(tradeMap, listing, false);
            }
        }

        // Undo added special/entity-based trades
        for (var entry : specialTradesAdded.entrySet()) {
            EntityType<?> entity = entry.getKey();
            Set<ModItemListing> listings = entry.getValue();

            if (entity == EntityType.WANDERING_TRADER) {
                Int2ObjectMap<VillagerTrades.ItemListing[]> tradeMap = VillagerTrades.WANDERING_TRADER_TRADES;

                for (ModItemListing listing : listings) {
                    int level = listing.getLevel();
                    VillagerTrades.ItemListing[] array = tradeMap.get(level);
                    if (array == null) continue;
                    addTrade(tradeMap, listing, false);
                }
            }
        }

        // Restore removed profession-based trades
        for (var entry : tradesRemoved.entrySet()) {
            VillagerProfession profession = entry.getKey();
            Int2ObjectMap<Set<VillagerTrades.ItemListing>> removedPerLevel = entry.getValue();
            Int2ObjectMap<VillagerTrades.ItemListing[]> tradeMap = getTradeMapForProfession(profession);

            restoreMap(tradeMap, removedPerLevel);
        }

        // Restore removed special/entity-based trades
        for (var entry : specialTradesRemoved.entrySet()) {
            EntityType<?> entity = entry.getKey();
            if (entity == EntityType.WANDERING_TRADER) {
                Int2ObjectMap<VillagerTrades.ItemListing[]> tradeMap = VillagerTrades.WANDERING_TRADER_TRADES;
                Int2ObjectMap<Set<VillagerTrades.ItemListing>> removedPerLevel = entry.getValue();

                restoreMap(tradeMap, removedPerLevel);
            }
        }

        tradesAdded.clear();
        specialTradesAdded.clear();
        tradesRemoved.clear();
        specialTradesRemoved.clear();
    }

    private void restoreMap(Int2ObjectMap<VillagerTrades.ItemListing[]> tradeMap,
                            Int2ObjectMap<Set<VillagerTrades.ItemListing>> removedPerLevel) {
        for (var levelEntry : removedPerLevel.int2ObjectEntrySet()) {
            int level = levelEntry.getIntKey();
            Set<VillagerTrades.ItemListing> removedTrades = levelEntry.getValue();
            VillagerTrades.ItemListing[] currentArray = tradeMap.get(level);
            tradeMap.put(level, mergeArrays(currentArray, true, removedTrades.toArray(VillagerTrades.ItemListing[]::new)));
        }
    }


    private static ModItemListing parseOrThrow(JsonElement j, ResourceLocation id, DynamicOps<JsonElement> ops) {
        return ModItemListing.CODEC.parse(ops, j).getOrThrow(
                false, s -> Moonlight.LOGGER.error("Failed to parse villager trade {}: {}", id, s)
        );
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
            var special = INSTANCE.specialTradesAdded.get(entityType);
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
    public synchronized static void registerSerializer(ResourceLocation id, Codec<? extends ModItemListing> trade) {
        REGISTRY.register(id, trade);
    }

    /**
     * Registers a simple special trade
     */
    public synchronized static void registerSimple(ResourceLocation id, VillagerTrades.ItemListing instance, int level) {
        SpecialListing specialListing = new SpecialListing(instance, level);
        registerSerializer(id, specialListing.getCodec());
    }

    private static class SpecialListing implements ModItemListing {

        private final Codec<ModItemListing> codec = Codec.unit(this);
        private final VillagerTrades.ItemListing listing;
        private final int level;

        public SpecialListing(VillagerTrades.ItemListing listing, int level) {
            this.listing = listing;
            this.level = level;
        }

        @Override
        public Codec<? extends ModItemListing> getCodec() {
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

}

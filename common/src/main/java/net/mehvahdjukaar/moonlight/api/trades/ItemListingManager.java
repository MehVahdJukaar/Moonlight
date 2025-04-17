package net.mehvahdjukaar.moonlight.api.trades;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.MapCodec;
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.mehvahdjukaar.moonlight.api.misc.CodecMapRegistry;
import net.mehvahdjukaar.moonlight.api.misc.MapRegistry;
import net.mehvahdjukaar.moonlight.api.misc.SidedInstance;
import net.mehvahdjukaar.moonlight.api.platform.ForgeHelper;
import net.mehvahdjukaar.moonlight.api.util.Utils;
import net.mehvahdjukaar.moonlight.core.Moonlight;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.RegistryOps;
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
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class ItemListingManager extends SimpleJsonResourceReloadListener {

    private static final SidedInstance<ItemListingManager> INSTANCE = SidedInstance.of(ItemListingManager::new);
    protected static final CodecMapRegistry<ModItemListing> LISTING_TYPES = MapRegistry.ofCodec();

    static {
        LISTING_TYPES.register(ResourceLocation.parse("simple"), SimpleItemListing.CODEC);
        LISTING_TYPES.register(ResourceLocation.parse("remove_all_non_data"), RemoveNonDataListingListing.CODEC);
        LISTING_TYPES.register(ResourceLocation.parse("no_op"), NoOpListing.CODEC);
        LISTING_TYPES.register(ResourceLocation.parse("villager_type_variant"), BiomeVariantItemListing.CODEC);
    }

    private final Map<EntityType<?>, Int2ObjectArrayMap<List<ModItemListing>>> specialCustomTrades = new HashMap<>();
    private final Map<VillagerProfession, Int2ObjectArrayMap<List<ModItemListing>>> customTrades = new HashMap<>();

    private final Map<EntityType<?>, Int2ObjectArrayMap<ModItemListing[]>> oldSpecialTrades = new HashMap<>();
    private final Map<VillagerProfession, Int2ObjectArrayMap<ModItemListing[]>> oldTrades = new HashMap<>();

    private final HolderLookup.Provider registryAccess;
    private int count = 0;

    public ItemListingManager(HolderLookup.Provider provider) {
        super(new Gson(), "moonlight/villager_trade");
        this.registryAccess = provider;

        INSTANCE.set(registryAccess, this);
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> jsons, ResourceManager resourceManager, ProfilerFiller profiler) {

        mergeProfessionAndSpecial(false);

        count = 0;
        customTrades.clear();
        specialCustomTrades.clear();

        DynamicOps<JsonElement> ops = ForgeHelper.conditionalOps(JsonOps.INSTANCE, registryAccess, this);
        for (var e : jsons.entrySet()) {
            var json = e.getValue();
            var id = e.getKey();
            if (id.getPath().contains("/")) {
                parseAndAddTrade(json, id, ops);
            }
        }

        mergeProfessionAndSpecial(true);
        if (count != 0) {
            Moonlight.LOGGER.info("Applied {} data villager trades", count);
        }
    }

    private void parseAndAddTrade(JsonElement json, ResourceLocation id, DynamicOps<JsonElement> ops) {
        var targetId = id.withPath(p -> p.substring(0, p.lastIndexOf('/')));
        var profession = BuiltInRegistries.VILLAGER_PROFESSION.getOptional(targetId);
        if (profession.isPresent()) {
            var trade = parseOrThrow(json, id, ops);
            if (trade.isEmpty() || (trade.get() instanceof NoOpListing)) {
                // no op
            } else if (trade.get() instanceof RemoveNonDataListingListing) {
                //TODO: add remove trades
            } else {
                customTrades.computeIfAbsent(profession.get(), t ->
                                new Int2ObjectArrayMap<>()).computeIfAbsent(trade.get().getLevel(), a -> new ArrayList<>())
                        .add(trade.get());
            }
            return;
        }
        var entityType = BuiltInRegistries.ENTITY_TYPE.getOptional(targetId);
        if (entityType.isPresent()) {
            var trade = parseOrThrow(json, id, ops);
            if (trade.isPresent() && !(trade.get() instanceof NoOpListing)) {
                specialCustomTrades.computeIfAbsent(entityType.get(), t ->
                                new Int2ObjectArrayMap<>()).computeIfAbsent(trade.get().getLevel(), a -> new ArrayList<>())
                        .add(trade.get());
            }

        } else {
            Moonlight.LOGGER.warn("Unknown villager type: {}", targetId);
        }
    }

    private void mergeAll(Int2ObjectMap<VillagerTrades.ItemListing[]> originalValues,
                          Int2ObjectArrayMap<List<ModItemListing>> newValues, boolean add) {
        for (var e : newValues.int2ObjectEntrySet()) {
            int level = e.getIntKey();

            VillagerTrades.ItemListing[] elements = originalValues.get(level);
            var original = new ArrayList<>(elements == null ? List.of() : List.of(elements));
            List<ModItemListing> value = e.getValue();
            if (add) {
                original.addAll(value);
                count += value.size();
            } else original.removeAll(value);
            originalValues.put(level, original.toArray(VillagerTrades.ItemListing[]::new));
        }
    }

    private void mergeProfessionAndSpecial(boolean add) {
        for (var p : customTrades.entrySet()) {
            VillagerProfession profession = p.getKey();
            Int2ObjectMap<VillagerTrades.ItemListing[]> map = VillagerTrades.TRADES.computeIfAbsent(profession, k ->
                    new Int2ObjectArrayMap<>());
            Int2ObjectArrayMap<List<ModItemListing>> value = p.getValue();
            mergeAll(map, value, add);
        }
        Int2ObjectArrayMap<List<ModItemListing>> wanderingStuff = specialCustomTrades.get(EntityType.WANDERING_TRADER);
        if (wanderingStuff != null) {
            mergeAll(VillagerTrades.WANDERING_TRADER_TRADES, wanderingStuff, add);
        }
    }

    private static Optional<ModItemListing> parseOrThrow(JsonElement j, ResourceLocation id, DynamicOps<JsonElement> ops) {
        return ForgeHelper.conditionalCodec(ModItemListing.CODEC).parse(ops, j)
                .getOrThrow();
    }

    public static List<? extends VillagerTrades.ItemListing> getVillagerListings(VillagerProfession profession, int level) {
        VillagerTrades.ItemListing[] array = VillagerTrades.TRADES.get(profession).get(level);
        if (array == null) return List.of();
        return Arrays.stream(array).toList();
    }

    public static List<? extends VillagerTrades.ItemListing> getSpecialListings(EntityType<?> entityType, int level, HolderLookup.Provider provider) {
        if (entityType == EntityType.WANDERING_TRADER) {
            VillagerTrades.ItemListing[] array = VillagerTrades.WANDERING_TRADER_TRADES.get(level);
            if (array == null) return List.of();
            return Arrays.stream(array).toList();
        } else {
            var special = INSTANCE.get(provider).specialCustomTrades.get(entityType);
            if (special == null) return List.of();
            return special.getOrDefault(level, List.of());
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
        LISTING_TYPES.register(id, trade);
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

}

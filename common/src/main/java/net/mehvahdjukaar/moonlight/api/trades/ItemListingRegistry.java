package net.mehvahdjukaar.moonlight.api.trades;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.mehvahdjukaar.moonlight.core.Moonlight;
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
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class ItemListingRegistry extends SimpleJsonResourceReloadListener {

    public static final ItemListingRegistry INSTANCE = new ItemListingRegistry();

    private final BiMap<ResourceLocation, Codec<ModItemListing>> serializers = HashBiMap.create();
    private final Map<EntityType<?>, Int2ObjectArrayMap<List<ModItemListing>>> specialCustomTrades = new HashMap<>();
    private final Map<VillagerProfession, Int2ObjectArrayMap<List<ModItemListing>>> customTrades = new HashMap<>();


    private final Map<EntityType<?>, Int2ObjectArrayMap<ModItemListing[]>> oldSpecialTrades = new HashMap<>();
    private final Map<VillagerProfession, Int2ObjectArrayMap<ModItemListing[]>> oldTrades = new HashMap<>();


    private int count = 0;

    public ItemListingRegistry() {
        super(new Gson(), "villager_trades");
        serializers.put(new ResourceLocation("simple"), (Codec<ModItemListing>) (Object) SimpleItemListing.CODEC);
        serializers.put(new ResourceLocation("remove_all_non_data"), (Codec<ModItemListing>) (Object) RemoveNonDataListingListing.CODEC);
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> jsons, ResourceManager resourceManager, ProfilerFiller profiler) {

        mergeProfessionAndSpecial(false);

        count = 0;
        customTrades.clear();
        specialCustomTrades.clear();

        for (var e : jsons.entrySet()) {
            var j = e.getValue();
            var id = e.getKey();
            try {
                if (!id.getPath().contains("/")) continue;
                var targetId = id.withPath(p -> p.substring(0, p.lastIndexOf('/')));
                var profession = BuiltInRegistries.VILLAGER_PROFESSION.getOptional(targetId);
                if (profession.isPresent()) {
                    ModItemListing trade = parseOrThrow(j, id);
                    customTrades.computeIfAbsent(profession.get(), t ->
                                    new Int2ObjectArrayMap<>()).computeIfAbsent(trade.getLevel(), a -> new ArrayList<>())
                            .add(trade);
                    continue;
                }
                var entityType = BuiltInRegistries.ENTITY_TYPE.getOptional(targetId);
                if (entityType.isPresent()) {
                    ModItemListing trade = parseOrThrow(j, id);
                    specialCustomTrades.computeIfAbsent(entityType.get(), t ->
                                    new Int2ObjectArrayMap<>()).computeIfAbsent(trade.getLevel(), a -> new ArrayList<>())
                            .add(trade);

                } else {
                    Moonlight.LOGGER.warn("Unknown villager type: {}", targetId);
                }
            } catch (Exception err) {
                Moonlight.LOGGER.error("Something went wrong while parsing custom villager trades", err);
            }
        }

        mergeProfessionAndSpecial(true);
        if (count != 0) {
            Moonlight.LOGGER.info("Applied {} data villager trades", count);
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

    private static ModItemListing parseOrThrow(JsonElement j, ResourceLocation id) {
        return ModItemListing.CODEC.decode(JsonOps.INSTANCE, j)
                .getOrThrow(false, errorMsg -> Moonlight.LOGGER.warn("Failed to parse custom trade with id {} - error: {}",
                        id, errorMsg)).getFirst();
    }

    public static List<? extends VillagerTrades.ItemListing> getVillagerListings(VillagerProfession profession, int level) {
        VillagerTrades.ItemListing[] array = VillagerTrades.TRADES.get(profession).get(level);
        if (array == null) return List.of();
        return Arrays.stream(array).toList();
    }

    public static List<? extends VillagerTrades.ItemListing> getSpecialListings(EntityType<?> entityType, int level) {
        if (entityType == EntityType.WANDERING_TRADER) {
            VillagerTrades.ItemListing[] array = VillagerTrades.WANDERING_TRADER_TRADES.get(level);
            if (array == null) return List.of();
            return Arrays.stream(array).toList();
        } else {
            var special = INSTANCE.specialCustomTrades.get(entityType);
            if (special == null) return List.of();
            return special.getOrDefault(level, List.of());
        }
    }

    /**
     * Call on mod setup. Register a new serializer for your trade
     */
    public static void registerSerializer(ResourceLocation id, Codec<? extends ModItemListing> trade) {
        INSTANCE.serializers.put(id, (Codec<ModItemListing>) trade);
    }

    /**
     * Registers a simple special trade
     */
    public static void registerSimple(ResourceLocation id, VillagerTrades.ItemListing instance, int level) {
        SpecialListing specialListing = new SpecialListing(instance, level);
        registerSerializer(id, specialListing.getCodec());
    }

    public static Codec<ModItemListing> getSerializer(ResourceLocation id) {
        return INSTANCE.serializers.get(id);
    }

    public static ResourceLocation getSerializerKey(Codec<ModItemListing> object) {
        return INSTANCE.serializers.inverse().get(object);
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

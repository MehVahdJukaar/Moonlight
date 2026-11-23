package net.mehvahdjukaar.moonlight.api.trades.platform;

import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.mehvahdjukaar.moonlight.api.trades.ItemListingManager;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.npc.VillagerTrades;

import java.util.*;

public class ItemListingManagerImpl {

    private static final List<AppliedTrades> lastApplied = new ArrayList<>();

    public static void rebuildTrades() {
        //only undo our stuff other mods might have touched these maps since
        for (var a : lastApplied) {
            undo(a);
        }
        lastApplied.clear();

        for (VillagerProfession profession : BuiltInRegistries.VILLAGER_PROFESSION) {
            //fabric api puts these same maps in experimental trades too. so they cant be swapped
            var tradeMap = VillagerTrades.TRADES.computeIfAbsent(profession, k -> new Int2ObjectArrayMap<>());
            Int2ObjectMap<List<VillagerTrades.ItemListing>> trades = toMutable(tradeMap);
            ItemListingManager.modifyVillagerTrades(profession, trades);
            apply(tradeMap, trades);
        }

        var wanderer = toMutable(VillagerTrades.WANDERING_TRADER_TRADES);
        ItemListingManager.modifyWandererTrades(wanderer.computeIfAbsent(1, k -> new ArrayList<>()),
                wanderer.computeIfAbsent(2, k -> new ArrayList<>()));
        apply(VillagerTrades.WANDERING_TRADER_TRADES, wanderer);
    }

    private static void apply(Int2ObjectMap<VillagerTrades.ItemListing[]> tradeMap, Int2ObjectMap<List<VillagerTrades.ItemListing>> trades) {
        for (var e : trades.int2ObjectEntrySet()) {
            int level = e.getIntKey();
            VillagerTrades.ItemListing[] old = tradeMap.get(level);
            List<VillagerTrades.ItemListing> before = old == null ? List.of() : List.of(old);
            List<VillagerTrades.ItemListing> after = e.getValue();

            var added = missingFrom(after, before);
            var removed = missingFrom(before, after);
            if (added.isEmpty() && removed.isEmpty()) continue;

            tradeMap.put(level, after.toArray(VillagerTrades.ItemListing[]::new));
            lastApplied.add(new AppliedTrades(tradeMap, level, added, removed));
        }
    }

    private static void undo(AppliedTrades applied) {
        VillagerTrades.ItemListing[] current = applied.tradeMap.get(applied.level);
        List<VillagerTrades.ItemListing> list = new ArrayList<>();
        if (current != null) {
            list = missingFrom(List.of(current), applied.added);
        }
        list.addAll(applied.removed);

        applied.tradeMap.put(applied.level, list.toArray(VillagerTrades.ItemListing[]::new));
    }

    //by identity listings can be records
    private static List<VillagerTrades.ItemListing> missingFrom(List<VillagerTrades.ItemListing> list, List<VillagerTrades.ItemListing> other) {
        Set<VillagerTrades.ItemListing> set = Collections.newSetFromMap(new IdentityHashMap<>());
        set.addAll(other);
        List<VillagerTrades.ItemListing> res = new ArrayList<>();
        for (var l : list) {
            if (!set.contains(l)) {
                res.add(l);
            }
        }
        return res;
    }

    private static Int2ObjectMap<List<VillagerTrades.ItemListing>> toMutable(Int2ObjectMap<VillagerTrades.ItemListing[]> map) {
        Int2ObjectMap<List<VillagerTrades.ItemListing>> mutable = new Int2ObjectArrayMap<>();
        for (var e : map.int2ObjectEntrySet()) {
            mutable.put(e.getIntKey(), new ArrayList<>(List.of(e.getValue())));
        }
        return mutable;
    }

    private record AppliedTrades(Int2ObjectMap<VillagerTrades.ItemListing[]> tradeMap, int level,
                                 List<VillagerTrades.ItemListing> added, List<VillagerTrades.ItemListing> removed) {
    }
}

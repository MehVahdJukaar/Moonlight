package net.mehvahdjukaar.moonlight.api.item.additional_placements;

import com.mojang.datafixers.util.Pair;
import net.mehvahdjukaar.moonlight.core.misc.IExtendedItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class AdditionalItemPlacementsAPI {

    static final List<Pair<Supplier<AdditionalItemPlacement>, Supplier<Item>>> PLACEABLE_ITEMS = new ArrayList<>();

    /**
     * Adds a behavior to an existing block. Ideally call after blocks are registered and before items are registered
     */
    public static void register(Supplier<AdditionalItemPlacement> placement, Supplier<Item> itemSupplier) {
        PLACEABLE_ITEMS.add(Pair.of(placement, itemSupplier));
    }

    public static void registerSimple(Supplier<Block> block, Supplier<Item> itemSupplier) {
        PLACEABLE_ITEMS.add(Pair.of(() -> new AdditionalItemPlacement(block.get()), itemSupplier));
    }

    @Nullable
    public static AdditionalItemPlacement getBehavior(Item item) {
        return ((IExtendedItem) item).moonlight$getAdditionalBehavior();
    }


    public static boolean hasBehavior(Item item) {
        return getBehavior(item) != null;
    }

}

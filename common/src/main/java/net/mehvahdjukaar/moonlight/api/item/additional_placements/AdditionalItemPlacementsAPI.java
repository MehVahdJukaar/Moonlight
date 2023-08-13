package net.mehvahdjukaar.moonlight.api.item.additional_placements;

import com.mojang.datafixers.util.Pair;
import net.mehvahdjukaar.moonlight.api.platform.PlatHelper;
import net.mehvahdjukaar.moonlight.core.misc.IExtendedItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public class AdditionalItemPlacementsAPI {

    static WeakReference<Map<Block, Item>> blockToItemsMap = new WeakReference<>(null);
    private static final List<Pair<Supplier<? extends AdditionalItemPlacement>, Supplier<? extends Item>>> QUEUE = new ArrayList<>();

    /**
     * Adds a behavior to an existing block. can be called at any time but ideally before registration. Less ideally during mod setup
     */
    public static void register(Supplier<? extends AdditionalItemPlacement> placement, Supplier<? extends Item> itemSupplier) {
        QUEUE.add(Pair.of(placement, itemSupplier));
        if (blockToItemsMap != null) {
            attemptRegistering();
        }
    }

    public static void registerSimple(Supplier<? extends Block> block, Supplier<? extends Item> itemSupplier) {
        register(() -> new AdditionalItemPlacement(block.get()), itemSupplier);
    }

    @Nullable
    public static AdditionalItemPlacement getBehavior(Item item) {
        return ((IExtendedItem) item).moonlight$getAdditionalBehavior();
    }


    public static boolean hasBehavior(Item item) {
        return getBehavior(item) != null;
    }


    //needed as all items have to be registered before we can add them to maps. ALso better to do this asap
    @ApiStatus.Internal
    public static void afterItemReg() {
        if (blockToItemsMap.get() == null) {
            if (PlatHelper.isDev()) {
                throw new AssertionError("Block to items map was null");
            }
        }
        attemptRegistering();
    }

    private static void attemptRegistering() {
        Map<Block, Item> map = blockToItemsMap.get();
        if (map != null) {
            for (var p : QUEUE) {
                AdditionalItemPlacement placement = p.getFirst().get();
                Block b = placement.getPlacedBlock();
                Item i = p.getSecond().get();
                if (i == null || b == null) continue;
                if (i != Items.AIR && b != Blocks.AIR) {
                    ((IExtendedItem) i).moonlight$addAdditionalBehavior(placement);
                    map.put(b, i);
                } else {
                    throw new AssertionError("Attempted to register an Additional behavior to invalid blocks or items: " + b + ", " + i);
                }
            }
            QUEUE.clear();
        }
    }

}

package net.mehvahdjukaar.moonlight.core.set.fabric;

import net.mehvahdjukaar.moonlight.api.set.BlockSetAPI;
import net.mehvahdjukaar.moonlight.api.set.BlockType;
import net.mehvahdjukaar.moonlight.core.set.BlockSetInternal;
import net.minecraft.core.Registry;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

import java.util.*;

public class BlockSetInternalImpl {

    public static boolean hasFilledBlockSets = false;

    public static boolean hasFilledBlockSets() {
        return hasFilledBlockSets;
    }

    public static final Map<Class<? extends BlockType>, Queue<BlockSetAPI.BlockTypeRegistryCallback<Block, ?>>> BLOCK_QUEUE = new LinkedHashMap<>();
    public static final Map<Class<? extends BlockType>, Queue<BlockSetAPI.BlockTypeRegistryCallback<Item, ?>>> ITEM_QUEUE = new LinkedHashMap<>();

    public static <T extends BlockType> void addDynamicBlockRegistration(
            BlockSetAPI.BlockTypeRegistryCallback<Block, T> registrationFunction, Class<T> blockType) {
        BLOCK_QUEUE.computeIfAbsent(blockType, b -> new ArrayDeque<>()).add(registrationFunction);
    }

    public static <T extends BlockType> void addDynamicItemRegistration(
            BlockSetAPI.BlockTypeRegistryCallback<Item, T> registrationFunction, Class<T> blockType) {
        ITEM_QUEUE.computeIfAbsent(blockType, b -> new ArrayDeque<>()).add(registrationFunction);
    }

    public static void registerEntries() {
        BlockSetInternal.initializeBlockSets();
        for (var e : BLOCK_QUEUE.entrySet()) {

            registerQueue(e.getKey(), e.getValue(), Registry.BLOCK);
        }
        for (var e : ITEM_QUEUE.entrySet()) {
            registerQueue(e.getKey(), e.getValue(), Registry.ITEM);
        }
    }

    private static <R, T extends BlockType> void registerQueue(Class<T> type, Queue<BlockSetAPI.BlockTypeRegistryCallback<R, ?>> callback,
                                                               Registry<R> registry) {

        callback.forEach(a -> a.accept((n, i) -> Registry.register(registry, n, i), (Collection) BlockSetAPI.getBlockSet(type).getValues()));
    }
}

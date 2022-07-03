package net.mehvahdjukaar.moonlight.block_set.fabric;

import net.mehvahdjukaar.moonlight.block_set.BlockSetManager;
import net.mehvahdjukaar.moonlight.block_set.BlockType;
import net.minecraft.core.Registry;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

import java.util.*;

public class BlockSetManagerImpl {

    public static boolean hasFilledBlockSets = false;

    public static boolean hasFilledBlockSets() {
        return hasFilledBlockSets;
    }

    public static final Map<Class<? extends BlockType>, Queue<BlockSetManager.BlockTypeRegistryCallback<Block, ?>>> BLOCK_QUEUE = new HashMap<>();
    public static final Map<Class<? extends BlockType>, Queue<BlockSetManager.BlockTypeRegistryCallback<Item, ?>>> ITEM_QUEUE = new HashMap<>();

    public static <T extends BlockType> void addDynamicBlockRegistration(
            BlockSetManager.BlockTypeRegistryCallback<Block, T> registrationFunction, Class<T> blockType) {
        BLOCK_QUEUE.computeIfAbsent(blockType, b -> new ArrayDeque<>()).add(registrationFunction);
    }

    public static <T extends BlockType> void addDynamicItemRegistration(
            BlockSetManager.BlockTypeRegistryCallback<Item, T> registrationFunction, Class<T> blockType) {
        ITEM_QUEUE.computeIfAbsent(blockType, b -> new ArrayDeque<>()).add(registrationFunction);
    }

    public static void registerEntries() {
        for(var e : BLOCK_QUEUE.entrySet()){
            registerQueue(e.getKey(), e.getValue(), Registry.BLOCK);
        }
        for(var e : ITEM_QUEUE.entrySet()){
            registerQueue(e.getKey(), e.getValue(), Registry.ITEM);
        }

        BlockSetManager.initializeBlockSets();
    }

    private static <R,T extends BlockType> void registerQueue(Class<T> type, Queue<BlockSetManager.BlockTypeRegistryCallback<R, ?>> callback,
                                                              Registry<R> registry){
        var values = BlockSetManager.getBlockSet(type).getValues();
        callback.forEach(a->a.accept((n,i)->Registry.register(registry,n,i), (Collection) values));
    }
}

package net.mehvahdjukaar.moonlight.core.set;

import com.google.common.base.Stopwatch;
import dev.architectury.injectables.annotations.ExpectPlatform;
import net.mehvahdjukaar.moonlight.api.events.AfterLanguageLoadEvent;
import net.mehvahdjukaar.moonlight.api.events.SimpleEvent;
import net.mehvahdjukaar.moonlight.api.misc.EventCalled;
import net.mehvahdjukaar.moonlight.api.misc.Registrator;
import net.mehvahdjukaar.moonlight.api.set.BlockSetAPI;
import net.mehvahdjukaar.moonlight.api.set.BlockType;
import net.mehvahdjukaar.moonlight.api.set.BlockTypeRegistry;
import net.mehvahdjukaar.moonlight.core.Moonlight;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;

public class BlockSetInternal {

    //Frick mod loading is multithreaded, so we need to beware of concurrent access
    private static final Map<Class<? extends BlockType>, BlockTypeRegistry<?>> BLOCK_SET_CONTAINERS = new ConcurrentHashMap<>();
    private static final ConcurrentLinkedDeque<Runnable> FINDER_ADDER = new ConcurrentLinkedDeque<>();
    private static final ConcurrentLinkedDeque<Runnable> REMOVER_ADDER = new ConcurrentLinkedDeque<>();

    public static void initializeBlockSets() {
        Stopwatch sw = Stopwatch.createStarted();
        if (hasFilledBlockSets()) throw new UnsupportedOperationException("block sets have already bee initialized");
        FINDER_ADDER.forEach(Runnable::run);
        FINDER_ADDER.clear();

        BLOCK_SET_CONTAINERS.values().forEach(BlockTypeRegistry::buildAll);
        BLOCK_SET_CONTAINERS.values().forEach(BlockTypeRegistry::onBlockInit);

        //remove not wanted ones
        REMOVER_ADDER.forEach(Runnable::run);
        REMOVER_ADDER.clear();

        Moonlight.LOGGER.info("Initialized block sets in {}ms", sw.elapsed().toMillis());
    }

    @ExpectPlatform
    protected static boolean hasFilledBlockSets() {
        throw new AssertionError();
    }

    public static <T extends BlockType> void registerBlockSetDefinition(BlockTypeRegistry<T> typeRegistry) {
        if (hasFilledBlockSets()) {
            throw new UnsupportedOperationException(
                    String.format("Tried to register block set definition %s after registry events", typeRegistry));
        }
        BLOCK_SET_CONTAINERS.put(typeRegistry.getType(), typeRegistry);
    }

    public static <T extends BlockType> void addBlockTypeFinder(Class<T> type, BlockType.SetFinder<T> blockFinder) {
        if (hasFilledBlockSets()) {
            throw new UnsupportedOperationException(
                    String.format("Tried to register block %s finder %s after registry events", type, blockFinder));
        }
        FINDER_ADDER.add(() -> {
            BlockTypeRegistry<T> container = getBlockSet(type);
            container.addFinder(blockFinder);
        });
    }

    public static <T extends BlockType> void addBlockTypeRemover(Class<T> type, ResourceLocation id) {
        if (hasFilledBlockSets()) {
            throw new UnsupportedOperationException(
                    String.format("Tried to remove block type %s for type %s after registry events", id, type));
        }
        REMOVER_ADDER.add(() -> {
            BlockTypeRegistry<T> container = getBlockSet(type);
            container.addRemover(id);
        });
    }

    @SuppressWarnings("unchecked")
    public static <T extends BlockType> BlockTypeRegistry<T> getBlockSet(Class<T> type) {
        return (BlockTypeRegistry<T>) BLOCK_SET_CONTAINERS.get(type);
    }

    @EventCalled
    public static void addTranslations(AfterLanguageLoadEvent event) {
        BlockSetAPI.getRegistries().forEach(r -> r.addTypeTranslations(event));
    }

    @ExpectPlatform
    public static <T extends BlockType, E> void addDynamicRegistration(
            BlockSetAPI.BlockTypeRegistryCallback<E, T> registrationFunction, Class<T> blockType,
            Registry<E> registry) {
        throw new AssertionError();
    }

    public static <T extends BlockType> void addDynamicBlockRegistration(
            BlockSetAPI.BlockTypeRegistryCallback<Block, T> registrationFunction, Class<T> blockType) {
        addDynamicRegistration(registrationFunction, blockType, BuiltInRegistries.BLOCK);
    }

    public static <T extends BlockType> void addDynamicItemRegistration(
            BlockSetAPI.BlockTypeRegistryCallback<Item, T> registrationFunction, Class<T> blockType) {
        addDynamicRegistration(registrationFunction, blockType, BuiltInRegistries.ITEM);
    }


    public static Collection<BlockTypeRegistry<?>> getRegistries() {
        return BLOCK_SET_CONTAINERS.values();
    }

    @Nullable
    public static <T extends BlockType> BlockTypeRegistry<T> getRegistry(Class<T> typeClass) {
        return (BlockTypeRegistry<T>) BLOCK_SET_CONTAINERS.get(typeClass);
    }

    @Nullable
    public static <T extends BlockType> T getBlockTypeOf(ItemLike itemLike, Class<T> typeClass) {
        BlockTypeRegistry<T> r = getRegistry(typeClass);
        if (r != null) {
            return r.getBlockTypeOf(itemLike);
        }
        return null;
    }
}

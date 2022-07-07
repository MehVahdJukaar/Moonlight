package net.mehvahdjukaar.moonlight.api.set;

import dev.architectury.injectables.annotations.ExpectPlatform;
import net.mehvahdjukaar.moonlight.api.misc.Registrator;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.ApiStatus;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;

public class BlockSetManager {

    //Frick mod loading is multi-threaded, so we need to beware of concurrent access
    private static final Map<Class<? extends BlockType>, BlockTypeRegistry<?>> BLOCK_SET_CONTAINERS = new ConcurrentHashMap<>();
    private static final ConcurrentLinkedDeque<Runnable> FINDER_ADDER = new ConcurrentLinkedDeque<>();
    private static final ConcurrentLinkedDeque<Runnable> REMOVER_ADDER = new ConcurrentLinkedDeque<>();


    /**
     * Registers a block set definition (like wood type, leaf type etc...)
     * Can be called only during mod startup (not during mod setup as it needs to run before registry events
     *
     * @param typeRegistry block set registry class instance. This contains all the logic that determines how a blockset
     *                     gets formed
     * @param <T>          IBlockType
     */
    public static <T extends BlockType> void registerBlockSetDefinition(BlockTypeRegistry<T> typeRegistry) {
        if (hasFilledBlockSets()) {
            throw new UnsupportedOperationException(
                    String.format("Tried to addListener block set definition %s after registry events", typeRegistry));
        }
        BLOCK_SET_CONTAINERS.put(typeRegistry.getType(), typeRegistry);
    }

    /**
     * Use this function to add a (modded) block type finder manually.
     * This is handy for block types that are unique and which can't be detected by the detection system defined in their BlockSetContainer class
     * Call during mod startup (not mod setup as it will be too late for this to affect block registration)
     *
     * @param blockFinder Finder object that will provide the modded block type when the time is right
     */
    public static <T extends BlockType> void addBlockTypeFinder(Class<T> type, BlockType.SetFinder<T> blockFinder) {
        if (hasFilledBlockSets()) {
            throw new UnsupportedOperationException(
                    String.format("Tried to addListener block %s finder %s after registry events", type, blockFinder));
        }
        FINDER_ADDER.add(() -> {
            BlockTypeRegistry<T> container = getBlockSet(type);
            container.addFinder(blockFinder);
        });
    }

    /**
     * Use this function to remove incorrectly detected block types from your mod. The opposite
     * Call during mod startup (not mod setup as it will be too late for this to affect block registration)
     *
     * @param id id of the block that is getting erroneously added and should be removed
     */
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

    @FunctionalInterface
    public interface BlockTypeRegistryCallback<E, T extends BlockType> {
        void accept(Registrator<E> reg, Collection<T> wood);
    }

    /**
     * Add a registry function meant to addListener a set of blocks that use a specific wood type
     * Other entries like items can access the block types directly since it will be filled
     * Will be called (hopefully) after all other block registrations have been fired so the block set type is complete
     * Note that whatever gets registered here should in no way influence the block sets themselves (you shouldn't add new wood types here for example)
     *
     * @param registrationFunction registry function
     */
    @ExpectPlatform
    public static <T extends BlockType> void addDynamicBlockRegistration(
            BlockTypeRegistryCallback<Block, T> registrationFunction, Class<T> blockType) {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static <T extends BlockType> void addDynamicItemRegistration(
            BlockTypeRegistryCallback<Item,T> registrationFunction, Class<T> blockType) {
        throw new AssertionError();
    }

    @ExpectPlatform
    protected static boolean hasFilledBlockSets() {
        throw new AssertionError();
    }


    //do NOT call
    @ApiStatus.Internal
    public static void initializeBlockSets() {
        if(hasFilledBlockSets())throw new UnsupportedOperationException("block sets have already bee initialized");
        FINDER_ADDER.forEach(Runnable::run);
        FINDER_ADDER.clear();

        BLOCK_SET_CONTAINERS.values().forEach(BlockTypeRegistry::buildAll);

        //remove not wanted ones
        REMOVER_ADDER.forEach(Runnable::run);
        REMOVER_ADDER.clear();
    }


    public static Collection<BlockTypeRegistry<?>> getRegistries() {
        return BLOCK_SET_CONTAINERS.values();
    }

    @Nullable
    public static BlockTypeRegistry<?> getRegistry(Class<? extends BlockType> typeClass) {
        return BLOCK_SET_CONTAINERS.get(typeClass);
    }
}

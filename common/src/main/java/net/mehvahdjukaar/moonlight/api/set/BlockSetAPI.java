package net.mehvahdjukaar.moonlight.api.set;

import net.mehvahdjukaar.moonlight.api.misc.Registrator;
import net.mehvahdjukaar.moonlight.core.set.BlockSetInternal;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.function.Consumer;

//Interface for Modders!
public class BlockSetAPI {

    /**
     * Registers a block set definition (like wood type, leaf type etc...)
     * Can be called only during mod startup (not during mod setup as it needs to run before registry events
     *
     * @param typeRegistry block set registry class instance. This contains all the logic that determines how a blockset
     *                     gets formed
     * @param <T>          IBlockType
     */
    public static <T extends BlockType> void registerBlockSetDefinition(BlockTypeRegistry<T> typeRegistry) {
        BlockSetInternal.registerBlockSetDefinition(typeRegistry);
    }

    /**
     * Use this function to add a (modded) block type finder manually. (i.e. for your wood type)
     * This is handy for block types that are unique and which can't be detected by the detection system defined in their BlockSetContainer class
     * Call during mod startup (not mod setup as it will be too late for this to affect block registration)
     *
     * @param type        the block type class you are registering this for (WoodType.class, LeafType.class...)
     * @param blockFinder Finder object that will provide the modded block type when the time is right
     */
    @Deprecated
    public static <T extends BlockType> void addBlockTypeFinder(Class<T> type, BlockType.SetFinder<T> blockFinder) {
        BlockSetInternal.addBlockTypeFinder(type, blockFinder);
    }

    /**
     * Use this function to remove incorrectly detected block types from your mod. The opposite
     * Call during mod startup (not mod setup as it will be too late for this to affect block registration)
     *
     * @param id id of the block that is getting erroneously added and should be removed
     */
    @Deprecated
    public static <T extends BlockType> void addBlockTypeRemover(Class<T> type, Identifier id) {
        BlockSetInternal.addBlockTypeRemover(type, id);
    }

    public static <T extends BlockType> BlockTypeRegistry<T> getBlockSet(Class<T> type) {
        return BlockSetInternal.getBlockSet(type);
    }

    @FunctionalInterface
    public interface BlockTypeRegistryCallback<E, T extends BlockType> {
        void accept(Registrator<E> reg, Collection<T> wood);
    }

    public static <E> void addDynamicRegistration(
            String myModId, Consumer<Registrator<E>> registrationFunction, Registry<E> registry) {
        BlockSetInternal.addDynamicRegistration(myModId, registrationFunction, registry);
    }


    /**
     * Grabs all blockTypes registries
     */
    public static Collection<BlockTypeRegistry<?>> getRegistries() {
        return BlockSetInternal.getRegistries();
    }

    @Nullable
    public static<T extends BlockType> BlockTypeRegistry<T> getTypeRegistry(Class<T> typeClass) {
        return BlockSetInternal.getRegistry(typeClass);
    }

    /**
     * @return BlockType of the provided block
     */
    @Nullable
    public static <T extends BlockType> T getBlockTypeOf(ItemLike itemLike, Class<T> typeClass){
        return BlockSetInternal.getBlockTypeOf(itemLike, typeClass);
    }


    /**
     * Tries changing an object block type. Returns null if it fails
     *
     * @param current        target item
     * @param originalMat    material from which the target item is made of
     * @param destinationMat desired block type
     */
    @Nullable
    public static Object changeType(Object current, BlockType originalMat, BlockType destinationMat) {
        return BlockType.changeType(current, originalMat, destinationMat);
    }

    //for items
    @Nullable
    public static Item changeItemType(Item current, BlockType originalMat, BlockType destinationMat) {
        return BlockType.changeItemType(current, originalMat, destinationMat);
    }

    //for blocks
    @Nullable
    public static Block changeBlockType(Block current, BlockType originalMat, BlockType destinationMat) {
        return BlockType.changeBlockType(current, originalMat, destinationMat);
    }
}

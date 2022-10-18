package net.mehvahdjukaar.moonlight.api.set;

import net.mehvahdjukaar.moonlight.api.misc.Registrator;
import net.mehvahdjukaar.moonlight.core.set.BlockSetInternal;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

import javax.annotation.Nullable;
import java.util.Collection;

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
    public static <T extends BlockType> void addBlockTypeFinder(Class<T> type, BlockType.SetFinder<T> blockFinder) {
        BlockSetInternal.addBlockTypeFinder(type, blockFinder);
    }

    /**
     * Use this function to remove incorrectly detected block types from your mod. The opposite
     * Call during mod startup (not mod setup as it will be too late for this to affect block registration)
     *
     * @param id id of the block that is getting erroneously added and should be removed
     */
    public static <T extends BlockType> void addBlockTypeRemover(Class<T> type, ResourceLocation id) {
        BlockSetInternal.addBlockTypeRemover(type, id);
    }

    public static <T extends BlockType> BlockTypeRegistry<T> getBlockSet(Class<T> type) {
        return BlockSetInternal.getBlockSet(type);
    }

    @FunctionalInterface
    public interface BlockTypeRegistryCallback<E, T extends BlockType> {
        void accept(Registrator<E> reg, Collection<T> wood);
    }

    /**
     * Add a registry function meant to register a set of blocks that use a specific wood type
     * Other entries like items can access the block types directly since it will be filled
     * Will be called (hopefully) after all other blocks registrations have been fired so the block set type is complete
     * Note that whatever gets registered here should in no way influence the block sets themselves (you shouldn't add new wood types here for example)
     *
     * @param registrationFunction registry function
     */
    public static <T extends BlockType> void addDynamicBlockRegistration(
            BlockTypeRegistryCallback<Block, T> registrationFunction, Class<T> blockType) {
        addDynamicRegistration(registrationFunction, blockType, Registry.BLOCK);
    }

    public static <T extends BlockType> void addDynamicItemRegistration(
            BlockTypeRegistryCallback<Item, T> registrationFunction, Class<T> blockType) {
        addDynamicRegistration(registrationFunction, blockType, Registry.ITEM);
    }

    public static <T extends BlockType, E> void addDynamicRegistration(
            BlockSetAPI.BlockTypeRegistryCallback<E, T> registrationFunction, Class<T> blockType,
            Registry<E> registry) {
        BlockSetInternal.addDynamicRegistration(registrationFunction, blockType, registry);
    }


    public static Collection<BlockTypeRegistry<?>> getRegistries() {
        return BlockSetInternal.getRegistries();
    }

    @Nullable
    public static BlockTypeRegistry<?> getTypeRegistry(Class<? extends BlockType> typeClass) {
        return BlockSetInternal.getRegistry(typeClass);
    }
}

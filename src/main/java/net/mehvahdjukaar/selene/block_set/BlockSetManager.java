package net.mehvahdjukaar.selene.block_set;

import com.mojang.datafixers.util.Pair;
import net.mehvahdjukaar.selene.block_set.wood.WoodType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.ForgeRegistry;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.IForgeRegistryEntry;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.function.Consumer;

public class BlockSetManager {

    private static boolean hasFilledBlockSets = false;

    //Frick mod loading is multi-threaded, so we need to beware of concurrent access
    private static final Map<Class<? extends BlockType>, BlockTypeRegistry<?>> BLOCK_SET_CONTAINERS = new ConcurrentHashMap<>();
    private static final ConcurrentLinkedDeque<Runnable> FINDER_ADDER = new ConcurrentLinkedDeque<>();
    private static final ConcurrentLinkedDeque<Runnable> REMOVER_ADDER = new ConcurrentLinkedDeque<>();

    //maps containing mod ids and block and items runnable. Block one is ready to run, items needs the bus supplied to it
    //they will be run each mod at a time block first then items
    private static final Map<String, Pair<
            List<Runnable>, //block registration function
            List<Consumer<RegistryEvent.Register<Item>>> //item registration function
            >>
            LATE_REGISTRATION_QUEUE = new ConcurrentHashMap<>();

    /**
     * Registers a block set definition (like wood type, leaf type etc...)
     * Can be called only during mod startup (not during mod setup as it needs to run before registry events
     *
     * @param type         class of the target block set
     * @param typeRegistry block set registry class instance. This contains all the logic that determines how a blockset
     *                     gets formed
     * @param <T>          IBlockType
     */
    public static <T extends BlockType> void registerBlockSetDefinition(Class<T> type, BlockTypeRegistry<T> typeRegistry) {
        if (hasFilledBlockSets) {
            throw new UnsupportedOperationException(
                    String.format("Tried to register block set definition %s for block type %s after registry events", typeRegistry, type));
        }
        BLOCK_SET_CONTAINERS.put(type, typeRegistry);
    }

    /**
     * Use this function to register a (modded) block type finder manually.
     * This is handy for block types that are unique and which can't be detected by the detection system defined in their BlockSetContainer class
     * Call during mod startup (not mod setup as it will be too late for this to affect block registration)
     *
     * @param blockFinder Finder object that will provide the modded block type when the time is right
     */
    public static <T extends BlockType> void addBlockTypeFinder(Class<T> type, BlockType.SetFinder<T> blockFinder) {
        if (hasFilledBlockSets) {
            throw new UnsupportedOperationException(
                    String.format("Tried to register block %s finder %s after registry events", type, blockFinder));
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
        if (hasFilledBlockSets) {
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
    public interface BlockSetRegistryCallback<T extends BlockType, R extends IForgeRegistryEntry<R>> {
        void accept(RegistryEvent.Register<R> reg, Collection<T> wood);
    }

    @Deprecated
    public static <R extends IForgeRegistryEntry<R>> void addWoodRegistrationCallback(
            BlockSetRegistryCallback<WoodType, R> registrationFunction, Class<R> regType) {
        addBlockSetRegistrationCallback(registrationFunction, regType, WoodType.class);
    }

    /**
     * Add a registry function meant to register a set of blocks that use a specific wood type
     * Other entries like items can access the block types directly since it will be filled
     * Will be called (hopefully) after all other block registrations have been fired so the block set type is complete
     * Note that whatever gets registered here should in no way influence the block sets themselves (you shouldn't add new wood types here for example)
     *
     * @param registrationFunction registry function
     */
    public static <T extends BlockType, R extends IForgeRegistryEntry<R>> void addBlockSetRegistrationCallback(
            BlockSetRegistryCallback<T, R> registrationFunction, Class<R> regType, Class<T> blockType) {
        //this is horrible. worst shit ever
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        Consumer<RegistryEvent.Register<R>> eventConsumer;

        if (regType == Block.class || regType == Item.class) {

            //get the queue corresponding to this certain mod
            String modId = ModLoadingContext.get().getActiveContainer().getModId();

            var registrationQueues =
                    LATE_REGISTRATION_QUEUE.computeIfAbsent(modId, s -> {
                        //if absent we register its registration callback
                        bus.addGenericListener(Item.class, EventPriority.HIGHEST, BlockSetManager::registerLateBlockAndItems);
                        return Pair.of(new ArrayList<>(), new ArrayList<>());
                    });


            if (regType == Block.class) {
                //if block makes a function that just adds the bus and runnable to the queue whenever reg block is fired
                eventConsumer = e -> {
                    //actual runnable which will registers the blocks
                    Runnable lateRegistration = () -> {

                        IForgeRegistry<?> registry = e.getRegistry();
                        if (registry instanceof ForgeRegistry fr) {
                            boolean frozen = fr.isLocked();
                            fr.unfreeze();
                            registrationFunction.accept(e, getBlockSet(blockType).getTypes().values());
                            if (frozen) fr.freeze();
                        }
                    };
                    //when this reg block event fires we only add a runnable to the queue
                    registrationQueues.getFirst().add(lateRegistration);
                };
                //registering block event to the bus

                bus.addGenericListener(regType, EventPriority.HIGHEST, eventConsumer);
            } else {
                //items just get added to the queue. they will already be called with the correct event

                Consumer<RegistryEvent.Register<Item>> itemEvent = e ->
                        registrationFunction.accept((RegistryEvent.Register<R>) e, getBlockSet(blockType).getTypes().values());
                registrationQueues.getSecond().add(itemEvent);
            }
        } else {
            //non block /item event. just wraps it by giving it the wood types

            eventConsumer = e -> registrationFunction.accept(e, getBlockSet(blockType).getTypes().values());
            bus.addGenericListener(regType, eventConsumer);
        }
    }


    //shittiest code ever lol
    protected static void registerLateBlockAndItems(RegistryEvent.Register<Item> event) {
        //when the first registration function is called we find all block types
        if (!hasFilledBlockSets) {
            initializeBlockSets();
            hasFilledBlockSets = true;
        }
        //get the queue corresponding to this certain mod
        String modId = ModLoadingContext.get().getActiveContainer().getModId();
        var registrationQueues =
                LATE_REGISTRATION_QUEUE.get(modId);
        if (registrationQueues != null) {
            //register blocks
            var blockQueue = registrationQueues.getFirst();
            blockQueue.forEach(Runnable::run);
            //registers items
            var itemQueue = registrationQueues.getSecond();
            itemQueue.forEach(q -> q.accept(event));
        }
        //clears stuff that's been execured. not really needed but just to be safe its here
        LATE_REGISTRATION_QUEUE.remove(modId);
    }


    private static void initializeBlockSets() {
        FINDER_ADDER.forEach(Runnable::run);
        FINDER_ADDER.clear();

        //wood types need to run before leaves
        BLOCK_SET_CONTAINERS.get(WoodType.class).buildAll();

        for (var c : BLOCK_SET_CONTAINERS.entrySet()) {
            c.getValue().buildAll();
        }

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

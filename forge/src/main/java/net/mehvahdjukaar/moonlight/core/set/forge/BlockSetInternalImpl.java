package net.mehvahdjukaar.moonlight.core.set.forge;

import net.mehvahdjukaar.moonlight.api.platform.RegHelper;
import net.mehvahdjukaar.moonlight.api.set.BlockSetAPI;
import net.mehvahdjukaar.moonlight.api.set.BlockType;
import net.mehvahdjukaar.moonlight.api.set.BlockTypeRegistry;
import net.mehvahdjukaar.moonlight.core.set.BlockSetInternal;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.ForgeRegistry;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegisterEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

//spaghetti code. Do not touch, it just works
public class BlockSetInternalImpl {

    //maps containing mod ids and block and items runnable. Block one is ready to run, items needs the bus supplied to it
    //they will be run each mod at a time block first then items
    private static final Map<ResourceKey<? extends Registry<?>>,
            Map<String, List<Runnable>>> LATE_REGISTRATION_QUEUE = new ConcurrentHashMap<>();

    private static boolean hasFilledBlockSets = false;
    private static boolean hasRegisteredDynamic = false;

    //aaaa
    public static <T extends BlockType, E> void addDynamicRegistration(
            BlockSetAPI.BlockTypeRegistryCallback<E, T> registrationFunction, Class<T> blockType, Registry<E> registry) {
        if (registry == BuiltInRegistries.BLOCK) {
            addEvent(ForgeRegistries.BLOCKS, (BlockSetAPI.BlockTypeRegistryCallback<Block, T>) registrationFunction, blockType);
        } else if (registry == BuiltInRegistries.ITEM) {
            addEvent(ForgeRegistries.ITEMS, (BlockSetAPI.BlockTypeRegistryCallback<Item, T>) registrationFunction, blockType);
        } else if (registry == BuiltInRegistries.FLUID || registry == BuiltInRegistries.SOUND_EVENT) {
            throw new IllegalArgumentException("Fluid and Sound Events registry not supported here");
        } else {
            //ensure has filled block set
            getOrAddQueue(Registries.BLOCK_ENTITY_TYPE); //dummy key
            //other entries
            RegHelper.registerInBatch(registry, e -> registrationFunction.accept(e, BlockSetAPI.getBlockSet(blockType).getValues()));
        }
    }


    public static <T extends BlockType, E> void addEvent(IForgeRegistry<E> reg,
                                                         BlockSetAPI.BlockTypeRegistryCallback<E, T> registrationFunction,
                                                         Class<T> blockType) {

        Consumer<RegisterEvent> eventConsumer;

        List<Runnable> registrationQueues = getOrAddQueue(reg.getRegistryKey());

        //if block makes a function that just adds the bus and runnable to the queue whenever reg block is fired
        eventConsumer = e -> {
            if (e.getRegistryKey().equals(reg.getRegistryKey())) {
                //actual runnable which will registers the blocks
                Runnable lateRegistration = () -> {
                    IForgeRegistry<E> registry = e.getForgeRegistry();
                    if (registry instanceof ForgeRegistry<?> fr) {
                        boolean frozen = fr.isLocked();
                        fr.unfreeze();
                        registrationFunction.accept(registry::register, BlockSetAPI.getBlockSet(blockType).getValues());
                        if (frozen) fr.freeze();
                    }
                };
                //when this reg block event fires we only add a runnable to the queue
                registrationQueues.add(lateRegistration);
            }
        };
        //registering block event to the bus
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        bus.addListener(EventPriority.HIGHEST, eventConsumer);
    }

    @NotNull
    private static <R> List<Runnable> getOrAddQueue(ResourceKey<Registry<R>> reg) {
        //this is horrible. worst shit ever
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        //get the queue corresponding to this certain mod
        String modId = ModLoadingContext.get().getActiveContainer().getModId();
        if (LATE_REGISTRATION_QUEUE.isEmpty()) {
            //just added once by whoever arrives first
            bus.addListener(EventPriority.HIGHEST, BlockSetInternalImpl::registerLateBlockAndItems);
        }
        return LATE_REGISTRATION_QUEUE.computeIfAbsent(reg,
                r -> new LinkedHashMap<>()).computeIfAbsent(modId,
                r -> new ArrayList<>());
    }


    //shittiest code ever lol
    protected static void registerLateBlockAndItems(RegisterEvent event) {

        // just first once called by whichever mod called the event first
        // fires right after items so we also have all modded items filled in (for EC)
        if (event.getRegistryKey().equals(ForgeRegistries.ENTITY_TYPES.getRegistryKey()) && !hasRegisteredDynamic) {
            hasRegisteredDynamic = true;
            //when the first registration function is called we find all block types

            //fires right after blocks
            if (!hasFilledBlockSets) {
                BlockSetInternal.initializeBlockSets();
                hasFilledBlockSets = true;
            }

            //get the queue corresponding to this certain mod
            var registrationQueues = LATE_REGISTRATION_QUEUE;

            var blockQueue = registrationQueues.remove(ForgeRegistries.BLOCKS.getRegistryKey());
            if (blockQueue != null) {
                //register blocks
                for (var list : blockQueue.values()) {
                    list.forEach(Runnable::run);
                }
            }
            var itemQueue = registrationQueues.remove(ForgeRegistries.ITEMS.getRegistryKey());
            if (itemQueue != null) {
                //register items
                for (var list : itemQueue.values()) {
                    list.forEach(Runnable::run);
                }
            }
            //other
            for (var e : registrationQueues.values()) {
                for (var list : e.values()) {
                    list.forEach(Runnable::run);
                }
            }
            //clears stuff that's been executed. not really needed but just to be safe its here
            LATE_REGISTRATION_QUEUE.clear();
        }
    }

    public static boolean hasFilledBlockSets() {
        return hasFilledBlockSets;
    }


}

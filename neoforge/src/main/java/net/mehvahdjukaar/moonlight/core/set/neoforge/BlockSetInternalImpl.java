package net.mehvahdjukaar.moonlight.core.set.neoforge;

import net.mehvahdjukaar.moonlight.api.platform.RegHelper;
import net.mehvahdjukaar.moonlight.api.set.BlockSetAPI;
import net.mehvahdjukaar.moonlight.api.set.BlockType;
import net.mehvahdjukaar.moonlight.api.set.BlockTypeRegistry;
import net.mehvahdjukaar.moonlight.core.set.BlockSetInternal;
import net.mehvahdjukaar.moonlight.neoforge.MoonlightForge;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.neoforge.registries.RegisterEvent;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

//spaghetti code. Do not touch, it just works
public class BlockSetInternalImpl {

    //maps containing mod ids and block and items runnable. Block one is ready to run, items needs the bus supplied to it
    //they will be run each mod at a time block first then items
    private static final Map<String, List<Runnable>> LATE_REGISTRATION_QUEUE = new ConcurrentHashMap<>();

    private static boolean hasFilledBlockSets = false;

    static {
        //if loaded registers post item init
        Consumer<RegisterEvent> eventConsumer = e -> {
            //fired right after items
            if (e.getRegistryKey().equals(BuiltInRegistries.POTION.key())) {
                BlockSetInternal.getRegistries().forEach(BlockTypeRegistry::onItemInit);
            }
        };
        MoonlightForge.getCurrentBus().addListener(eventConsumer);
    }

    //aaaa
    public static <T extends BlockType, E> void addDynamicRegistration(
            BlockSetAPI.BlockTypeRegistryCallback<E, T> registrationFunction, Class<T> blockType, Registry<E> registry) {
        if (registry == BuiltInRegistries.BLOCK) {
            addEvent(BuiltInRegistries.BLOCK, (BlockSetAPI.BlockTypeRegistryCallback<Block, T>) registrationFunction, blockType);
        } else if (registry == BuiltInRegistries.ITEM) {
            addEvent(BuiltInRegistries.ITEM, (BlockSetAPI.BlockTypeRegistryCallback<Item, T>) registrationFunction, blockType);
        } else if (registry == BuiltInRegistries.FLUID || registry == BuiltInRegistries.SOUND_EVENT) {
            throw new IllegalArgumentException("Fluid and Sound Events registry not supported here");
        } else {
            //ensure has filled block set
            getOrAddQueue();
            //other entries
            RegHelper.registerInBatch(registry, e -> registrationFunction.accept(e, BlockSetAPI.getBlockSet(blockType).getValues()));
        }
    }


    public static <T extends BlockType, E> void addEvent(Registry<E> reg,
                                                         BlockSetAPI.BlockTypeRegistryCallback<E, T> registrationFunction,
                                                         Class<T> blockType) {
        List<Runnable> registrationQueues = getOrAddQueue();

        //if block makes a function that just adds the bus and runnable to the queue whenever reg block is fired
        //actual runnable which will registers the blocks
        Runnable lateRegistration = () -> {
            registrationFunction.accept((r, o) -> Registry.register(reg, r, o),
                    BlockSetAPI.getBlockSet(blockType).getValues());
        };
        //when this reg block event fires we only add a runnable to the queue
        registrationQueues.add(lateRegistration);
    }

    @NotNull
    private static List<Runnable> getOrAddQueue() {
        //this is horrible. worst shit ever
        IEventBus bus = MoonlightForge.getCurrentBus();
        //get the queue corresponding to this certain mod
        String modId = ModLoadingContext.get().getActiveContainer().getModId();

        List<Runnable> list;
        if (!LATE_REGISTRATION_QUEUE.containsKey(modId)) {
            list = new ArrayList<>();
            //if absent we register its registration callback
            Consumer<RegisterEvent> eventConsumer = r -> {
                BlockSetInternalImpl.registerLateBlockAndItems(r, list);
            };
            bus.addListener(EventPriority.HIGHEST, eventConsumer);
            LATE_REGISTRATION_QUEUE.put(modId, list);
        } else {
            list = LATE_REGISTRATION_QUEUE.get(modId);
        }
        return list;
    }


    //shittiest code ever lol
    protected static void registerLateBlockAndItems(RegisterEvent event,
                                                    List<Runnable> toRun) {
        //fires right after blocks
        if (event.getRegistryKey().equals(BuiltInRegistries.ATTRIBUTE.key())) {
            if (!hasFilledBlockSets) {
                BlockSetInternal.initializeBlockSets();
                hasFilledBlockSets = true;
            }
        }

        // fires right after items so we also have all modded items filled in (for EC)
        if (event.getRegistryKey().equals(Registries.ENTITY_TYPE)) {
            //when the first registration function is called we find all block types

            BlockSetInternal.getRegistries().forEach(BlockTypeRegistry::onItemInit);
            // prob not needed
            if (!hasFilledBlockSets) {
                BlockSetInternal.initializeBlockSets();
                hasFilledBlockSets = true;
            }
            //get the queue corresponding to this certain mod

            //register blocks
            toRun.forEach(Runnable::run);

            toRun.clear();
            //clears stuff that's been executed. not really needed but just to be safe its here
        }
    }

    public static boolean hasFilledBlockSets() {
        return hasFilledBlockSets;
    }


}

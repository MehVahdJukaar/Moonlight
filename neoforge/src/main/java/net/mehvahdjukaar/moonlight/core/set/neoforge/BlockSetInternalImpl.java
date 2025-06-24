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
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.neoforge.registries.RegisterEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

//spaghetti code. Do not touch, it just works
public class BlockSetInternalImpl {

    //maps containing mod ids and block and items runnable. Block one is ready to run, items needs the bus supplied to it
    //they will be run each mod at a time block first then items
    private static final Map<String,
            Map<ResourceKey<? extends Registry<?>>, List<Runnable>>> LATE_REGISTRATION_QUEUE = new ConcurrentHashMap<>();

    private static boolean hasFilledBlockSets = false;

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
            getOrAddQueue(Registries.POTION);
            //other entries
            RegHelper.registerInBatch(registry, e -> registrationFunction.accept(e, BlockSetAPI.getBlockSet(blockType).getValues()));
        }
    }


    public static <T extends BlockType, E> void addEvent(Registry<E> reg,
                                                         BlockSetAPI.BlockTypeRegistryCallback<E, T> registrationFunction,
                                                         Class<T> blockType) {
        List<Runnable> registrationQueues = getOrAddQueue( reg.key());

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
    private static List<Runnable> getOrAddQueue(@Nullable ResourceKey<? extends Registry<?>> regKey) {
        //this is horrible. worst shit ever
        IEventBus bus = MoonlightForge.getCurrentBus();
        //get the queue corresponding to this certain mod
        String modId = ModLoadingContext.get().getActiveContainer().getModId();

        return LATE_REGISTRATION_QUEUE.computeIfAbsent(modId,
                m -> {
                    Map<ResourceKey<? extends Registry<?>>, List<Runnable>> map = new HashMap<>();
                    //if absent we register its registration callback
                    Consumer<RegisterEvent> eventConsumer = r -> {
                        BlockSetInternalImpl.registerLateBlockAndItems(r, map);
                    };
                    bus.addListener(EventPriority.HIGHEST, eventConsumer);
                    return map;
                }).computeIfAbsent(regKey,
                c -> new ArrayList<>());
    }


    //shittiest code ever lol
    protected static void registerLateBlockAndItems(RegisterEvent event,
                                                    Map<ResourceKey<? extends Registry<?>>, List<Runnable>> toRun) {
        // fires right after items so we also have all modded items filled in (for EC)
        if (event.getRegistryKey().equals(Registries.POTION)) {
            //when the first registration function is called we find all block types
            // prob not needed
            if (!hasFilledBlockSets) {
                BlockSetInternal.initializeBlockSets();
                hasFilledBlockSets = true;
            }
            //get the queue corresponding to this certain mod

            //register blocks
            var blockQueue = toRun.remove(Registries.BLOCK);
            if (blockQueue != null) {
                //register blocks
                blockQueue.forEach(Runnable::run);
            }
            var itemQueue = toRun.remove(Registries.ITEM);
            if (itemQueue != null) {
                //register items
                itemQueue.forEach(Runnable::run);
            }
            //other
            for (var e : toRun.entrySet()) {
                e.getValue().forEach(Runnable::run);
            }

            toRun.clear();
            //clears stuff that's been executed. not really needed but just to be safe its here
        }
    }

    public static boolean hasFilledBlockSets() {
        return hasFilledBlockSets;
    }


}
